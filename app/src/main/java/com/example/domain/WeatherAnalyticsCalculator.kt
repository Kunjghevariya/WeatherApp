package com.example.domain

import com.example.data.model.AirQualityCurrentData
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * High-accuracy astronomical, meteorological, and lifestyle analytics calculator.
 * Evaluates verified Air Quality Index from atmospheric chemistry models,
 * computes Astronomical Progression (Sunrise, Sunset, Moon Phases, Golden Hour),
 * Wardrobe Guidance, Wind Dynamics (Beaufort scale, Gusts, Flight Safety), and Barometric Trends.
 */
object WeatherAnalyticsCalculator {

    /**
     * Evaluates official air quality readings from verified atmospheric chemistry models
     * (Copernicus Atmosphere Monitoring Service CAMS / NOAA GFS via Open-Meteo).
     * Does NOT fabricate pollution measurements from temperature or wind heuristics.
     */
    fun evaluateAirQuality(
        obs: AirQualityCurrentData?,
        dataSource: String = "Open-Meteo Air Quality (Copernicus CAMS / NOAA GFS)"
    ): AirQualityData {
        if (obs == null || obs.usAqi == null) {
            return AirQualityData.unavailable(
                message = "Verified station or model air quality observations are currently unavailable for this region.",
                source = dataSource
            )
        }

        val aqi = obs.usAqi.coerceIn(0, 500)
        val pm25 = obs.pm25
        val pm10 = obs.pm10
        val o3 = obs.ozone
        val no2 = obs.nitrogenDioxide
        val co = obs.carbonMonoxide

        // Determine primary pollutant based on elevated thresholds
        val primary = when {
            pm25 != null && pm25 >= 35.0 -> "Fine Particles (PM2.5)"
            o3 != null && o3 >= 70.0 -> "Ozone (O₃)"
            pm10 != null && pm10 >= 50.0 -> "Coarse Dust (PM10)"
            no2 != null && no2 >= 40.0 -> "Nitrogen Dioxide (NO₂)"
            else -> if (pm25 != null) "Fine Particles (PM2.5)" else "Ozone (O₃)"
        }

        val (category, colorHex, advisory, sensitiveAdvisory) = when {
            aqi <= 50 -> Quadruple(
                "Good",
                0xFF10B981, // Emerald
                "Air quality is satisfactory and poses little or no risk.",
                "Air quality is ideal for all outdoor activities and exercise."
            )
            aqi <= 100 -> Quadruple(
                "Moderate",
                0xFFF59E0B, // Amber
                "Air quality is acceptable. A few unusually sensitive individuals may notice mild irritation.",
                "Unusually sensitive individuals should consider taking breaks during prolonged outdoor exertion."
            )
            aqi <= 150 -> Quadruple(
                "Unhealthy for Sensitive Groups",
                0xFFF97316, // Orange
                "Members of sensitive groups may experience health effects. The general public is less likely to be affected.",
                "People with asthma, respiratory illness, or heart conditions should limit prolonged outdoor exertion."
            )
            aqi <= 200 -> Quadruple(
                "Unhealthy",
                0xFFEF4444, // Red
                "Everyone may begin to experience health effects; members of sensitive groups may experience more serious health effects.",
                "Sensitive groups should avoid prolonged outdoor exposure. Others should reduce heavy exertion outdoors."
            )
            aqi <= 300 -> Quadruple(
                "Very Unhealthy",
                0xFF8B5CF6, // Purple
                "Health alert: the risk of health effects is increased for everyone.",
                "Active children, older adults, and individuals with respiratory issues should avoid all outdoor exertion."
            )
            else -> Quadruple(
                "Hazardous",
                0xFF7F1D1D, // Maroon / Dark Red
                "Emergency health warnings: the entire population is likely to be affected.",
                "Everyone should remain indoors with clean air filtration and avoid physical activity outside."
            )
        }

        val outdoorExerciseScore = (100 - (aqi * 0.65)).roundToInt().coerceIn(10, 100)

        return AirQualityData(
            isAvailable = true,
            aqi = aqi,
            category = category,
            categoryColorHex = colorHex,
            primaryPollutant = primary,
            pm25 = pm25,
            pm10 = pm10,
            o3 = o3,
            no2 = no2,
            co = co,
            healthAdvisory = advisory,
            sensitiveGroupsAdvisory = sensitiveAdvisory,
            outdoorExerciseScore = outdoorExerciseScore,
            dataSource = dataSource,
            updatedAtIso = obs.time ?: ""
        )
    }

    /**
     * Preserved for backwards compatibility with legacy callers.
     * Always returns an honest unavailable state rather than fabricating particulate numbers.
     */
    @Deprecated("Do not estimate air quality from weather variables. Use evaluateAirQuality with real atmospheric observation data.")
    fun calculateAirQuality(
        current: CurrentWeather,
        latitude: Double
    ): AirQualityData {
        return AirQualityData.unavailable(
            message = "Weather variables cannot reliably establish local air pollution measurements. Verified observation feeds required.",
            source = "Open-Meteo Air Quality"
        )
    }


    fun calculateAstronomy(
        latitude: Double,
        longitude: Double,
        timezoneId: String
    ): AstronomyData {
        val tz = try { TimeZone.getTimeZone(timezoneId) } catch (_: Exception) { TimeZone.getDefault() }
        val calendar = Calendar.getInstance(tz)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60.0)

        // Solar Declination (degrees)
        val bRad = Math.toRadians((360.0 / 365.0) * (dayOfYear - 81))
        val declinationRad = Math.toRadians(23.45 * sin(bRad))

        // Equation of Time (minutes)
        val eotMinutes = 9.87 * sin(2 * bRad) - 7.53 * cos(bRad) - 1.5 * sin(bRad)

        // Solar Noon in local time hours
        val timeZoneOffsetHours = tz.getOffset(calendar.timeInMillis) / 3600000.0
        val solarNoonHour = 12.0 - (longitude / 15.0) + timeZoneOffsetHours - (eotMinutes / 60.0)

        // Sunrise & Sunset Hour Angle calculation
        val latRad = Math.toRadians(latitude)
        val cosHourAngle = -tan(latRad) * tan(declinationRad)
        val clampedCos = cosHourAngle.coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(acos(clampedCos))
        val halfDayHours = hourAngleDeg / 15.0

        val sunriseHour = (solarNoonHour - halfDayHours).let { if (it < 0) it + 24 else if (it >= 24) it - 24 else it }
        val sunsetHour = (solarNoonHour + halfDayHours).let { if (it < 0) it + 24 else if (it >= 24) it - 24 else it }

        val dayLengthTotalMinutes = (halfDayHours * 2.0 * 60.0).roundToInt().coerceIn(60, 24 * 60)
        val dayLengthHours = dayLengthTotalMinutes / 60
        val dayLengthMinutes = dayLengthTotalMinutes % 60

        // Golden Hour (starts 40 min before sunset or ends 40 min after sunrise)
        val goldenMorningEnd = (sunriseHour + 0.7).coerceIn(0.0, 23.9)
        val goldenEveStart = (sunsetHour - 0.7).coerceIn(0.0, 23.9)
        val blueEveEnd = (sunsetHour + 0.35).coerceIn(0.0, 23.9)

        val isSunUp = currentHourOfDay in sunriseHour..sunsetHour
        val sunProgressFraction = if (isSunUp && halfDayHours > 0) {
            ((currentHourOfDay - sunriseHour) / (sunsetHour - sunriseHour)).toFloat().coerceIn(0.05f, 0.95f)
        } else if (currentHourOfDay < sunriseHour) {
            0.0f
        } else {
            1.0f
        }

        // Moon phase calculation using Synodic Month (29.530588853 days)
        // Reference known New Moon: Jan 11, 2024
        val refNewMoonMillis = 1704975420000L // 2024-01-11 11:57 UTC
        val diffDays = (calendar.timeInMillis - refNewMoonMillis) / (1000.0 * 60 * 60 * 24)
        val cyclePosition = (diffDays % 29.530588853).let { if (it < 0) it + 29.530588853 else it }
        val phaseFraction = cyclePosition / 29.530588853

        val (moonName, moonEmoji) = when {
            phaseFraction < 0.03 || phaseFraction >= 0.97 -> Pair("New Moon", "🌑")
            phaseFraction < 0.22 -> Pair("Waxing Crescent", "🌒")
            phaseFraction < 0.28 -> Pair("First Quarter", "🌓")
            phaseFraction < 0.47 -> Pair("Waxing Gibbous", "🌔")
            phaseFraction < 0.53 -> Pair("Full Moon", "🌕")
            phaseFraction < 0.72 -> Pair("Waning Gibbous", "🌖")
            phaseFraction < 0.78 -> Pair("Last Quarter", "🌗")
            else -> Pair("Waning Crescent", "🌘")
        }

        // Illumination: 0% at New Moon (0.0), 100% at Full Moon (0.5)
        val illumination = ((1 - cos(phaseFraction * 2 * Math.PI)) / 2.0 * 100).roundToInt().coerceIn(0, 100)
        val daysToFull = ((0.5 - phaseFraction).let { if (it < 0) it + 1.0 else it } * 29.53).roundToInt()

        return AstronomyData(
            sunriseTime = formatHourFraction(sunriseHour),
            sunsetTime = formatHourFraction(sunsetHour),
            solarNoonTime = formatHourFraction(solarNoonHour),
            goldenHourMorning = "${formatHourFraction(sunriseHour)} – ${formatHourFraction(goldenMorningEnd)}",
            goldenHourEvening = "${formatHourFraction(goldenEveStart)} – ${formatHourFraction(sunsetHour)}",
            blueHourEvening = "${formatHourFraction(sunsetHour)} – ${formatHourFraction(blueEveEnd)}",
            dayLengthHours = dayLengthHours,
            dayLengthMinutes = dayLengthMinutes,
            sunProgressFraction = sunProgressFraction,
            isSunUp = isSunUp,
            moonPhaseName = moonName,
            moonPhaseEmoji = moonEmoji,
            moonIlluminationPercent = illumination,
            nextFullMoonDays = daysToFull
        )
    }

    fun calculateWardrobe(
        current: CurrentWeather,
        rainWindow: RainWindow,
        hourly: List<HourlyPoint>,
        userSettings: UserSettings
    ): WardrobeRecommendation {
        val feelsLike = current.feelsLikeC
        val maxRainProb = hourly.take(12).maxOfOrNull { it.precipProbability } ?: 0
        val isRainLikely = rainWindow.hasRain || maxRainProb >= 35

        val (headline, top, bottom, shoes) = when {
            feelsLike < 0 -> Quadruple(
                "Heavy Winter Layers & Insulation",
                "Thermal base layer + heavy down parka or wool coat",
                "Thermal pants or fleece-lined denim",
                "Insulated waterproof winter boots"
            )
            feelsLike < 10 -> Quadruple(
                "Warm Layers & Outer Jacket",
                "Warm sweater or fleece + insulated windproof jacket",
                "Full-length trousers or heavy jeans",
                "Sturdy closed leather shoes or boots"
            )
            feelsLike < 16 -> Quadruple(
                "Light Jacket or Cardigan",
                "Long-sleeve shirt + light jacket, windbreaker, or hoodie",
                "Chinos, denim, or comfortable pants",
                "Sneakers or casual walking shoes"
            )
            feelsLike < 23 -> Quadruple(
                "Comfortable Breathable Layers",
                "Cotton t-shirt or polo with optional light overshirt",
                "Light jeans, chinos, or relaxed trousers",
                "Breathable sneakers or loafers"
            )
            feelsLike < 30 -> Quadruple(
                "Warm Weather Attire",
                "Lightweight breathable linen or cotton t-shirt / blouse",
                "Shorts, skirt, or lightweight linen pants",
                "Sandals, boat shoes, or breathable mesh sneakers"
            )
            else -> Quadruple(
                "Ultra-Light Summer Wear",
                "Moisture-wicking, loose-fitting, light-colored clothing",
                "Breezy shorts or linen trousers",
                "Open sandals or light athletic footwear"
            )
        }

        val accessories = mutableListOf<String>()
        if (isRainLikely) {
            accessories.add("Compact Umbrella ($maxRainProb% rain chance)")
            accessories.add("Waterproof outerwear")
        }
        if (current.uvIndex >= 3.0) {
            accessories.add("UV Sunglasses (UV ${String.format(Locale.US, "%.1f", current.uvIndex)})")
            accessories.add("SPF 30+ Sunscreen")
        }
        if (current.windSpeedKmh > 35) {
            accessories.add("Windbreaker shell")
        }
        if (feelsLike < 8) {
            accessories.add("Knit beanie / scarf")
        }
        if (accessories.isEmpty()) {
            accessories.add("Light everyday essentials")
        }

        val summary = buildString {
            append("Feels like ${if (userSettings.tempUnit == "F") "${((feelsLike * 9 / 5) + 32).toInt()}°F" else "${feelsLike.toInt()}°C"}. ")
            if (isRainLikely) {
                append("Showers expected today; keep rain gear handy. ")
            } else {
                append("No significant rain expected. ")
            }
            if (current.uvIndex >= 4.0) {
                append("Moderate to high UV; sun protection recommended.")
            }
        }

        return WardrobeRecommendation(
            headline = headline,
            summary = summary,
            topLayer = top,
            bottomLayer = bottom,
            footwear = shoes,
            accessories = accessories,
            umbrellaNeeded = isRainLikely,
            umbrellaProbability = maxRainProb,
            sunProtectionNeeded = current.uvIndex >= 3.0
        )
    }

    fun calculateWindDynamics(current: CurrentWeather, hourly: List<HourlyPoint>): WindDynamics {
        val speedKmh = current.windSpeedKmh
        val gustSpeed = (speedKmh * 1.4).roundToInt().toDouble()

        val (scale, description) = when {
            speedKmh < 1 -> Pair(0, "Calm (smoke rises vertically)")
            speedKmh < 6 -> Pair(1, "Light Air (direction shown by smoke drift)")
            speedKmh < 12 -> Pair(2, "Light Breeze (wind felt on exposed skin, leaves rustle)")
            speedKmh < 20 -> Pair(3, "Gentle Breeze (leaves and twigs in constant motion)")
            speedKmh < 29 -> Pair(4, "Moderate Breeze (small branches move, dust raised)")
            speedKmh < 39 -> Pair(5, "Fresh Breeze (small trees begin to sway)")
            speedKmh < 50 -> Pair(6, "Strong Breeze (large branches move, umbrella difficult)")
            speedKmh < 62 -> Pair(7, "High Wind (near gale, whole trees in motion)")
            speedKmh < 75 -> Pair(8, "Gale (twigs break off trees, progress impeded)")
            speedKmh < 89 -> Pair(9, "Strong Gale (slight structural damage possible)")
            else -> Pair(10, "Storm (trees uprooted, considerable damage)")
        }

        val droneFlightSafety = when {
            speedKmh < 20 -> "Optimal for all drones"
            speedKmh < 32 -> "Caution: Moderate drift for sub-250g drones"
            else -> "Hazardous: Ground all commercial drones"
        }

        val cyclingImpact = when {
            speedKmh < 10 -> "Minimal aero penalty (~3% effort)"
            speedKmh < 25 -> "Moderate resistance on headwinds (~15% effort)"
            else -> "Heavy headwind penalty (~35% effort); watch crosswind gusts"
        }

        return WindDynamics(
            beaufortScale = scale,
            beaufortDescription = description,
            gustSpeedKmh = gustSpeed,
            droneFlyingSafety = droneFlightSafety,
            cyclingImpact = cyclingImpact
        )
    }

    fun calculateBarometerTrend(current: CurrentWeather, hourly: List<HourlyPoint>): BarometerTrend {
        // Compute 3-hour pressure change from numerical forecast model
        val futurePoint = hourly.getOrNull(3)
        val delta = if (futurePoint?.pressureHpa != null && current.airPressureHpa > 0) {
            futurePoint.pressureHpa - current.airPressureHpa
        } else {
            0.0
        }

        val roundedDelta = (delta * 10).roundToInt() / 10.0

        val (label, implication) = when {
            roundedDelta < -2.0 -> Pair(
                "Falling Rapidly",
                "Incoming low-pressure front. Gusty winds and sudden precipitation expected within 2 to 4 hours."
            )
            roundedDelta < -0.5 -> Pair(
                "Falling Slowly",
                "Gradual pressure drop. Cloud cover likely increasing with unsettled conditions."
            )
            roundedDelta > 2.0 -> Pair(
                "Rising Rapidly",
                "Strong high-pressure ridge building. Clearing skies with brisk, refreshing winds."
            )
            roundedDelta > 0.5 -> Pair(
                "Rising Slowly",
                "Improving atmospheric stability. Expect fair weather and lighter winds."
            )
            else -> Pair(
                "Steady",
                "Barometer is stable (±0.5 hPa). Expect persistent conditions over the next 6 hours."
            )
        }

        return BarometerTrend(
            pressureHpa = current.airPressureHpa,
            change3hHpa = roundedDelta,
            trendLabel = label,
            shortTermImplication = implication
        )
    }

    private fun formatHourFraction(hourFraction: Double): String {
        val totalMinutes = (hourFraction * 60).roundToInt().let { if (it < 0) it + 1440 else if (it >= 1440) it - 1440 else it }
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
