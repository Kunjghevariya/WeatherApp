package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WeatherIconService
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.getConditionEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    onNavigateToWidgets: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onNavigateToTrustCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val freshness = weatherData?.freshness
    val userSettings = uiState.userSettings
    val current = weatherData?.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "You & Weather Intelligence",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Personal comfort, sensitivities & trust",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("you_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("you_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("you_screen_list"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Personal Weather Signature Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF132238).copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f)),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("you_profile_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.35f))
                                    .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Your Weather Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Active in ${uiState.currentCity.name}, ${uiState.currentCity.country}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8)
                                    )
                                )
                                val headerMin = if (userSettings.tempUnit == "F") "${((userSettings.idealTempMin * 9 / 5) + 32).toInt()}°F" else "${userSettings.idealTempMin.toInt()}°C"
                                val headerMax = if (userSettings.tempUnit == "F") "${((userSettings.idealTempMax * 9 / 5) + 32).toInt()}°F" else "${userSettings.idealTempMax.toInt()}°C"
                                Text(
                                    text = "Preference: $headerMin - $headerMax Ideal Range",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Quick stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProfileMetricPill(
                                label = "Saved Places",
                                value = "${uiState.savedCities.size}",
                                icon = Icons.Default.Bookmark
                            )
                            ProfileMetricPill(
                                label = "Living Weather",
                                value = if (userSettings.reducedMotion) "Static" else "Active",
                                icon = Icons.Default.Animation
                            )
                            ProfileMetricPill(
                                label = "Unit Mode",
                                value = if (userSettings.tempUnit == "F") "Imperial" else "Metric",
                                icon = Icons.Default.Speed
                            )
                        }
                    }
                }
            }

            // Quick Widgets Hub Launcher Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWidgets() }
                        .testTag("you_widgets_hub_button")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Widgets Studio",
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Home & Lockscreen Widgets Studio",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Customize 5 real-time Android widget form factors with frosted glass & OLED styling.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Widgets Studio",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Weather Comfort & Thermal Preferences
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("you_comfort_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Thermal Comfort & Ideal Range",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Text(
                            text = "WeatherFlow customizes rain planner alerts and outdoor activity scores according to your preferred comfort threshold.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )

                        // Ideal Temperature Range controls
                        val idealMinFormatted = if (userSettings.tempUnit == "F") "${((userSettings.idealTempMin * 9 / 5) + 32).toInt()}°F" else "${userSettings.idealTempMin.toInt()}°C"
                        val idealMaxFormatted = if (userSettings.tempUnit == "F") "${((userSettings.idealTempMax * 9 / 5) + 32).toInt()}°F" else "${userSettings.idealTempMax.toInt()}°C"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Minimum Preferred: $idealMinFormatted",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                                Slider(
                                    value = userSettings.idealTempMin,
                                    onValueChange = { newMin ->
                                        if (newMin < userSettings.idealTempMax) {
                                            viewModel.updateSettings(userSettings.copy(idealTempMin = newMin))
                                        }
                                    },
                                    valueRange = 5f..25f,
                                    steps = 19,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .testTag("slider_temp_min")
                                )
                            }

                            Column {
                                Text(
                                    text = "Maximum Preferred: $idealMaxFormatted",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                                Slider(
                                    value = userSettings.idealTempMax,
                                    onValueChange = { newMax ->
                                        if (newMax > userSettings.idealTempMin) {
                                            viewModel.updateSettings(userSettings.copy(idealTempMax = newMax))
                                        }
                                    },
                                    valueRange = 15f..38f,
                                    steps = 22,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .testTag("slider_temp_max")
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Dedicated Temperature Unit Toggle Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("temp_unit_toggle_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (userSettings.tempUnit == "F") "°F" else "°C",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF38BDF8)
                                                )
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Temperature Scale",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = if (userSettings.tempUnit == "F") "Fahrenheit (°F) active globally" else "Celsius (°C) active globally",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    // Switch toggle between Celsius and Fahrenheit
                                    Switch(
                                        checked = userSettings.tempUnit == "F",
                                        onCheckedChange = { isFahrenheit ->
                                            viewModel.updateSettings(userSettings.copy(tempUnit = if (isFahrenheit) "F" else "C"))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF0284C7),
                                            uncheckedThumbColor = Color(0xFF94A3B8),
                                            uncheckedTrackColor = Color(0xFF1E293B)
                                        ),
                                        modifier = Modifier
                                            .testTag("temp_unit_switch")
                                            .testTag("toggle_celsius_fahrenheit")
                                    )
                                }

                                // Interactive Segmented Buttons for quick 1-tap switching
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = userSettings.tempUnit == "C",
                                        onClick = { viewModel.updateSettings(userSettings.copy(tempUnit = "C")) },
                                        label = {
                                            Text(
                                                text = "Celsius (°C)",
                                                fontWeight = if (userSettings.tempUnit == "C") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = {
                                            if (userSettings.tempUnit == "C") {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0284C7),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chip_unit_c")
                                            .testTag("btn_celsius")
                                    )

                                    FilterChip(
                                        selected = userSettings.tempUnit == "F",
                                        onClick = { viewModel.updateSettings(userSettings.copy(tempUnit = "F")) },
                                        label = {
                                            Text(
                                                text = "Fahrenheit (°F)",
                                                fontWeight = if (userSettings.tempUnit == "F") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = {
                                            if (userSettings.tempUnit == "F") {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0284C7),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chip_unit_f")
                                            .testTag("btn_fahrenheit")
                                    )
                                }
                            }
                        }

                        // Wind Speed Measurement Units
                        Text(
                            text = "Wind Speed Units",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = userSettings.windUnit == "kmh",
                                onClick = { viewModel.updateSettings(userSettings.copy(windUnit = "kmh")) },
                                label = { Text("km/h") }
                            )
                            FilterChip(
                                selected = userSettings.windUnit == "mph",
                                onClick = { viewModel.updateSettings(userSettings.copy(windUnit = "mph")) },
                                label = { Text("mph") }
                            )
                            FilterChip(
                                selected = userSettings.windUnit == "ms",
                                onClick = { viewModel.updateSettings(userSettings.copy(windUnit = "ms")) },
                                label = { Text("m/s") }
                            )
                        }
                    }
                }
            }

            // MET Norway Condition Mapping & Living Weather Showcase
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("you_living_weather_service_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Living Weather Visual System",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Text(
                            text = "Standardized icon mapping from official MET Norway Locationforecast 2.0 symbol codes to dynamic ambient sky animations, vector icons, and Lottie states.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )

                        // Sample conditions grid from WeatherIconService
                        val sampleConditions = listOf(
                            "clearsky_day", "partlycloudy_day", "heavyrain",
                            "thunderstorm", "snow", "fog"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sampleConditions.forEach { code ->
                                val mapped = WeatherIconService.mapCondition(code)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = mapped.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mapped.displayName.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    )
                                }
                            }
                        }

                        // Living Motion Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reduced Motion",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                                Text(
                                    text = "Pauses particle sweeps & sky physics",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = userSettings.reducedMotion,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings(userSettings.copy(reducedMotion = checked))
                                },
                                modifier = Modifier.testTag("switch_reduced_motion")
                            )
                        }
                    }
                }
            }

            // Trust Center, Transparency & Data Audit Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("you_trust_audit_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Meteorological Trust & Integrity",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Text(
                            text = "WeatherFlow operates strictly with verified data sources. All hourly predictions cite provider model runs and cache freshness.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        AuditRow(label = "Primary Provider", value = "MET Norway (Meteorologisk Institutt)")
                        AuditRow(label = "Data Product", value = "Locationforecast 2.0 (Compact JSON)")
                        AuditRow(label = "Air Quality Provider", value = "Open-Meteo Air Quality / Copernicus CAMS")
                        AuditRow(label = "License", value = "Creative Commons BY 4.0")
                        AuditRow(label = "Provider Model Run", value = freshness?.providerUpdatedAtIso ?: "Recent model run")
                        AuditRow(label = "Client Cache Status", value = freshness?.latencyOrAgeText ?: "Live Cache")
                        AuditRow(label = "Active Temperature Scale", value = if (userSettings.tempUnit == "F") "Fahrenheit (°F)" else "Celsius (°C)")
                        AuditRow(label = "Privacy Protection", value = "Coordinates rounded to 4 decimals (~11m)")

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = onNavigateToTrustCenter,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_open_trust_center"),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Forecast Trust & Transparency Center",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Trigger Demo Safety Alert Button
                        OutlinedButton(
                            onClick = { viewModel.toggleDemoAlert() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_toggle_demo_alert"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.weatherData?.alerts.isNullOrEmpty()) "Simulate Civil Protection Severe Alert" else "Dismiss Active Severe Alert",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.28f),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun AuditRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 12.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        )
    }
}
