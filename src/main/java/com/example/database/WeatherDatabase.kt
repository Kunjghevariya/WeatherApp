package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for caching weather forecast data, including [HourlyForecast] entries
 * and [ForecastMetadata].
 */
@Database(
    entities = [
        HourlyForecast::class,
        ForecastMetadata::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun forecastDao(): ForecastDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInMemoryDatabase(context: Context): WeatherDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                WeatherDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
