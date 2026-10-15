package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetWeatherResponse(
    @Json(name = "type") val type: String? = null,
    @Json(name = "properties") val properties: MetProperties? = null
)

@JsonClass(generateAdapter = true)
data class MetProperties(
    @Json(name = "meta") val meta: MetMeta? = null,
    @Json(name = "timeseries") val timeseries: List<MetTimeSeriesItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MetMeta(
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "units") val units: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class MetTimeSeriesItem(
    @Json(name = "time") val time: String,
    @Json(name = "data") val data: MetTimeSeriesData
)

@JsonClass(generateAdapter = true)
data class MetTimeSeriesData(
    @Json(name = "instant") val instant: MetInstantData? = null,
    @Json(name = "next_1_hours") val next1Hours: MetPeriodForecast? = null,
    @Json(name = "next_6_hours") val next6Hours: MetPeriodForecast? = null,
    @Json(name = "next_12_hours") val next12Hours: MetPeriodForecast? = null
)

@JsonClass(generateAdapter = true)
data class MetInstantData(
    @Json(name = "details") val details: MetInstantDetails? = null
)

@JsonClass(generateAdapter = true)
data class MetInstantDetails(
    @Json(name = "air_temperature") val airTemperature: Double? = null,
    @Json(name = "dew_point_temperature") val dewPointTemperature: Double? = null,
    @Json(name = "relative_humidity") val relativeHumidity: Double? = null,
    @Json(name = "wind_speed") val windSpeed: Double? = null,
    @Json(name = "wind_from_direction") val windFromDirection: Double? = null,
    @Json(name = "cloud_area_fraction") val cloudAreaFraction: Double? = null,
    @Json(name = "air_pressure_at_sea_level") val airPressureAtSeaLevel: Double? = null,
    @Json(name = "ultraviolet_index_clear_sky") val uvIndex: Double? = null
)

@JsonClass(generateAdapter = true)
data class MetPeriodForecast(
    @Json(name = "summary") val summary: MetPeriodSummary? = null,
    @Json(name = "details") val details: MetPeriodDetails? = null
)

@JsonClass(generateAdapter = true)
data class MetPeriodSummary(
    @Json(name = "symbol_code") val symbolCode: String? = null
)

@JsonClass(generateAdapter = true)
data class MetPeriodDetails(
    @Json(name = "precipitation_amount") val precipitationAmount: Double? = null,
    @Json(name = "precipitation_amount_max") val precipitationAmountMax: Double? = null,
    @Json(name = "precipitation_amount_min") val precipitationAmountMin: Double? = null,
    @Json(name = "probability_of_precipitation") val probabilityOfPrecipitation: Double? = null
)

// NWS Alert Models for US official warnings
@JsonClass(generateAdapter = true)
data class NwsAlertsResponse(
    @Json(name = "features") val features: List<NwsAlertFeature> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NwsAlertFeature(
    @Json(name = "properties") val properties: NwsAlertProperties? = null
)

@JsonClass(generateAdapter = true)
data class NwsAlertProperties(
    @Json(name = "id") val id: String? = null,
    @Json(name = "event") val event: String? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "certainty") val certainty: String? = null,
    @Json(name = "urgency") val urgency: String? = null,
    @Json(name = "effective") val effective: String? = null,
    @Json(name = "expires") val expires: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "instruction") val instruction: String? = null,
    @Json(name = "areaDesc") val areaDesc: String? = null,
    @Json(name = "senderName") val senderName: String? = null
)

// Open-Meteo Air Quality Models (Copernicus CAMS & NOAA GFS real atmospheric observation models)
@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "generationtime_ms") val generationTimeMs: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: AirQualityCurrentData? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityCurrentData(
    @Json(name = "time") val time: String? = null,
    @Json(name = "interval") val interval: Int? = null,
    @Json(name = "us_aqi") val usAqi: Int? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null
)

