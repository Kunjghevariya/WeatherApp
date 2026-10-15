package com.example.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room @Entity representing weather forecast metadata such as source information,
 * licensing, and last-updated timestamp.
 */
@Entity(
    tableName = "forecast_metadata",
    indices = [
        Index(value = ["location_key"], unique = true),
        Index(value = ["last_updated"])
    ]
)
data class ForecastMetadata(
    @PrimaryKey
    @ColumnInfo(name = "location_key")
    val locationKey: String, // Coordinates or place key (e.g. "51.5074_-0.1278")

    @ColumnInfo(name = "city_name")
    val cityName: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(), // Epoch millisecond timestamp

    @ColumnInfo(name = "provider_updated_at")
    val providerUpdatedAt: String = "", // Upstream provider's timestamp (ISO 8601)

    @ColumnInfo(name = "source")
    val source: String = "MET Norway Locationforecast 2.0", // Upstream data source

    @ColumnInfo(name = "license")
    val license: String = "CC BY 4.0", // License & attribution

    @ColumnInfo(name = "license_notice")
    val licenseNotice: String = license,

    @ColumnInfo(name = "http_last_modified")
    val httpLastModified: String? = null,

    @ColumnInfo(name = "cache_status")
    val cacheStatus: String = "LIVE", // "LIVE", "CACHED", "STALE"

    @ColumnInfo(name = "alert_type")
    val alertType: String? = null, // "severe", "advisory", "warning", etc.

    @ColumnInfo(name = "alert_headline")
    val alertHeadline: String? = null,

    @ColumnInfo(name = "alert_severity")
    val alertSeverity: String? = null, // "WARNING", "WATCH", "ADVISORY"

    @ColumnInfo(name = "alert_id")
    val alertId: String? = null,

    @ColumnInfo(name = "alert_description")
    val alertDescription: String? = null
)
