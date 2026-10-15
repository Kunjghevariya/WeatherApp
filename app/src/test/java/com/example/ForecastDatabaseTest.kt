package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.WeatherDatabase
import com.example.database.ForecastDao
import com.example.database.ForecastMetadata
import com.example.database.HourlyForecast
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
class ForecastDatabaseTest {

    private lateinit var db: WeatherDatabase
    private lateinit var forecastDao: ForecastDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        forecastDao = db.forecastDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQueryForecastMetadata() = runBlocking {
        val metadata = ForecastMetadata(
            locationKey = "51.5074_-0.1278",
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            lastUpdated = 1700000000000L,
            source = "MET Norway Locationforecast 2.0"
        )
        forecastDao.insertMetadata(metadata)

        val retrieved = forecastDao.getForecastMetadataOnce("51.5074_-0.1278")
        assertNotNull(retrieved)
        assertEquals("London", retrieved?.cityName)
        assertEquals("MET Norway Locationforecast 2.0", retrieved?.source)
        assertEquals(1700000000000L, retrieved?.lastUpdated)
    }

    @Test
    fun insertAndQueryHourlyForecasts() = runBlocking {
        val metadata = ForecastMetadata(
            locationKey = "35.6762_139.6503",
            cityName = "Tokyo",
            latitude = 35.6762,
            longitude = 139.6503,
            lastUpdated = 1700000000000L,
            source = "MET Norway"
        )
        forecastDao.insertMetadata(metadata)

        val hourlyList = listOf(
            HourlyForecast(
                locationKey = "35.6762_139.6503",
                timestamp = 1700003600000L,
                temperature = 18.5,
                feelsLike = 18.0,
                precipitation = 0.5,
                wind = 12.0
            ),
            HourlyForecast(
                locationKey = "35.6762_139.6503",
                timestamp = 1700007200000L,
                temperature = 19.2,
                feelsLike = 19.0,
                precipitation = 0.0,
                wind = 10.5
            )
        )
        forecastDao.insertHourlyForecasts(hourlyList)

        val retrievedHours = forecastDao.getHourlyForecastsOnce("35.6762_139.6503")
        assertEquals(2, retrievedHours.size)
        assertEquals(18.5, retrievedHours[0].temperature, 0.01)
        assertEquals(18.5, retrievedHours[0].temp, 0.01)
        assertEquals(0.5, retrievedHours[0].precipitation, 0.01)
        assertEquals(12.0, retrievedHours[0].wind, 0.01)
    }

    @Test
    fun relationWithHourlyWorks() = runBlocking {
        val locKey = "40.7128_-74.0060"
        val metadata = ForecastMetadata(
            locationKey = locKey,
            cityName = "New York",
            latitude = 40.7128,
            longitude = -74.0060,
            lastUpdated = 1700000000000L,
            source = "NWS & MET"
        )
        val hourlyList = listOf(
            HourlyForecast(
                locationKey = locKey,
                timestamp = 1700000000000L,
                temperature = 22.0,
                precipitation = 0.0,
                wind = 15.0
            )
        )
        forecastDao.saveForecast(metadata, hourlyList)

        val forecastWithHourly = forecastDao.getForecastWithHourly(locKey).first()
        assertNotNull(forecastWithHourly)
        assertEquals("New York", forecastWithHourly?.metadata?.cityName)
        assertEquals(1, forecastWithHourly?.hourlyForecasts?.size)
        assertEquals(22.0, forecastWithHourly?.hourlyForecasts?.first()?.temperature ?: 0.0, 0.01)
        assertEquals(22.0, forecastWithHourly?.hourlyForecasts?.first()?.temp ?: 0.0, 0.01)
    }
}
