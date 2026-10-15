package com.example.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room @Entity representing weather data for an individual hour
 * with fields for timestamp, temperature, precipitation, and wind,
 * as well as location foreign key and atmospheric metrics.
 */
@Entity(
    tableName = "hourly_forecasts",
    foreignKeys = [
        ForeignKey(
            entity = ForecastMetadata::class,
            parentColumns = ["location_key"],
            childColumns = ["location_key"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["location_key"]),
        Index(value = ["timestamp"]),
        Index(value = ["location_key", "timestamp"], unique = true)
    ]
)
data class HourlyForecast(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "location_key")
    val locationKey: String = "default", // Foreign key linking to ForecastMetadata

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L, // Epoch millisecond timestamp for this forecast point

    @ColumnInfo(name = "timestamp_iso")
    val timestampIso: String = "", // ISO 8601 string (e.g. "2026-10-13T14:00:00Z")

    @ColumnInfo(name = "hour_label")
    val hourLabel: String = "", // Formatted hour label (e.g. "14:00")

    @ColumnInfo(name = "temperature")
    val temperature: Double = 0.0, // Temperature in °C

    @ColumnInfo(name = "feels_like")
    val feelsLike: Double = temperature, // Apparent temperature in °C

    @ColumnInfo(name = "precipitation")
    val precipitation: Double = 0.0, // Expected precipitation amount in mm

    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: Int = 0, // Precipitation probability percentage (0 - 100%)

    @ColumnInfo(name = "wind")
    val wind: Double = 0.0, // Wind speed in km/h

    @ColumnInfo(name = "wind_direction_deg")
    val windDirectionDeg: Double = 0.0, // Wind direction in degrees

    @ColumnInfo(name = "wind_cardinal")
    val windCardinal: String = "N", // Cardinal wind direction (e.g. "SW", "N")

    @ColumnInfo(name = "condition_type")
    val conditionType: String = "CLEAR_DAY", // Weather condition category

    @ColumnInfo(name = "symbol_code")
    val symbolCode: String = "clearsky_day", // Icon symbol code from provider

    @ColumnInfo(name = "humidity_percent")
    val humidityPercent: Int = 50, // Relative humidity percentage

    @ColumnInfo(name = "uv_index")
    val uvIndex: Double = 0.0, // UV Index

    @ColumnInfo(name = "air_pressure_hpa")
    val airPressureHpa: Double = 1013.25, // Atmospheric pressure in hPa

    @ColumnInfo(name = "cloud_cover_percent")
    val cloudCoverPercent: Int = 0, // Cloud cover percentage

    @ColumnInfo(name = "is_daylight")
    val isDaylight: Boolean = true // Whether this hour occurs during daytime
) {
    /**
     * Backward-compatible alias for [temperature] ensuring code accessing .temp works seamlessly.
     */
    @get:Ignore
    val temp: Double
        get() = temperature

    /**
     * Secondary constructor supporting minimal parameters: timestamp, temperature, precipitation, wind.
     */
    @Ignore
    constructor(
        timestamp: Long,
        temperature: Double,
        precipitation: Double,
        wind: Double,
        locationKey: String = "default"
    ) : this(
        id = 0,
        locationKey = locationKey,
        timestamp = timestamp,
        temperature = temperature,
        feelsLike = temperature,
        precipitation = precipitation,
        wind = wind
    )
}
