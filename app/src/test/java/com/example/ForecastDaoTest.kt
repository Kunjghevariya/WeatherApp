package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.*
import com.example.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ForecastDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var forecastDao: ForecastDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        forecastDao = database.forecastDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveForecastMetadata() = runBlocking {
        val locationKey = "51.5074_-0.1278"
        val timestamp = System.currentTimeMillis()
        val metadata = ForecastMetadata(
            locationKey = locationKey,
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            lastUpdated = timestamp,
            providerUpdatedAt = "2026-10-13T12:00:00Z",
            source = "MET Norway Locationforecast 2.0",
            licenseNotice = "CC BY 4.0",
            cacheStatus = "LIVE"
        )

        forecastDao.insertMetadata(metadata)

        val retrieved = forecastDao.getForecastMetadata(locationKey).first()
        assertNotNull(retrieved)
        assertEquals("London", retrieved?.cityName)
        assertEquals(timestamp, retrieved?.lastUpdated)
        assertEquals("MET Norway Locationforecast 2.0", retrieved?.source)
        assertEquals("CC BY 4.0", retrieved?.licenseNotice)
        assertEquals("LIVE", retrieved?.cacheStatus)
    }

    @Test
    fun atomicSaveForecast_persistsMetadataAndHourlyForecasts() = runBlocking {
        val locationKey = "40.7128_-74.0060"
        val timestamp = System.currentTimeMillis()
        val metadata = ForecastMetadata(
            locationKey = locationKey,
            cityName = "New York",
            latitude = 40.7128,
            longitude = -74.0060,
            lastUpdated = timestamp,
            providerUpdatedAt = "2026-10-13T14:00:00Z",
            source = "MET Norway Locationforecast 2.0",
            licenseNotice = "CC BY 4.0",
            cacheStatus = "LIVE"
        )

        val hourlyList = listOf(
            HourlyForecast(
                locationKey = locationKey,
                timestamp = timestamp + 3600_000L,
                timestampIso = "2026-10-13T14:00:00Z",
                hourLabel = "14:00",
                temperature = 21.5,
                feelsLike = 21.0,
                precipitation = 0.5,
                precipitationProbability = 40,
                wind = 14.2,
                windDirectionDeg = 180.0,
                windCardinal = "S",
                conditionType = "PARTLY_CLOUDY_DAY",
                symbolCode = "partlycloudy_day",
                humidityPercent = 52,
                isDaylight = true
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = timestamp + 7200_000L,
                timestampIso = "2026-10-13T15:00:00Z",
                hourLabel = "15:00",
                temperature = 22.0,
                feelsLike = 21.8,
                precipitation = 0.0,
                precipitationProbability = 5,
                wind = 12.0,
                windDirectionDeg = 190.0,
                windCardinal = "S",
                conditionType = "CLEAR_DAY",
                symbolCode = "clearsky_day",
                humidityPercent = 48,
                isDaylight = true
            )
        )

        forecastDao.saveForecast(metadata, hourlyList)

        // Retrieve relation ForecastWithHourly
        val compound = forecastDao.getForecastWithHourly(locationKey).first()
        assertNotNull(compound)
        assertEquals("New York", compound?.metadata?.cityName)
        assertEquals(2, compound?.hourlyForecasts?.size)

        val firstEntry = compound?.hourlyForecasts?.get(0)
        assertEquals("14:00", firstEntry?.hourLabel)
        assertEquals(21.5, firstEntry?.temperature ?: 0.0, 0.01)
        assertEquals(21.5, firstEntry?.temp ?: 0.0, 0.01)
        assertEquals(0.5, firstEntry?.precipitation ?: 0.0, 0.01)
        assertEquals(14.2, firstEntry?.wind ?: 0.0, 0.01)

        val secondEntry = compound?.hourlyForecasts?.get(1)
        assertEquals("15:00", secondEntry?.hourLabel)
        assertEquals(22.0, secondEntry?.temperature ?: 0.0, 0.01)
        assertEquals(22.0, secondEntry?.temp ?: 0.0, 0.01)
        assertEquals(0.0, secondEntry?.precipitation ?: 0.0, 0.01)
        assertEquals(12.0, secondEntry?.wind ?: 0.0, 0.01)
    }

    @Test
    fun cascadeDeletion_deletesHourlyForecastsWhenMetadataDeleted() = runBlocking {
        val locationKey = "35.6762_139.6503"
        val metadata = ForecastMetadata(
            locationKey = locationKey,
            cityName = "Tokyo",
            latitude = 35.6762,
            longitude = 139.6503,
            lastUpdated = System.currentTimeMillis(),
            providerUpdatedAt = "2026-10-13T10:00:00Z"
        )
        val hourly = listOf(
            HourlyForecast(
                locationKey = locationKey,
                timestamp = System.currentTimeMillis(),
                timestampIso = "2026-10-13T10:00:00Z",
                hourLabel = "10:00",
                temperature = 19.0,
                precipitation = 0.0,
                wind = 8.0,
                windDirectionDeg = 90.0,
                windCardinal = "E",
                conditionType = "CLEAR_DAY",
                symbolCode = "clearsky_day",
                humidityPercent = 45
            )
        )

        forecastDao.saveForecast(metadata, hourly)
        assertEquals(1, forecastDao.getHourlyForecastsOnce(locationKey).size)

        // Delete parent forecast metadata
        forecastDao.deleteForecast(locationKey)

        // Child hourly entries should be deleted by cascade
        val remainingHourly = forecastDao.getHourlyForecastsOnce(locationKey)
        assertTrue(remainingHourly.isEmpty())
    }

    @Test
    fun deleteOlderThan_removesStaleForecasts() = runBlocking {
        val now = System.currentTimeMillis()
        val oldKey = "old_loc"
        val freshKey = "fresh_loc"

        val oldMetadata = ForecastMetadata(
            locationKey = oldKey,
            cityName = "Old City",
            latitude = 10.0,
            longitude = 20.0,
            lastUpdated = now - 24 * 60 * 60 * 1000, // 24 hours ago
            providerUpdatedAt = "2026-10-12T10:00:00Z"
        )
        val freshMetadata = ForecastMetadata(
            locationKey = freshKey,
            cityName = "Fresh City",
            latitude = 15.0,
            longitude = 25.0,
            lastUpdated = now, // Now
            providerUpdatedAt = "2026-10-13T10:00:00Z"
        )

        forecastDao.insertMetadata(oldMetadata)
        forecastDao.insertMetadata(freshMetadata)

        val deletedCount = forecastDao.deleteOlderThan(now - 12 * 60 * 60 * 1000) // Cutoff 12h ago
        assertEquals(1, deletedCount)

        assertNull(forecastDao.getForecastMetadataOnce(oldKey))
        assertNotNull(forecastDao.getForecastMetadataOnce(freshKey))
    }
}
