package com.example

import com.example.domain.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun rainPlanner_identifiesDryPeriodCorrectly() {
        val dryHours = (0..23).map { i ->
            HourlyPoint(
                timeIso = "2026-10-13T${i}:00:00Z",
                hourLabel = "${i}:00",
                temperatureC = 20.0,
                conditionType = WeatherConditionType.CLEAR_DAY,
                symbolCode = "clearsky_day",
                precipProbability = 0,
                precipAmountMm = 0.0,
                windSpeedKmh = 10.0,
                windDirectionDeg = 180.0,
                windCardinal = "S",
                humidityPercent = 45
            )
        }

        val window = RainPlanner.calculateRainWindow(dryHours, "2026-10-13T10:00:00Z")
        assertFalse(window.hasRain)
        assertEquals("High Confidence", window.confidenceRating)
        assertTrue(window.summaryHeadline.contains("Dry conditions"))
    }

    @Test
    fun rainPlanner_detectsRainWindowAccurately() {
        val hours = (0..23).map { i ->
            val hasPrecip = i in 14..17
            HourlyPoint(
                timeIso = "2026-10-13T${i}:00:00Z",
                hourLabel = "${i}:00",
                temperatureC = 18.0,
                conditionType = if (hasPrecip) WeatherConditionType.LIGHT_RAIN else WeatherConditionType.CLOUDY,
                symbolCode = if (hasPrecip) "rain" else "cloudy",
                precipProbability = if (hasPrecip) 75 else 10,
                precipAmountMm = if (hasPrecip) 1.5 else 0.0,
                windSpeedKmh = 12.0,
                windDirectionDeg = 200.0,
                windCardinal = "SSW",
                humidityPercent = 70
            )
        }

        val window = RainPlanner.calculateRainWindow(hours, "2026-10-13T10:00:00Z")
        assertTrue(window.hasRain)
        assertEquals("14:00", window.startHour)
        assertEquals("17:00", window.endHour)
        assertEquals(75, window.peakProbability)
        assertEquals("High Confidence", window.confidenceRating)
    }

    @Test
    fun activityCalculator_ratesComfortableWeatherHighly() {
        val current = CurrentWeather(
            temperatureC = 20.0,
            feelsLikeC = 19.5,
            minTempTodayC = 15.0,
            maxTempTodayC = 22.0,
            conditionType = WeatherConditionType.CLEAR_DAY,
            conditionLabel = "Clear Sky",
            symbolCode = "clearsky_day",
            humidityPercent = 50,
            windSpeedKmh = 10.0,
            windDirectionDeg = 90.0,
            windCardinal = "E",
            cloudCoverPercent = 20,
            uvIndex = 3.0,
            airPressureHpa = 1015.0,
            dewPointC = 10.0,
            nextChangeDescription = "Clear conditions"
        )

        val hourly = (0..12).map { i ->
            HourlyPoint(
                timeIso = "2026-10-13T${i}:00:00Z",
                hourLabel = "${i}:00",
                temperatureC = 20.0,
                conditionType = WeatherConditionType.CLEAR_DAY,
                symbolCode = "clearsky_day",
                precipProbability = 0,
                precipAmountMm = 0.0,
                windSpeedKmh = 10.0,
                windDirectionDeg = 90.0,
                windCardinal = "E",
                humidityPercent = 50
            )
        }

        val recs = ActivityCalculator.evaluateActivities(current, hourly, UserSettings())
        val walkingRec = recs.first { it.activity == ActivityType.WALKING }
        assertTrue(walkingRec.scorePercent >= 80)
        assertEquals("Optimal", walkingRec.ratingLabel)
    }

    @Test
    fun analyticsCalculator_evaluatesAirQualityAccuratelyWithRealObservations() {
        val obs = com.example.data.model.AirQualityCurrentData(
            time = "2026-10-15T12:00",
            interval = 3600,
            usAqi = 42,
            pm25 = 9.8,
            pm10 = 16.4,
            carbonMonoxide = 210.0,
            nitrogenDioxide = 12.5,
            ozone = 45.0
        )

        val aq = WeatherAnalyticsCalculator.evaluateAirQuality(obs)
        assertTrue("Air quality must be available", aq.isAvailable)
        assertEquals(42, aq.aqi)
        assertEquals("Good", aq.category)
        assertEquals(9.8, aq.pm25 ?: 0.0, 0.01)
        assertEquals(16.4, aq.pm10 ?: 0.0, 0.01)
        assertTrue("Exercise safety score must be high for good AQI", aq.outdoorExerciseScore in 70..100)
        assertTrue("Data source must be documented", aq.dataSource.contains("Open-Meteo"))

        // When observation feed is unavailable (offline or missing station)
        val unavailableAq = WeatherAnalyticsCalculator.evaluateAirQuality(null)
        assertFalse("Must be marked unavailable when observation is null", unavailableAq.isAvailable)
        assertEquals("Unavailable", unavailableAq.category)
        assertNull("PM2.5 must not be fabricated when unavailable", unavailableAq.pm25)
    }

    @Test
    fun analyticsCalculator_calculatesAstronomyProgression() {
        val astronomy = WeatherAnalyticsCalculator.calculateAstronomy(51.5074, -0.1278, "Europe/London")
        assertTrue("Sunrise must have format HH:mm", astronomy.sunriseTime.contains(":"))
        assertTrue("Sunset must have format HH:mm", astronomy.sunsetTime.contains(":"))
        assertTrue("Day length hours must be realistic", astronomy.dayLengthHours in 6..18)
        assertTrue("Sun progress fraction must be between 0 and 1", astronomy.sunProgressFraction in 0.0f..1.0f)
        assertTrue("Moon illumination must be between 0 and 100", astronomy.moonIlluminationPercent in 0..100)
    }

    @Test
    fun analyticsCalculator_calculatesWardrobeAndWind() {
        val current = CurrentWeather(
            temperatureC = 4.0,
            feelsLikeC = 1.0,
            minTempTodayC = 0.0,
            maxTempTodayC = 7.0,
            conditionType = WeatherConditionType.LIGHT_RAIN,
            conditionLabel = "Cold Light Rain",
            symbolCode = "rain",
            humidityPercent = 88,
            windSpeedKmh = 28.0,
            windDirectionDeg = 45.0,
            windCardinal = "NE",
            cloudCoverPercent = 95,
            uvIndex = 1.0,
            airPressureHpa = 1002.0,
            dewPointC = 2.0,
            nextChangeDescription = "Rain ending later"
        )

        val rainWindow = RainWindow(
            hasRain = true,
            startHour = "12:00",
            endHour = "16:00",
            peakProbability = 80,
            totalEstimatedMm = 3.5,
            confidenceRating = "High Confidence",
            summaryHeadline = "Rain expected",
            detailedReasoning = "Front passing",
            modelLastUpdatedAt = "2026-10-13T10:00:00Z"
        )

        val wardrobe = WeatherAnalyticsCalculator.calculateWardrobe(current, rainWindow, emptyList(), UserSettings())
        assertTrue("Should recommend umbrella when rain is likely", wardrobe.umbrellaNeeded)
        assertTrue("Should recommend warm layers in cold temperatures", wardrobe.headline.contains("Warm") || wardrobe.headline.contains("Winter") || wardrobe.headline.contains("Layers"))

        val wind = WeatherAnalyticsCalculator.calculateWindDynamics(current, emptyList())
        assertTrue("Gust speed should exceed sustained wind speed", wind.gustSpeedKmh > current.windSpeedKmh)
        assertTrue("Beaufort scale should be positive", wind.beaufortScale >= 4)
    }

    @Test
    fun rainPlanner_detectsIntermittentClustersSeparatedByGaps() {
        val hours = (0..23).map { i ->
            when (i) {
                8 -> createHourlyPoint(i, precipProb = 65, precipMm = 1.2, cond = WeatherConditionType.LIGHT_RAIN)
                20 -> createHourlyPoint(i, precipProb = 75, precipMm = 2.0, cond = WeatherConditionType.HEAVY_RAIN)
                else -> createHourlyPoint(i, precipProb = 5, precipMm = 0.0)
            }
        }

        val window = RainPlanner.calculateRainWindow(hours, "2026-10-15T06:00:00Z")
        assertTrue("Must detect rain when showers are forecasted", window.hasRain)
        assertTrue("Must report intermittent showers rather than continuous all-day rain", window.summaryHeadline.contains("Intermittent"))
        assertTrue("Must include morning shower", window.summaryHeadline.contains("08:00"))
        assertTrue("Must include evening shower", window.summaryHeadline.contains("20:00"))
        assertEquals(3.2, window.totalEstimatedMm, 0.01)
        assertEquals(75, window.peakProbability)
    }

    @Test
    fun rainPlanner_handlesSingleIsolatedShower() {
        val hours = (0..23).map { i ->
            if (i == 15) createHourlyPoint(i, precipProb = 55, precipMm = 0.8, cond = WeatherConditionType.LIGHT_RAIN)
            else createHourlyPoint(i, precipProb = 0, precipMm = 0.0)
        }

        val window = RainPlanner.calculateRainWindow(hours, "2026-10-15T06:00:00Z")
        assertTrue("Must detect single shower", window.hasRain)
        assertTrue("Must identify specific hour", window.summaryHeadline.contains("Rain possible around 15:00"))
    }

    @Test
    fun rainPlanner_disclosesPartialForecastCoverage() {
        // Only 6 hours of forecast points available
        val partialHours = (0..5).map { createHourlyPoint(it, precipProb = 0, precipMm = 0.0) }

        val window = RainPlanner.calculateRainWindow(partialHours, "2026-10-15T06:00:00Z")
        assertFalse(window.hasRain)
        assertTrue("Must disclose exact coverage hours in headline", window.summaryHeadline.contains("6 hours"))
    }

    @Test
    fun rainPlanner_handlesEmptyDataGracefully() {
        val window = RainPlanner.calculateRainWindow(emptyList(), "2026-10-15T06:00:00Z")
        assertFalse(window.hasRain)
        assertEquals("Forecast unavailable", window.summaryHeadline)
        assertEquals("--", window.startHour)
    }

    @Test
    fun analyticsCalculator_calculatesBarometerTrendFromRealPressureDelta() {
        val current = CurrentWeather(
            temperatureC = 20.0,
            feelsLikeC = 20.0,
            minTempTodayC = 15.0,
            maxTempTodayC = 25.0,
            conditionType = WeatherConditionType.CLEAR_DAY,
            conditionLabel = "Clear Sky",
            symbolCode = "clearsky_day",
            humidityPercent = 50,
            windSpeedKmh = 10.0,
            windDirectionDeg = 180.0,
            windCardinal = "S",
            cloudCoverPercent = 10,
            uvIndex = 5.0,
            airPressureHpa = 1016.0,
            dewPointC = 10.0,
            nextChangeDescription = "Steady"
        )

        // Hour 3 has pressure 1011.0 hPa (drop of 5.0 hPa)
        val hourly = (0..5).map { i ->
            val p = if (i == 3) 1011.0 else 1016.0
            createHourlyPoint(i, pressure = p)
        }

        val trend = WeatherAnalyticsCalculator.calculateBarometerTrend(current, hourly)
        assertEquals(-5.0, trend.change3hHpa, 0.01)
        assertEquals("Falling Rapidly", trend.trendLabel)
        assertTrue("Implication should warn about incoming low front", trend.shortTermImplication.contains("front") || trend.shortTermImplication.contains("low"))
    }

    private fun createHourlyPoint(
        hour: Int,
        precipProb: Int = 0,
        precipMm: Double = 0.0,
        cond: WeatherConditionType = WeatherConditionType.CLEAR_DAY,
        pressure: Double? = 1013.2
    ): HourlyPoint {
        return HourlyPoint(
            timeIso = "2026-10-15T${String.format(java.util.Locale.US, "%02d", hour)}:00:00Z",
            hourLabel = "${String.format(java.util.Locale.US, "%02d", hour)}:00",
            temperatureC = 20.0,
            conditionType = cond,
            symbolCode = "clearsky_day",
            precipProbability = precipProb,
            precipAmountMm = precipMm,
            windSpeedKmh = 12.0,
            windDirectionDeg = 180.0,
            windCardinal = "S",
            humidityPercent = 50,
            isDaylight = hour in 6..19,
            pressureHpa = pressure
        )
    }
}
