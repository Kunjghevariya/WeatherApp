package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val locationKey: String,
    val cityName: String,
    val jsonContent: String,
    val fetchedAtEpochMs: Long,
    val providerUpdatedAt: String,
    val httpLastModified: String? = null
)

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String = "UTC",
    val isCurrentLocation: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val tempUnit: String = "C",      // "C" or "F"
    val windUnit: String = "kmh",    // "kmh", "mph", "ms"
    val precipUnit: String = "mm",   // "mm", "in"
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val idealTempMin: Float = 16f,
    val idealTempMax: Float = 24f,
    val maxWindComfort: Float = 25f,
    val maxRainComfort: Float = 15f,
    val themeMode: String = "auto",  // "auto", "day", "night", "system"
    val cardOrder: String = "radar,air_health,time_travel,rain_window,wardrobe,hourly,activity,sun_moon,wind_pressure,barometer,metrics,daily",
    val hiddenCards: String = "",    // comma-separated list of disabled card keys
    val cardStyle: String = "atmospheric", // "atmospheric", "compact", "oled"
    val isOnboardingCompleted: Boolean = false,
    val useGpsLocation: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyBriefingEnabled: Boolean = true,
    val rainAlertsEnabled: Boolean = true,
    val severeAlertsEnabled: Boolean = true
)

