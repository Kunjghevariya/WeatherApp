package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.domain.LivingParticleType
import com.example.domain.WeatherConditionType
import com.example.domain.WeatherIconMapper
import com.example.domain.WeatherIconService
import org.junit.Assert.*
import org.junit.Test

class WeatherIconServiceTest {

    @Test
    fun mapCondition_clearskyDay_returnsSunnyIconAndSunbeams() {
        val mapped = WeatherIconService.mapCondition("clearsky_day")
        assertEquals(WeatherConditionType.CLEAR_DAY, mapped.conditionType)
        assertEquals("☀️", mapped.emoji)
        assertEquals(Icons.Filled.WbSunny, mapped.icon)
        assertEquals("lottie/weather_clear_day.json", mapped.lottieAssetPath)
        assertEquals(LivingParticleType.SUNBEAMS, mapped.particleType)
    }

    @Test
    fun mapCondition_clearskyNight_returnsMoonIconAndStars() {
        val mapped = WeatherIconService.mapCondition("clearsky_night")
        assertEquals(WeatherConditionType.CLEAR_NIGHT, mapped.conditionType)
        assertEquals("🌙", mapped.emoji)
        assertEquals(Icons.Filled.NightsStay, mapped.icon)
        assertEquals("lottie/weather_clear_night.json", mapped.lottieAssetPath)
        assertEquals(LivingParticleType.STARS, mapped.particleType)
    }

    @Test
    fun mapCondition_thunderstorm_returnsThunderboltsAndCorrectLottie() {
        val mapped = WeatherIconService.mapCondition("heavyrainandthunder")
        assertEquals(WeatherConditionType.THUNDERSTORM, mapped.conditionType)
        assertEquals("⛈️", mapped.emoji)
        assertEquals("lottie/weather_thunderstorm.json", mapped.lottieAssetPath)
        assertEquals(LivingParticleType.THUNDER_BOLTS, mapped.particleType)
    }

    @Test
    fun mapCondition_heavyrain_returnsRainHeavyParticles() {
        val mapped = WeatherIconService.mapCondition("heavyrain")
        assertEquals(WeatherConditionType.HEAVY_RAIN, mapped.conditionType)
        assertEquals("🌧️", mapped.emoji)
        assertEquals("lottie/weather_rain_heavy.json", mapped.lottieAssetPath)
        assertEquals(LivingParticleType.RAIN_HEAVY, mapped.particleType)
    }

    @Test
    fun mapCondition_snowShowers_returnsSnowDriftParticles() {
        val mapped = WeatherIconService.mapCondition("snowshowers_day")
        assertEquals(WeatherConditionType.SNOW, mapped.conditionType)
        assertEquals("❄️", mapped.emoji)
        assertEquals(Icons.Filled.AcUnit, mapped.icon)
        assertEquals(LivingParticleType.SNOW_DRIFT, mapped.particleType)
    }

    @Test
    fun mapCondition_fog_returnsMistParticles() {
        val mapped = WeatherIconService.mapCondition("fog")
        assertEquals(WeatherConditionType.FOG, mapped.conditionType)
        assertEquals("🌫️", mapped.emoji)
        assertEquals(LivingParticleType.MIST_FOG, mapped.particleType)
    }

    @Test
    fun mapCondition_nullOrUnknown_fallsBackGracefully() {
        val mappedNull = WeatherIconService.mapCondition(null)
        assertNotNull(mappedNull)
        assertEquals(WeatherConditionType.CLEAR_DAY, mappedNull.conditionType)

        val mappedUnknown = WeatherIconService.mapCondition("unusual_weather_code_xyz")
        assertNotNull(mappedUnknown)
        assertEquals(WeatherConditionType.CLEAR_DAY, mappedUnknown.conditionType)
    }

    @Test
    fun mapConditionType_generatesValidStandardizedCondition() {
        val condition = WeatherIconService.mapConditionType(WeatherConditionType.THUNDERSTORM)
        assertEquals(WeatherConditionType.THUNDERSTORM, condition.conditionType)
        assertEquals("lottie/weather_thunderstorm.json", condition.lottieAssetPath)
        assertTrue(condition.daySkyColors.isNotEmpty())
        assertTrue(condition.nightSkyColors.isNotEmpty())
    }

    @Test
    fun helperMethods_returnConsistentValues() {
        val lottie = WeatherIconService.getLottieAnimationPath("heavyrain")
        assertEquals("lottie/weather_rain_heavy.json", lottie)

        val emoji = WeatherIconService.getEmoji("clearsky_day")
        assertEquals("☀️", emoji)

        val icon = WeatherIconService.getStandardizedIcon("fog")
        assertEquals(Icons.Filled.CloudQueue, icon)

        // Verify getMaterialIcon alias
        val materialIcon = WeatherIconService.getMaterialIcon("snow")
        assertEquals(Icons.Filled.AcUnit, materialIcon)

        // Verify getIconTint returns non-null valid colors
        val sunTint = WeatherIconService.getIconTint("clearsky_day")
        val rainTint = WeatherIconService.getIconTint("heavyrain")
        assertNotEquals(sunTint, rainTint)

        // Verify WeatherIconMapper typealias points to WeatherIconService
        val mappedViaAlias = WeatherIconMapper.getMaterialIcon("clearsky_day")
        assertEquals(Icons.Filled.WbSunny, mappedViaAlias)
    }
}
