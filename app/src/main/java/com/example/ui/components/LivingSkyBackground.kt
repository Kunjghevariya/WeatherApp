package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.domain.LivingParticleType
import com.example.domain.WeatherConditionType
import com.example.domain.WeatherIconService

@Composable
fun LivingSkyBackground(
    condition: WeatherConditionType,
    isDaytime: Boolean,
    reducedMotion: Boolean,
    symbolCode: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Standardized condition configuration from MET Norway mapper
    val stdCondition = symbolCode?.let { WeatherIconService.mapCondition(it) }
        ?: WeatherIconService.mapConditionType(condition, isDaytime)

    // Atmospheric colors based on condition and daylight
    val skyColors = if (isDaytime) stdCondition.daySkyColors else stdCondition.nightSkyColors
    val topColor = skyColors.firstOrNull() ?: Color(0xFF0284C7)
    val bottomColor = skyColors.getOrNull(1) ?: Color(0xFF38BDF8)
    val accentGlow = skyColors.lastOrNull() ?: Color(0xFFFBBF24)

    // Gentle ambient motion animation for particles / clouds
    val infiniteTransition = rememberInfiniteTransition(label = "SkyMotion")
    val animOffset by if (!reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SkyFlow"
        )
    } else {
        rememberUpdatedState(0.5f)
    }

    val rainOffset by if (!reducedMotion && (condition == WeatherConditionType.LIGHT_RAIN || condition == WeatherConditionType.HEAVY_RAIN)) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "RainDropMotion"
        )
    } else {
        rememberUpdatedState(0f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                    startY = 0f,
                    endY = height
                )
            )

            // 2. Celestial Accent Glow (Sun or Moon glow at top-right)
            val glowCenter = Offset(width * 0.85f, height * 0.12f)
            val glowRadius = width * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentGlow.copy(alpha = if (isDaytime) 0.35f else 0.15f), Color.Transparent),
                    center = glowCenter,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = glowCenter
            )

            // 3. Ambient Clouds / Horizon Mist
            val cloudY = height * 0.35f
            val drift = (animOffset * width) % width
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset((drift + width * 0.2f) % width, cloudY),
                    radius = width * 0.6f
                ),
                topLeft = Offset(((drift + width * 0.2f) % width) - width * 0.6f, cloudY - 100f),
                size = Size(width * 1.2f, 200f)
            )

            // 4. Subtle Rain Streaks if raining
            if (condition == WeatherConditionType.LIGHT_RAIN || condition == WeatherConditionType.HEAVY_RAIN || condition == WeatherConditionType.THUNDERSTORM) {
                val dropCount = if (condition == WeatherConditionType.HEAVY_RAIN) 35 else 18
                for (i in 0 until dropCount) {
                    val x = (width / dropCount) * i + (i * 13) % 40
                    val dropY = ((rainOffset + (i.toFloat() / dropCount)) % 1f) * height
                    drawLine(
                        color = Color(0x70E0F2FE),
                        start = Offset(x, dropY),
                        end = Offset(x - 8f, dropY + 28f),
                        strokeWidth = 2f
                    )
                }
            }
        }

        content()
    }
}
