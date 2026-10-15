package com.example.domain

object RainPlanner {
    fun calculateRainWindow(
        hourlyPoints: List<HourlyPoint>,
        modelUpdatedAt: String
    ): RainWindow {
        if (hourlyPoints.isEmpty()) {
            return RainWindow(
                hasRain = false,
                startHour = "--",
                endHour = "--",
                peakProbability = 0,
                totalEstimatedMm = 0.0,
                confidenceRating = "Unknown",
                summaryHeadline = "Forecast unavailable",
                detailedReasoning = "No hourly forecasts currently loaded.",
                modelLastUpdatedAt = modelUpdatedAt
            )
        }

        // Analyze next 24 hours
        val next24 = hourlyPoints.take(24)
        val rainPoints = next24.filter { it.precipProbability >= 30 || it.precipAmountMm >= 0.1 }

        if (rainPoints.isEmpty()) {
            return RainWindow(
                hasRain = false,
                startHour = "--",
                endHour = "--",
                peakProbability = next24.maxOfOrNull { it.precipProbability } ?: 0,
                totalEstimatedMm = 0.0,
                confidenceRating = "High Confidence",
                summaryHeadline = "Dry conditions for the next 24 hours",
                detailedReasoning = "All forecast models project clear skies or minimal precipitation probability below 25%. Ideal for outdoor plans.",
                modelLastUpdatedAt = modelUpdatedAt
            )
        }

        val firstRain = rainPoints.first()
        val lastRain = rainPoints.last()
        val peakProb = rainPoints.maxOf { it.precipProbability }
        val totalMm = rainPoints.sumOf { it.precipAmountMm }

        val confidence = when {
            peakProb >= 70 && rainPoints.size >= 2 -> "High Confidence"
            peakProb >= 50 -> "Moderate Confidence"
            else -> "Moderate Uncertainty"
        }

        val headline = if (firstRain.hourLabel == lastRain.hourLabel) {
            "Rain shower likely around ${firstRain.hourLabel}"
        } else {
            "Rain window: ${firstRain.hourLabel} – ${lastRain.hourLabel}"
        }

        val reasoning = buildString {
            append("Peak chance of ${peakProb}% with ~${String.format(java.util.Locale.US, "%.1f", totalMm)} mm expected. ")
            if (peakProb >= 70) {
                append("Multiple model runs show consistent front timing. Waterproof gear recommended.")
            } else {
                append("Scattered or convective showers possible. Microbursts may start slightly earlier or later than hourly model steps.")
            }
        }

        return RainWindow(
            hasRain = true,
            startHour = firstRain.hourLabel,
            endHour = lastRain.hourLabel,
            peakProbability = peakProb,
            totalEstimatedMm = totalMm,
            confidenceRating = confidence,
            summaryHeadline = headline,
            detailedReasoning = reasoning,
            modelLastUpdatedAt = modelUpdatedAt
        )
    }
}
