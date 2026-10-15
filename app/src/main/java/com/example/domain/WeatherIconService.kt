package com.example.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Standardized representation of weather visual assets for UI rendering,
 * living weather backgrounds, and interactive widgets.
 */
data class StandardizedWeatherCondition(
    val conditionType: WeatherConditionType,
    val metSymbolCode: String,
    val displayName: String,
    val icon: ImageVector,
    val emoji: String,
    val lottieAssetPath: String,
    val daySkyColors: List<Color>,
    val nightSkyColors: List<Color>,
    val particleType: LivingParticleType
)

enum class LivingParticleType {
    NONE,
    SUNBEAMS,
    STARS,
    CLOUDS,
    RAIN_LIGHT,
    RAIN_HEAVY,
    THUNDER_BOLTS,
    SNOW_DRIFT,
    MIST_FOG,
    WIND_GUSTS
}

/**
 * Utility service that maps MET Norway condition strings (symbol codes) to a standardized
 * set of weather icons, animated Lottie files, emojis, and styling for the 'Living Weather' interface.
 */
object WeatherIconService {

    private val CLEAR_DAY_SKY = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFFBBF24))
    private val CLEAR_NIGHT_SKY = listOf(Color(0xFF070B19), Color(0xFF111D38), Color(0xFF1E3A8A))
    private val CLOUDY_DAY_SKY = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8))
    private val CLOUDY_NIGHT_SKY = listOf(Color(0xFF0A0F24), Color(0xFF152244), Color(0xFF2E3B5C))
    private val RAIN_DAY_SKY = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF38BDF8))
    private val RAIN_NIGHT_SKY = listOf(Color(0xFF090D1A), Color(0xFF16233B), Color(0xFF1D3557))
    private val THUNDER_DAY_SKY = listOf(Color(0xFF181829), Color(0xFF2E2344), Color(0xFF818CF8))
    private val THUNDER_NIGHT_SKY = listOf(Color(0xFF0D0B1C), Color(0xFF1D1638), Color(0xFF4C1D95))
    private val SNOW_DAY_SKY = listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFFE0F2FE))
    private val SNOW_NIGHT_SKY = listOf(Color(0xFF0E1726), Color(0xFF1E293B), Color(0xFF38BDF8))
    private val FOG_SKY = listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFFCBD5E1))

    /**
     * Maps any MET Norway symbol code to a [StandardizedWeatherCondition].
     * Handles suffixes (_day, _night, _polartwilight) and compound descriptions.
     */
    fun mapCondition(symbolCode: String?): StandardizedWeatherCondition {
        val code = symbolCode?.lowercase()?.trim() ?: "clearsky_day"
        val isNight = code.endsWith("_night")
        val baseCode = code.removeSuffix("_day").removeSuffix("_night").removeSuffix("_polartwilight")

        return when {
            baseCode.contains("thunder") -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.THUNDERSTORM,
                metSymbolCode = code,
                displayName = "Thunderstorm",
                icon = Icons.Filled.Thunderstorm,
                emoji = "⛈️",
                lottieAssetPath = "lottie/weather_thunderstorm.json",
                daySkyColors = THUNDER_DAY_SKY,
                nightSkyColors = THUNDER_NIGHT_SKY,
                particleType = LivingParticleType.THUNDER_BOLTS
            )
            baseCode.contains("snow") -> {
                val isHeavy = baseCode.contains("heavy")
                StandardizedWeatherCondition(
                    conditionType = WeatherConditionType.SNOW,
                    metSymbolCode = code,
                    displayName = if (isHeavy) "Heavy Snow" else "Snow",
                    icon = Icons.Filled.AcUnit,
                    emoji = "❄️",
                    lottieAssetPath = "lottie/weather_snow.json",
                    daySkyColors = SNOW_DAY_SKY,
                    nightSkyColors = SNOW_NIGHT_SKY,
                    particleType = LivingParticleType.SNOW_DRIFT
                )
            }
            baseCode.contains("sleet") -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.SLEET,
                metSymbolCode = code,
                displayName = "Sleet",
                icon = Icons.Filled.Grain,
                emoji = "🌨️",
                lottieAssetPath = "lottie/weather_sleet.json",
                daySkyColors = SNOW_DAY_SKY,
                nightSkyColors = SNOW_NIGHT_SKY,
                particleType = LivingParticleType.SNOW_DRIFT
            )
            baseCode.contains("heavyrain") -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.HEAVY_RAIN,
                metSymbolCode = code,
                displayName = "Heavy Rain",
                icon = Icons.Filled.WaterDrop,
                emoji = "🌧️",
                lottieAssetPath = "lottie/weather_rain_heavy.json",
                daySkyColors = RAIN_DAY_SKY,
                nightSkyColors = RAIN_NIGHT_SKY,
                particleType = LivingParticleType.RAIN_HEAVY
            )
            baseCode.contains("rain") -> {
                val isShowers = baseCode.contains("shower")
                StandardizedWeatherCondition(
                    conditionType = WeatherConditionType.LIGHT_RAIN,
                    metSymbolCode = code,
                    displayName = if (isShowers) "Rain Showers" else "Light Rain",
                    icon = Icons.Filled.WaterDrop,
                    emoji = if (isNight) "🌧️" else "🌦️",
                    lottieAssetPath = "lottie/weather_rain_light.json",
                    daySkyColors = RAIN_DAY_SKY,
                    nightSkyColors = RAIN_NIGHT_SKY,
                    particleType = LivingParticleType.RAIN_LIGHT
                )
            }
            baseCode.contains("fog") -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.FOG,
                metSymbolCode = code,
                displayName = "Fog",
                icon = Icons.Filled.CloudQueue,
                emoji = "🌫️",
                lottieAssetPath = "lottie/weather_fog.json",
                daySkyColors = FOG_SKY,
                nightSkyColors = FOG_SKY,
                particleType = LivingParticleType.MIST_FOG
            )
            baseCode == "cloudy" -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.CLOUDY,
                metSymbolCode = code,
                displayName = "Overcast Cloudy",
                icon = Icons.Filled.Cloud,
                emoji = "☁️",
                lottieAssetPath = "lottie/weather_cloudy.json",
                daySkyColors = CLOUDY_DAY_SKY,
                nightSkyColors = CLOUDY_NIGHT_SKY,
                particleType = LivingParticleType.CLOUDS
            )
            baseCode.contains("partlycloudy") || baseCode.contains("fair") -> {
                val condType = if (isNight) WeatherConditionType.PARTLY_CLOUDY_NIGHT else WeatherConditionType.PARTLY_CLOUDY_DAY
                StandardizedWeatherCondition(
                    conditionType = condType,
                    metSymbolCode = code,
                    displayName = if (baseCode.contains("fair")) "Fair" else "Partly Cloudy",
                    icon = if (isNight) Icons.Filled.NightsStay else Icons.Filled.WbCloudy,
                    emoji = if (isNight) "☁️" else "⛅",
                    lottieAssetPath = if (isNight) "lottie/weather_partly_cloudy_night.json" else "lottie/weather_partly_cloudy_day.json",
                    daySkyColors = listOf(Color(0xFF0369A1), Color(0xFF60A5FA), Color(0xFFFDE68A)),
                    nightSkyColors = CLOUDY_NIGHT_SKY,
                    particleType = LivingParticleType.CLOUDS
                )
            }
            baseCode.contains("clearsky") -> {
                val condType = if (isNight) WeatherConditionType.CLEAR_NIGHT else WeatherConditionType.CLEAR_DAY
                StandardizedWeatherCondition(
                    conditionType = condType,
                    metSymbolCode = code,
                    displayName = if (isNight) "Clear Night" else "Clear Sky",
                    icon = if (isNight) Icons.Filled.NightsStay else Icons.Filled.WbSunny,
                    emoji = if (isNight) "🌙" else "☀️",
                    lottieAssetPath = if (isNight) "lottie/weather_clear_night.json" else "lottie/weather_clear_day.json",
                    daySkyColors = CLEAR_DAY_SKY,
                    nightSkyColors = CLEAR_NIGHT_SKY,
                    particleType = if (isNight) LivingParticleType.STARS else LivingParticleType.SUNBEAMS
                )
            }
            baseCode.contains("wind") -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.WINDY,
                metSymbolCode = code,
                displayName = "Windy",
                icon = Icons.Filled.Air,
                emoji = "💨",
                lottieAssetPath = "lottie/weather_windy.json",
                daySkyColors = CLEAR_DAY_SKY,
                nightSkyColors = CLEAR_NIGHT_SKY,
                particleType = LivingParticleType.WIND_GUSTS
            )
            else -> StandardizedWeatherCondition(
                conditionType = WeatherConditionType.CLEAR_DAY,
                metSymbolCode = code,
                displayName = "Fair Weather",
                icon = Icons.Filled.WbSunny,
                emoji = "☀️",
                lottieAssetPath = "lottie/weather_clear_day.json",
                daySkyColors = CLEAR_DAY_SKY,
                nightSkyColors = CLEAR_NIGHT_SKY,
                particleType = LivingParticleType.SUNBEAMS
            )
        }
    }

    /**
     * Map a WeatherConditionType enum and daylight status to a StandardizedWeatherCondition.
     */
    fun mapConditionType(type: WeatherConditionType, isDaytime: Boolean = true): StandardizedWeatherCondition {
        val symbolCode = when (type) {
            WeatherConditionType.CLEAR_DAY -> "clearsky_day"
            WeatherConditionType.CLEAR_NIGHT -> "clearsky_night"
            WeatherConditionType.PARTLY_CLOUDY_DAY -> "partlycloudy_day"
            WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "partlycloudy_night"
            WeatherConditionType.CLOUDY -> "cloudy"
            WeatherConditionType.LIGHT_RAIN -> if (isDaytime) "lightrainshowers_day" else "lightrainshowers_night"
            WeatherConditionType.HEAVY_RAIN -> "heavyrain"
            WeatherConditionType.THUNDERSTORM -> "thunderstorm"
            WeatherConditionType.SNOW -> if (isDaytime) "snowshowers_day" else "snowshowers_night"
            WeatherConditionType.SLEET -> "sleet"
            WeatherConditionType.FOG -> "fog"
            WeatherConditionType.WINDY -> "wind"
        }
        return mapCondition(symbolCode)
    }

    /**
     * Returns the standardized Lottie animation file path for Living Weather
     */
    fun getLottieAnimationPath(symbolCode: String?): String {
        return mapCondition(symbolCode).lottieAssetPath
    }

    /**
     * Returns the standardized Material ImageVector icon for a MET Norway symbol code
     */
    fun getStandardizedIcon(symbolCode: String?): ImageVector {
        return mapCondition(symbolCode).icon
    }

    /**
     * Alias for getStandardizedIcon - converts MET Norway weather symbol strings into Material design icons
     */
    fun getMaterialIcon(symbolCode: String?): ImageVector {
        return getStandardizedIcon(symbolCode)
    }

    /**
     * Converts a domain WeatherConditionType to a Material design icon
     */
    fun getMaterialIcon(conditionType: WeatherConditionType, isDaytime: Boolean = true): ImageVector {
        return mapConditionType(conditionType, isDaytime).icon
    }

    /**
     * Returns an optimal Material Design accent tint color for the weather symbol
     */
    fun getIconTint(symbolCode: String?): Color {
        val condition = mapCondition(symbolCode)
        return when (condition.conditionType) {
            WeatherConditionType.CLEAR_DAY -> Color(0xFFF59E0B) // Amber Gold Sun
            WeatherConditionType.CLEAR_NIGHT -> Color(0xFF818CF8) // Moon Indigo
            WeatherConditionType.PARTLY_CLOUDY_DAY -> Color(0xFFFDE047) // Warm Sunlight Behind Cloud
            WeatherConditionType.PARTLY_CLOUDY_NIGHT -> Color(0xFFA5B4FC) // Soft Night Sky
            WeatherConditionType.CLOUDY -> Color(0xFFCBD5E1) // Silver Cloud
            WeatherConditionType.LIGHT_RAIN -> Color(0xFF38BDF8) // Vibrant Sky Rain
            WeatherConditionType.HEAVY_RAIN -> Color(0xFF0284C7) // Deep Blue Rain
            WeatherConditionType.THUNDERSTORM -> Color(0xFFFBBF24) // Thunder Lightning
            WeatherConditionType.SNOW -> Color(0xFFE0F2FE) // Crisp Snow White/Blue
            WeatherConditionType.SLEET -> Color(0xFF7DD3FC) // Ice Cyan
            WeatherConditionType.FOG -> Color(0xFF94A3B8) // Fog Slate
            WeatherConditionType.WINDY -> Color(0xFF38BDF8) // Wind Stream Blue
        }
    }

    /**
     * Returns the display emoji
     */
    fun getEmoji(symbolCode: String?): String {
        return mapCondition(symbolCode).emoji
    }
}

/**
 * Reusable Composable that converts MET Norway weather symbol strings into standardized Material design icons
 */
@androidx.compose.runtime.Composable
fun WeatherSymbolIcon(
    symbolCode: String?,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    tint: Color? = null,
    contentDescription: String? = null
) {
    val condition = WeatherIconService.mapCondition(symbolCode)
    val effectiveTint = tint ?: WeatherIconService.getIconTint(symbolCode)
    androidx.compose.material3.Icon(
        imageVector = condition.icon,
        contentDescription = contentDescription ?: condition.displayName,
        tint = effectiveTint,
        modifier = modifier
    )
}

/**
 * Typealias ensuring the mapping utility is accessible under WeatherIconMapper as well.
 */
typealias WeatherIconMapper = WeatherIconService
