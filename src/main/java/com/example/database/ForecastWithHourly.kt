package com.example.database

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation

/**
 * 1-to-many relationship linking [ForecastMetadata] with its child [HourlyForecast] records.
 */
data class ForecastWithHourly(
    @Embedded
    val metadata: ForecastMetadata,

    @Relation(
        parentColumn = "location_key",
        entityColumn = "location_key"
    )
    val hourlyForecasts: List<HourlyForecast>
) {
    @get:Ignore
    val hourlyEntries: List<HourlyForecast>
        get() = hourlyForecasts
}
