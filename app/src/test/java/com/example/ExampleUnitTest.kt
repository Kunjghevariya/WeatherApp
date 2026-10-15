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
    fun analyticsCalculator_calculatesAirQualityAccurately() {
        val current = CurrentWeather(
            temperatureC = 22.0,
            feelsLikeC = 22.0,
            minTempTodayC = 16.0,
            maxTempTodayC = 25.0,
            conditionType = WeatherConditionType.CLEAR_DAY,
            conditionLabel = "Clear Sky",
            symbolCode = "clearsky_day",
            humidityPercent = 55,
            windSpeedKmh = 14.0,
            windDirectionDeg = 180.0,
            windCardinal = "S",
            cloudCoverPercent = 10,
            uvIndex = 6.0,
            airPressureHpa = 1016.0,
            dewPointC = 11.0,
            nextChangeDescription = "Clear sky continuing"
        )

        val aq = WeatherAnalyticsCalculator.calculateAirQuality(current, 51.5074)
        assertTrue("AQI must be positive", aq.aqi in 10..300)
        assertTrue("Sub-pollutant PM2.5 must be greater than zero", aq.pm25 > 0)
        assertNotNull("Category must not be null", aq.category)
        assertTrue("Exercise safety score must be bounded", aq.outdoorExerciseScore in 0..100)
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
}
