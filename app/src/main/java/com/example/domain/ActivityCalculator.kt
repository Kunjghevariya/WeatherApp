package com.example.domain

object ActivityCalculator {

    fun evaluateActivities(
        current: CurrentWeather,
        hourly: List<HourlyPoint>,
        userSettings: UserSettings
    ): List<ActivityRecommendation> {
        return ActivityType.values().map { activity ->
            evaluateSingleActivity(activity, current, hourly, userSettings)
        }
    }

    private fun evaluateSingleActivity(
        activity: ActivityType,
        current: CurrentWeather,
        hourly: List<HourlyPoint>,
        settings: UserSettings
    ): ActivityRecommendation {
        val next12 = hourly.take(12)

        // Evaluate current conditions
        var score = 100
        val factors = mutableListOf<ActivityFactor>()

        // 1. Rain Penalty
        val currentRainChance = next12.firstOrNull()?.precipProbability ?: 0
        if (currentRainChance > settings.maxRainTolerance) {
            val penalty = ((currentRainChance - settings.maxRainTolerance) * 0.8).toInt()
            score -= penalty.coerceIn(10, 60)
            factors.add(ActivityFactor(false, "Rain risk: ${currentRainChance}% (above your ${settings.maxRainTolerance.toInt()}% comfort limit)"))
        } else {
            factors.add(ActivityFactor(true, "Dry or low rain risk (${currentRainChance}%)"))
        }

        // 2. Temperature Check
        val idealMin = settings.idealTempMin
        val idealMax = settings.idealTempMax
        val temp = current.temperatureC
        val isF = settings.tempUnit == "F"
        val tempFormatted = if (isF) "${((temp * 9 / 5) + 32).toInt()}°F" else "${temp.toInt()}°C"
        val minFormatted = if (isF) "${((idealMin * 9 / 5) + 32).toInt()}°F" else "${idealMin.toInt()}°C"
        val maxFormatted = if (isF) "${((idealMax * 9 / 5) + 32).toInt()}°F" else "${idealMax.toInt()}°C"

        if (temp in idealMin..idealMax) {
            factors.add(ActivityFactor(true, "Pleasant temperature at $tempFormatted (within $minFormatted–$maxFormatted target)"))
        } else if (temp < idealMin) {
            val diff = idealMin - temp
            score -= (diff * 4).toInt().coerceIn(5, 40)
            factors.add(ActivityFactor(false, "Cooler than preferred ($tempFormatted vs $minFormatted min)"))
        } else {
            val diff = temp - idealMax
            score -= (diff * 5).toInt().coerceIn(5, 50)
            factors.add(ActivityFactor(false, "Warmer than preferred ($tempFormatted vs $maxFormatted max)"))
        }

        // 3. Wind Check
        val wind = current.windSpeedKmh
        if (wind > settings.maxWindTolerance) {
            val diff = wind - settings.maxWindTolerance
            score -= (diff * 2.5).toInt().coerceIn(10, 50)
            factors.add(ActivityFactor(false, "Breezy/gusty winds (${wind.toInt()} km/h exceeds ${settings.maxWindTolerance.toInt()} km/h limit)"))
        } else {
            factors.add(ActivityFactor(true, "Gentle wind (${wind.toInt()} km/h ${current.windCardinal})"))
        }

        // 4. Activity Specific Nuance
        when (activity) {
            ActivityType.RUNNING -> {
                if (current.humidityPercent > 75) {
                    score -= 10
                    factors.add(ActivityFactor(false, "High humidity (${current.humidityPercent}%) may increase perceived exertion"))
                }
            }
            ActivityType.CYCLING -> {
                if (wind > 20) {
                    score -= 15
                    factors.add(ActivityFactor(false, "Crosswinds may impact bicycle handling"))
                }
            }
            ActivityType.OUTDOOR_LAUNDRY -> {
                if (current.humidityPercent < 60 && currentRainChance < 10) {
                    score += 5
                    factors.add(ActivityFactor(true, "Low humidity & dry air allow rapid drying"))
                } else if (current.humidityPercent >= 70) {
                    score -= 20
                    factors.add(ActivityFactor(false, "High ambient humidity slows moisture evaporation"))
                }
            }
            ActivityType.PHOTOGRAPHY -> {
                if (current.conditionType == WeatherConditionType.PARTLY_CLOUDY_DAY || current.conditionType == WeatherConditionType.CLEAR_DAY) {
                    factors.add(ActivityFactor(true, "Scenic ambient lighting & cloud contrast"))
                }
            }
            ActivityType.DOG_WALKING -> {
                if (temp > 28) {
                    score -= 25
                    factors.add(ActivityFactor(false, "Hot pavement risk for dog paws"))
                }
            }
            else -> {}
        }

        score = score.coerceIn(5, 99)

        // Find best upcoming 2-hour window in next 12h
        val bestWindow = findBestWindow(hourly.take(12), settings, activity)

        val ratingLabel = when {
            score >= 80 -> "Optimal"
            score >= 60 -> "Good"
            score >= 40 -> "Moderate"
            else -> "Unfavorable"
        }

        val currentSuitabilityText = when {
            score >= 80 -> "Great conditions right now! Perfect for ${activity.displayName.lowercase()}."
            score >= 60 -> "Decent conditions with mild factors to note."
            score >= 40 -> "Acceptable, but consider the alternative window below."
            else -> "Conditions are suboptimal right now. Check later window."
        }

        return ActivityRecommendation(
            activity = activity,
            scorePercent = score,
            ratingLabel = ratingLabel,
            bestWindowLabel = bestWindow,
            currentSuitabilityText = currentSuitabilityText,
            factors = factors
        )
    }

    private fun findBestWindow(
        hourly: List<HourlyPoint>,
        settings: UserSettings,
        activity: ActivityType
    ): String {
        if (hourly.size < 2) return "Now"
        var bestIndex = 0
        var bestScore = -1

        for (i in 0 until (hourly.size - 1)) {
            val p1 = hourly[i]
            val p2 = hourly[i + 1]
            val avgRain = (p1.precipProbability + p2.precipProbability) / 2
            val avgTemp = (p1.temperatureC + p2.temperatureC) / 2
            val avgWind = (p1.windSpeedKmh + p2.windSpeedKmh) / 2

            var windowScore = 100 - (avgRain * 1.2).toInt()
            if (avgTemp < settings.idealTempMin) windowScore -= ((settings.idealTempMin - avgTemp) * 4).toInt()
            if (avgTemp > settings.idealTempMax) windowScore -= ((avgTemp - settings.idealTempMax) * 5).toInt()
            if (avgWind > settings.maxWindTolerance) windowScore -= ((avgWind - settings.maxWindTolerance) * 2).toInt()

            if (windowScore > bestScore) {
                bestScore = windowScore
                bestIndex = i
            }
        }

        val start = hourly[bestIndex].hourLabel
        val end = hourly[(bestIndex + 2).coerceAtMost(hourly.size - 1)].hourLabel
        return "$start – $end"
    }
}
