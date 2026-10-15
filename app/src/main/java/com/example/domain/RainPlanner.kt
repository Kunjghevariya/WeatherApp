package com.example.domain

import java.util.Locale

/**
 * Meteorological precipitation planner that evaluates rain onset, accumulation,
 * and intermittent shower clusters over available hourly forecast data.
 * Adheres strictly to conservative uncertainty language without claiming minute-level precision.
 */
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

        // Analyze up to next 24 available hourly points
        val availablePoints = hourlyPoints.take(24)
        val coverageHours = availablePoints.size

        // Identify points with measurable precipitation or high probability
        val rainPointsWithIndices = availablePoints.mapIndexedNotNull { index, pt ->
            val isRain = pt.precipProbability >= 30 || pt.precipAmountMm >= 0.1 ||
                pt.conditionType in listOf(
                    WeatherConditionType.LIGHT_RAIN,
                    WeatherConditionType.HEAVY_RAIN,
                    WeatherConditionType.THUNDERSTORM,
                    WeatherConditionType.SNOW,
                    WeatherConditionType.SLEET
                )
            if (isRain) Pair(index, pt) else null
        }

        if (rainPointsWithIndices.isEmpty()) {
            val maxProb = availablePoints.maxOfOrNull { it.precipProbability } ?: 0
            val headline = if (coverageHours < 24) {
                "Dry conditions projected for next $coverageHours hours"
            } else {
                "Dry conditions projected for the next 24 hours"
            }
            return RainWindow(
                hasRain = false,
                startHour = "--",
                endHour = "--",
                peakProbability = maxProb,
                totalEstimatedMm = 0.0,
                confidenceRating = if (coverageHours >= 12) "High Confidence" else "Moderate Confidence",
                summaryHeadline = headline,
                detailedReasoning = "Numerical weather prediction models project minimal precipitation probability (below 30%) across the next $coverageHours hours. Outdoor activities are favorable.",
                modelLastUpdatedAt = modelUpdatedAt
            )
        }

        // Group consecutive or close rain points into distinct clusters (separated by > 2 dry hours)
        val clusters = mutableListOf<MutableList<Pair<Int, HourlyPoint>>>()
        var currentCluster = mutableListOf<Pair<Int, HourlyPoint>>()

        for (item in rainPointsWithIndices) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(item)
            } else {
                val previousIndex = currentCluster.last().first
                // Gap between wet points <= 2 hours -> same precipitation event
                if (item.first - previousIndex <= 2) {
                    currentCluster.add(item)
                } else {
                    clusters.add(currentCluster)
                    currentCluster = mutableListOf(item)
                }
            }
        }
        if (currentCluster.isNotEmpty()) {
            clusters.add(currentCluster)
        }

        val allRainPoints = rainPointsWithIndices.map { it.second }
        val peakProb = allRainPoints.maxOf { it.precipProbability }
        val rawTotalMm = allRainPoints.sumOf { it.precipAmountMm }
        val totalMm = (rawTotalMm * 10.0).let { Math.round(it) } / 10.0

        val firstRain = allRainPoints.first()
        val lastRain = allRainPoints.last()

        val confidence = when {
            coverageHours < 6 -> "Low Confidence (Limited Model Coverage)"
            peakProb >= 70 && allRainPoints.size >= 2 -> "High Confidence"
            peakProb >= 45 -> "Moderate Confidence"
            else -> "Moderate Uncertainty (Scattered / Convective Risk)"
        }

        val headline = when {
            clusters.size == 1 -> {
                val cluster = clusters[0].map { it.second }
                if (cluster.size == 1) {
                    "Rain possible around ${cluster.first().hourLabel}"
                } else {
                    "Rain window: ${cluster.first().hourLabel} – ${cluster.last().hourLabel}"
                }
            }
            clusters.size > 1 -> {
                val clusterSummaries = clusters.map { cl ->
                    val pts = cl.map { it.second }
                    if (pts.size == 1) pts.first().hourLabel else "${pts.first().hourLabel}–${pts.last().hourLabel}"
                }
                "Intermittent rain: ${clusterSummaries.joinToString(", ")}"
            }
            else -> "Rain window: ${firstRain.hourLabel} – ${lastRain.hourLabel}"
        }

        val reasoning = buildString {
            append("Precipitation potential peaks at ${peakProb}% with ~${String.format(Locale.US, "%.1f", totalMm)} mm estimated accumulation across $coverageHours-hour model run. ")
            if (clusters.size > 1) {
                append("Showers are broken into multiple intermittent periods rather than continuous rain. ")
            }
            if (peakProb >= 70) {
                append("Model runs show strong synoptic agreement on front progression. Waterproof gear recommended.")
            } else {
                append("Convective or scattered showers possible. Actual start times may vary slightly from hourly model time steps.")
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
