package com.example.domain

enum class WeatherConditionType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    LIGHT_RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    FOG,
    SLEET,
    WINDY
}

data class CurrentWeather(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val minTempTodayC: Double,
    val maxTempTodayC: Double,
    val conditionType: WeatherConditionType,
    val conditionLabel: String,
    val symbolCode: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Double,
    val windCardinal: String,
    val cloudCoverPercent: Int,
    val uvIndex: Double,
    val airPressureHpa: Double,
    val dewPointC: Double,
    val nextChangeDescription: String
)

data class HourlyPoint(
    val timeIso: String,
    val hourLabel: String,            // e.g. "14:00" or "2 PM"
    val temperatureC: Double,
    val conditionType: WeatherConditionType,
    val symbolCode: String,
    val precipProbability: Int,       // 0 - 100%
    val precipAmountMm: Double,       // mm
    val windSpeedKmh: Double,
    val windDirectionDeg: Double,
    val windCardinal: String,
    val humidityPercent: Int,
    val isDaylight: Boolean = true
)

data class DailyPoint(
    val dateIso: String,
    val dayLabel: String,             // e.g. "Today", "Wed", "Thu"
    val dateDisplay: String,          // e.g. "Oct 14"
    val minTempC: Double,
    val maxTempC: Double,
    val conditionType: WeatherConditionType,
    val symbolCode: String,
    val conditionLabel: String,
    val precipProbability: Int,
    val precipTotalMm: Double
)

data class RainWindow(
    val hasRain: Boolean,
    val startHour: String,
    val endHour: String,
    val peakProbability: Int,
    val totalEstimatedMm: Double,
    val confidenceRating: String,     // e.g. "High Confidence", "Moderate Uncertainty"
    val summaryHeadline: String,      // e.g. "Rain expected from 14:00 to 17:00"
    val detailedReasoning: String,    // "Model indicates consistent 70%+ precipitation probability."
    val modelLastUpdatedAt: String
)

enum class ActivityType(val displayName: String, val iconName: String) {
    WALKING("Walking", "walk"),
    RUNNING("Running", "run"),
    CYCLING("Cycling", "bike"),
    DOG_WALKING("Dog Walking", "dog"),
    OUTDOOR_LAUNDRY("Outdoor Laundry", "laundry"),
    COMMUTE("Commute", "commute"),
    GARDENING("Gardening", "garden"),
    PHOTOGRAPHY("Outdoor Photography", "photo")
}

data class ActivityFactor(
    val isPositive: Boolean,
    val text: String
)

data class ActivityRecommendation(
    val activity: ActivityType,
    val scorePercent: Int,            // 0 - 100
    val ratingLabel: String,          // "Optimal", "Good", "Moderate", "Unfavorable"
    val bestWindowLabel: String,      // e.g. "07:00 – 10:00"
    val currentSuitabilityText: String,
    val factors: List<ActivityFactor>
)

enum class AlertSeverity {
    WARNING,
    WATCH,
    ADVISORY
}

data class SevereAlert(
    val id: String,
    val event: String,
    val headline: String,
    val severity: AlertSeverity,
    val area: String,
    val effectiveTime: String,
    val expiresTime: String,
    val instruction: String,
    val sourceAgency: String,
    val isDismissed: Boolean = false
)

enum class FreshnessStatus {
    LIVE,
    CACHED,
    REFRESHING,
    STALE
}

data class DataFreshness(
    val status: FreshnessStatus,
    val lastFetchedEpochMs: Long,
    val providerUpdatedAtIso: String,
    val providerName: String = "MET Norway",
    val licenseNotice: String = "CC BY 4.0",
    val coordinatePrecisionText: String = "Coordinates rounded to 4 decimals (lat/lon) per provider policy",
    val latencyOrAgeText: String = "Updated recently"
)

val DEFAULT_HOME_CARDS = listOf(
    "radar",
    "air_health",
    "time_travel",
    "rain_window",
    "wardrobe",
    "hourly",
    "activity",
    "sun_moon",
    "wind_pressure",
    "barometer",
    "metrics",
    "daily"
)

data class UserSettings(
    val tempUnit: String = "C",
    val windUnit: String = "kmh",
    val precipUnit: String = "mm",
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val idealTempMin: Float = 16f,
    val idealTempMax: Float = 24f,
    val maxWindTolerance: Float = 25f,
    val maxRainTolerance: Float = 15f,
    val themeMode: String = "auto", // "auto", "day", "night", "system"
    val cardOrder: List<String> = DEFAULT_HOME_CARDS,
    val hiddenCards: Set<String> = emptySet(),
    val cardStyle: String = "atmospheric", // "atmospheric", "compact", "oled"
    val isOnboardingCompleted: Boolean = false,
    val useGpsLocation: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyBriefingEnabled: Boolean = true,
    val rainAlertsEnabled: Boolean = true,
    val severeAlertsEnabled: Boolean = true
)

data class AirQualityData(
    val aqi: Int,
    val category: String,
    val categoryColorHex: Long,
    val primaryPollutant: String,
    val pm25: Double,
    val pm10: Double,
    val o3: Double,
    val no2: Double,
    val co: Double,
    val healthAdvisory: String,
    val sensitiveGroupsAdvisory: String,
    val outdoorExerciseScore: Int
)

data class AstronomyData(
    val sunriseTime: String,
    val sunsetTime: String,
    val solarNoonTime: String,
    val goldenHourMorning: String,
    val goldenHourEvening: String,
    val blueHourEvening: String,
    val dayLengthHours: Int,
    val dayLengthMinutes: Int,
    val sunProgressFraction: Float,
    val isSunUp: Boolean,
    val moonPhaseName: String,
    val moonPhaseEmoji: String,
    val moonIlluminationPercent: Int,
    val nextFullMoonDays: Int
)

data class WardrobeRecommendation(
    val headline: String,
    val summary: String,
    val topLayer: String,
    val bottomLayer: String,
    val footwear: String,
    val accessories: List<String>,
    val umbrellaNeeded: Boolean,
    val umbrellaProbability: Int,
    val sunProtectionNeeded: Boolean
)

data class WindDynamics(
    val beaufortScale: Int,
    val beaufortDescription: String,
    val gustSpeedKmh: Double,
    val droneFlyingSafety: String,
    val cyclingImpact: String
)

data class BarometerTrend(
    val pressureHpa: Double,
    val change3hHpa: Double,
    val trendLabel: String,
    val shortTermImplication: String
)
