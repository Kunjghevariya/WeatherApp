package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueDark,
    onPrimary = Color(0xFF003554),
    primaryContainer = Color(0xFF004D74),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = CyanAccentDark,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004E5B),
    onSecondaryContainer = Color(0xFFA6EEFF),
    tertiary = AmberSunDark,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = MidnightBackground,
    onBackground = TextHighContrastLight,
    surface = MidnightSurface,
    onSurface = TextHighContrastLight,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextMutedLight,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = CyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA6EEFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = AmberSun,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDB3),
    onTertiaryContainer = Color(0xFF2B1700),
    background = DaylightBackground,
    onBackground = TextDark,
    surface = DaylightSurface,
    onSurface = TextDark,
    surfaceVariant = DaylightSurfaceVariant,
    onSurfaceVariant = TextMutedDark,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun WeatherFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Weather app looks most cohesive with custom atmospheric palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
