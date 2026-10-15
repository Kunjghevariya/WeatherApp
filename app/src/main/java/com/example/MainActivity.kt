package com.example
 
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.WeatherConditionType
import com.example.ui.WeatherViewModel
import com.example.ui.components.LivingSkyBackground
import com.example.ui.screens.MainScreen
import com.example.ui.screens.MainTab
import com.example.ui.theme.WeatherFlowTheme

// Re-export for backward compatibility
typealias MainTab = com.example.ui.screens.MainTab

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

            // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // If granted, notifications are posted normally
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Calculate real daylight status from live astronomical calculations or hourly forecast
            val isDaytime = uiState.selectedTimeTravelHourIndex?.let {
                uiState.weatherData?.hourly?.getOrNull(it)?.isDaylight
            } ?: uiState.weatherData?.astronomy?.isSunUp
              ?: uiState.weatherData?.current?.symbolCode?.contains("day")
              ?: true

            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = when (uiState.userSettings.themeMode) {
                "day" -> false
                "night" -> true
                "system" -> systemDark
                else -> !isDaytime // "auto" adapts to actual sunup/sundown solar cycle
            }

            WeatherFlowTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                // Determine living sky condition and daylight state
                val condition = uiState.selectedTimeTravelHourIndex?.let {
                    uiState.weatherData?.hourly?.getOrNull(it)?.conditionType
                } ?: uiState.weatherData?.current?.conditionType ?: WeatherConditionType.CLEAR_DAY

                val symbolCode = uiState.selectedTimeTravelHourIndex?.let {
                    uiState.weatherData?.hourly?.getOrNull(it)?.symbolCode
                } ?: uiState.weatherData?.current?.symbolCode ?: (if (isDaytime) "clearsky_day" else "clearsky_night")

                LivingSkyBackground(
                    condition = condition,
                    isDaytime = isDaytime,
                    reducedMotion = uiState.userSettings.reducedMotion,
                    symbolCode = symbolCode
                ) {
                    MainScreen(
                        uiState = uiState,
                        viewModel = weatherViewModel,
                        modifier = Modifier.fillMaxSize()
                    )

                    // First-time install location onboarding dialog
                    if (uiState.isOnboarding) {
                        com.example.ui.components.LocationOnboardingDialog(
                            viewModel = weatherViewModel
                        )
                    }
                }
            }
        }
    }
}
