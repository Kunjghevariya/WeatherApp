package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.database.ForecastMetadata
import com.example.database.HourlyForecast
import com.example.database.WeatherDao
import com.example.database.WeatherDatabase
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
class WeatherDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var weatherDao: WeatherDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        weatherDao = database.weatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryHourlyForecastsByLocationAndTimeRange() = runBlocking {
        val locationKey = "48.8566_2.3522" // Paris
        val baseTime = 1700000000000L

        // Insert metadata parent
        val metadata = ForecastMetadata(
            locationKey = locationKey,
            cityName = "Paris",
            latitude = 48.8566,
            longitude = 2.3522,
            lastUpdated = baseTime,
            source = "MET Norway"
        )
        weatherDao.insertMetadata(metadata)

        val hourlyList = listOf(
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 3600_000L, // Hour 1: 1700003600000
                temperature = 16.0,
                precipitation = 0.0,
                wind = 12.0
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 7200_000L, // Hour 2: 1700007200000
                temperature = 17.5,
                precipitation = 0.2,
                wind = 14.0
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 10800_000L, // Hour 3: 1700010800000
                temperature = 19.0,
                precipitation = 1.5,
                wind = 18.0
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 14400_000L, // Hour 4: 1700014400000
                temperature = 18.0,
                precipitation = 0.0,
                wind = 10.0
            )
        )

        weatherDao.insertHourlyForecasts(hourlyList)

        // Query all by location
        val allHours = weatherDao.getHourlyForecastsByLocationOnce(locationKey)
        assertEquals(4, allHours.size)

        // Query by time range: Hour 2 to Hour 3
        val startTime = baseTime + 7200_000L
        val endTime = baseTime + 10800_000L
        val filteredHours = weatherDao.getHourlyForecastsByTimeRangeOnce(locationKey, startTime, endTime)

        assertEquals(2, filteredHours.size)
        assertEquals(baseTime + 7200_000L, filteredHours[0].timestamp)
        assertEquals(17.5, filteredHours[0].temperature, 0.01)
        assertEquals(baseTime + 10800_000L, filteredHours[1].timestamp)
        assertEquals(19.0, filteredHours[1].temperature, 0.01)

        // Test reactive Flow query
        val flowResults = weatherDao.getHourlyForecastsByTimeRange(locationKey, startTime, endTime).first()
        assertEquals(2, flowResults.size)
    }

    @Test
    fun deleteHourlyForecastsByLocationAndTimeRange() = runBlocking {
        val locationKey = "52.5200_13.4050" // Berlin
        val baseTime = 1700000000000L

        val metadata = ForecastMetadata(
            locationKey = locationKey,
            cityName = "Berlin",
            latitude = 52.5200,
            longitude = 13.4050,
            lastUpdated = baseTime
        )
        weatherDao.insertMetadata(metadata)

        val hourlyList = listOf(
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 3600_000L,
                temperature = 12.0,
                precipitation = 0.0,
                wind = 8.0
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 7200_000L,
                temperature = 13.0,
                precipitation = 0.4,
                wind = 10.0
            ),
            HourlyForecast(
                locationKey = locationKey,
                timestamp = baseTime + 10800_000L,
                temperature = 14.5,
                precipitation = 0.0,
                wind = 9.0
            )
        )
        weatherDao.insertHourlyForecasts(hourlyList)

        // Delete Hour 2 only by time range
        val deletedCount = weatherDao.deleteHourlyForecastsByTimeRange(
            locationKey = locationKey,
            startTime = baseTime + 7200_000L,
            endTime = baseTime + 7200_000L
        )
        assertEquals(1, deletedCount)

        val remainingHours = weatherDao.getHourlyForecastsByLocationOnce(locationKey)
        assertEquals(2, remainingHours.size)
        assertEquals(baseTime + 3600_000L, remainingHours[0].timestamp)
        assertEquals(baseTime + 10800_000L, remainingHours[1].timestamp)

        // Delete all remaining by location
        weatherDao.deleteHourlyForecastsByLocation(locationKey)
        val emptyHours = weatherDao.getHourlyForecastsByLocationOnce(locationKey)
        assertTrue(emptyHours.isEmpty())
    }

    @Test
    fun weatherDatabaseCompanion_getInstanceReturnsValidSingleton() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db1 = WeatherDatabase.getInstance(context)
        val db2 = WeatherDatabase.getInstance(context)
        assertSame(db1, db2)
        assertNotNull(db1.weatherDao())
        assertNotNull(db1.forecastDao())
    }
}
