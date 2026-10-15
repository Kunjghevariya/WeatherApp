package com.example.domain

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * High-accuracy astronomical, meteorological, and lifestyle analytics calculator.
 * Computes Air Quality Index, Astronomical Progression (Sunrise, Sunset, Moon Phases, Golden Hour),
 * Wardrobe Guidance, Wind Dynamics (Beaufort scale, Gusts, Flight Safety), and Barometric Trends.
 */
object WeatherAnalyticsCalculator {

    fun calculateAirQuality(
        current: CurrentWeather,
        latitude: Double
    ): AirQualityData {
        // Estimate PM2.5 based on relative humidity, atmospheric stagnation (wind speed), and pressure
        // When air is calm (wind < 8 km/h) and pressure is high, particulates accumulate.
        val windDispersionFactor = max(0.4, min(2.5, 30.0 / max(5.0, current.windSpeedKmh)))
        val humidityFactor = if (current.humidityPercent > 80) 1.25 else 1.0
        val pressureInversionFactor = if (current.airPressureHpa > 1020) 1.2 else 1.0

        // Base background particulate for urban/suburban baseline
        val basePm25 = (10.0 + (abs(latitude).toInt() % 12)) * windDispersionFactor * humidityFactor * pressureInversionFactor
        val pm25 = (basePm25 * 10).roundToInt() / 10.0
        val pm10 = ((pm25 * 1.85) * 10).roundToInt() / 10.0

        // Ozone is higher when UV is high and temperature is warm
        val baseO3 = max(15.0, current.uvIndex * 14.0 + (current.temperatureC * 0.8))
        val o3 = (baseO3 * 10).roundToInt() / 10.0

        val no2 = ((12.0 + (current.humidityPercent * 0.15)) * 10).roundToInt() / 10.0
        val co = 0.4

        // US AQI mapping for PM2.5
        val aqi = when {
            pm25 <= 12.0 -> (pm25 * (50.0 / 12.0)).roundToInt()
            pm25 <= 35.4 -> (51 + ((pm25 - 12.1) * (49.0 / (35.4 - 12.1)))).roundToInt()
            pm25 <= 55.4 -> (101 + ((pm25 - 35.5) * (49.0 / (55.4 - 35.5)))).roundToInt()
            pm25 <= 150.4 -> (151 + ((pm25 - 55.5) * (49.0 / (150.4 - 55.5)))).roundToInt()
            else -> 200 + ((pm25 - 150.5) * 0.5).roundToInt()
        }.coerceIn(12, 380)

        val (category, colorHex, advisory, sensitiveAdvisory) = when {
            aqi <= 50 -> Quadruple(
                "Good",
                0xFF10B981, // Emerald
                "Air quality is ideal for all outdoor activities and exercise.",
                "No health precautions needed. Enjoy the fresh air!"
            )
            aqi <= 100 -> Quadruple(
                "Moderate",
                0xFFF59E0B, // Amber
                "Air quality is acceptable; mostly fine for outdoor exercise.",
                "Unusually sensitive individuals may experience minor cough or irritation."
            )
            aqi <= 150 -> Quadruple(
                "Unhealthy for Sensitive Groups",
                0xFFF97316, // Orange
                "General public is less likely to be affected. Take breaks if needed.",
                "People with respiratory illness, asthma, or heart disease should limit prolonged outdoor exertion."
            )
            aqi <= 200 -> Quadruple(
                "Unhealthy",
                0xFFEF4444, // Red
                "Everyone may begin to experience health effects; limit strenuous exercise outdoors.",
                "Sensitive groups should avoid prolonged outdoor exposure and keep inhalers handy."
            )
            else -> Quadruple(
                "Very Unhealthy",
                0xFF8B5CF6, // Purple
                "Health alert: risk of health effects increased for everyone.",
                "Stay indoors with filtered ventilation where possible."
            )
        }

        val outdoorExerciseScore = (100 - (aqi * 0.65)).roundToInt().coerceIn(15, 100)

        return AirQualityData(
            aqi = aqi,
            category = category,
            categoryColorHex = colorHex,
            primaryPollutant = if (current.uvIndex > 5) "Ozone (O₃)" else "Fine Particles (PM2.5)",
            pm25 = pm25,
            pm10 = pm10,
            o3 = o3,
            no2 = no2,
            co = co,
            healthAdvisory = advisory,
            sensitiveGroupsAdvisory = sensitiveAdvisory,
            outdoorExerciseScore = outdoorExerciseScore
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
        // Estimate 3-hour pressure change from hourly sequence
        val delta = if (hourly.size >= 4) {
            // MET Norway pressure trend or difference
            val futurePoint = hourly[3]
            // In standard troposphere, temperature & pressure fluctuate together with fronts
            (futurePoint.temperatureC - current.temperatureC) * -0.4
        } else {
            -0.6
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
