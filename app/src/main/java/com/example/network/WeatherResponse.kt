package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Root GeoJSON response from MET Norway Locationforecast 2.0 API.
 * Endpoint: /weatherapi/locationforecast/2.0/compact (or /complete)
 */
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "type") val type: String? = "Feature",
    @Json(name = "geometry") val geometry: PointGeometry? = null,
    @Json(name = "properties") val properties: ForecastProperties? = null
)

// Convenient type aliases for alternative naming conventions
typealias LocationForecastResponse = WeatherResponse
typealias MetWeatherResponse = WeatherResponse
typealias ForecastResponse = WeatherResponse

@JsonClass(generateAdapter = true)
data class PointGeometry(
    @Json(name = "type") val type: String? = "Point",
    @Json(name = "coordinates") val coordinates: List<Double> = emptyList()
) {
    val longitude: Double? get() = coordinates.getOrNull(0)
    val latitude: Double? get() = coordinates.getOrNull(1)
    val altitude: Double? get() = coordinates.getOrNull(2)
}

typealias Geometry = PointGeometry

@JsonClass(generateAdapter = true)
data class ForecastProperties(
    @Json(name = "meta") val meta: ForecastMeta? = null,
    @Json(name = "timeseries") val timeseries: List<TimeSeriesItem> = emptyList()
) {
    val metaData: ForecastMeta? get() = meta
    val timeSeries: List<TimeSeriesItem> get() = timeseries
}

typealias WeatherProperties = ForecastProperties
typealias Properties = ForecastProperties
typealias MetProperties = ForecastProperties

@JsonClass(generateAdapter = true)
data class ForecastMeta(
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "units") val units: Map<String, String>? = null
) {
    val updated: String? get() = updatedAt
}

typealias Meta = ForecastMeta
typealias MetMeta = ForecastMeta

@JsonClass(generateAdapter = true)
data class TimeSeriesItem(
    @Json(name = "time") val time: String = "",
    @Json(name = "data") val data: TimeSeriesData = TimeSeriesData()
) {
    val timestampIso: String get() = time
}

typealias TimeSeries = TimeSeriesItem
typealias MetTimeSeriesItem = TimeSeriesItem

@JsonClass(generateAdapter = true)
data class TimeSeriesData(
    @Json(name = "instant") val instant: InstantData? = null,
    @Json(name = "next_1_hours") val next1Hours: PeriodForecast? = null,
    @Json(name = "next_6_hours") val next6Hours: PeriodForecast? = null,
    @Json(name = "next_12_hours") val next12Hours: PeriodForecast? = null
) {
    val nextOneHours: PeriodForecast? get() = next1Hours
    val next1Hour: PeriodForecast? get() = next1Hours
    val nextSixHours: PeriodForecast? get() = next6Hours
    val next6Hour: PeriodForecast? get() = next6Hours
    val nextTwelveHours: PeriodForecast? get() = next12Hours
    val next12Hour: PeriodForecast? get() = next12Hours
}

typealias MetTimeSeriesData = TimeSeriesData

@JsonClass(generateAdapter = true)
data class InstantData(
    @Json(name = "details") val details: InstantDetails? = null
)

typealias MetInstantData = InstantData

@JsonClass(generateAdapter = true)
data class InstantDetails(
    @Json(name = "air_temperature") val airTemperature: Double? = null,
    @Json(name = "dew_point_temperature") val dewPointTemperature: Double? = null,
    @Json(name = "relative_humidity") val relativeHumidity: Double? = null,
    @Json(name = "wind_speed") val windSpeed: Double? = null,
    @Json(name = "wind_from_direction") val windFromDirection: Double? = null,
    @Json(name = "wind_speed_of_gust") val windSpeedOfGust: Double? = null,
    @Json(name = "cloud_area_fraction") val cloudAreaFraction: Double? = null,
    @Json(name = "cloud_area_fraction_high") val cloudAreaFractionHigh: Double? = null,
    @Json(name = "cloud_area_fraction_medium") val cloudAreaFractionMedium: Double? = null,
    @Json(name = "cloud_area_fraction_low") val cloudAreaFractionLow: Double? = null,
    @Json(name = "fog_area_fraction") val fogAreaFraction: Double? = null,
    @Json(name = "air_pressure_at_sea_level") val airPressureAtSeaLevel: Double? = null,
    @Json(name = "ultraviolet_index_clear_sky") val uvIndex: Double? = null
) {
    val temperature: Double? get() = airTemperature
    val temp: Double? get() = airTemperature
    val dewPoint: Double? get() = dewPointTemperature
    val humidity: Double? get() = relativeHumidity
    val wind: Double? get() = windSpeed
    val windDirection: Double? get() = windFromDirection
    val cloudCover: Double? get() = cloudAreaFraction
    val pressure: Double? get() = airPressureAtSeaLevel
}

typealias MetInstantDetails = InstantDetails

@JsonClass(generateAdapter = true)
data class PeriodForecast(
    @Json(name = "summary") val summary: PeriodSummary? = null,
    @Json(name = "details") val details: PeriodDetails? = null
)

typealias MetPeriodForecast = PeriodForecast

@JsonClass(generateAdapter = true)
data class PeriodSummary(
    @Json(name = "symbol_code") val symbolCode: String? = null
) {
    val symbol: String? get() = symbolCode
}

typealias MetPeriodSummary = PeriodSummary

@JsonClass(generateAdapter = true)
data class PeriodDetails(
    @Json(name = "precipitation_amount") val precipitationAmount: Double? = null,
    @Json(name = "precipitation_amount_max") val precipitationAmountMax: Double? = null,
    @Json(name = "precipitation_amount_min") val precipitationAmountMin: Double? = null,
    @Json(name = "probability_of_precipitation") val probabilityOfPrecipitation: Double? = null,
    @Json(name = "air_temperature_max") val airTemperatureMax: Double? = null,
    @Json(name = "air_temperature_min") val airTemperatureMin: Double? = null
) {
    val precipitation: Double? get() = precipitationAmount
    val precipAmount: Double? get() = precipitationAmount
    val precipitationProbability: Double? get() = probabilityOfPrecipitation
    val precipProbability: Double? get() = probabilityOfPrecipitation
}

typealias MetPeriodDetails = PeriodDetails
