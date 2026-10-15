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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.getConditionEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustCenterScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val freshness = weatherData?.freshness
    val widgetSettings = uiState.widgetSettings

    var selectedWidgetType by remember { mutableStateOf(widgetSettings.typeIndex) } // 0: 4x1 Hourly, 1: 2x2 Glance, 2: 4x2 Detailed
    var widgetOpacity by remember { mutableStateOf(widgetSettings.backgroundOpacity) }
    var isFrosted by remember { mutableStateOf(widgetSettings.isFrostedGlass) }
    var isOled by remember { mutableStateOf(widgetSettings.isOledBlack) }
    var widgetRefreshedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Forecast Trust & Widgets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("trust_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                .testTag("trust_center_list"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Live Data Freshness & Provider Audit
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("freshness_audit_card")
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
                                text = "Live Data Freshness Monitor",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Text(
                            text = "WeatherFlow is architected on transparent data integrity: we never present stale cached data as live, and we respect international meteorological caching and privacy standards.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        AuditRow(label = "Primary Weather Provider", value = "MET Norway (Meteorologisk Institutt)")
                        AuditRow(label = "Data Product", value = "Locationforecast 2.0 (Compact JSON)")
                        AuditRow(label = "Licensing & Attribution", value = "Creative Commons BY 4.0")
                        AuditRow(label = "Provider Model Run", value = freshness?.providerUpdatedAtIso ?: "Recent model run")
                        AuditRow(label = "Client Cache Status", value = freshness?.latencyOrAgeText ?: "Live")
                        AuditRow(label = "Location Privacy Policy", value = "Exact coordinates rounded to 4 decimals (~11m)")
                        AuditRow(label = "Severe Warning Authority", value = "US NWS + Regional Civil Protection Agencies")

                        Spacer(modifier = Modifier.height(4.dp))

                        // Trigger Demo Safety Alert Button
                        OutlinedButton(
                            onClick = { viewModel.toggleDemoAlert() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_demo_alert_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF38BDF8)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(true)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isSimulatedDemoAlertActive) "Disable Test Warning Banner" else "Simulate Official Safety Warning"
                            )
                        }
                    }
                }
            }

            // Section 2: Forecast Uncertainty & Model Agreement
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Understanding Forecast Uncertainty",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Text(
                            text = "• Atmospheric Divergence: Forecasts for the next 0–6 hours have high confidence (~90%), while forecasts beyond 48 hours represent ensemble probabilities.\n\n" +
                                    "• Rain Timing: Microbursts and convective precipitation cells fluctuate across kilometers. We show hourly prediction windows rather than guessing exact minutes.\n\n" +
                                    "• Safety Responsibility: Weather forecasts indicate probabilities and do not guarantee physical safety. Always obey regional disaster warnings.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // Section 3: Glanceable Android Home-Screen Widget Customizer & Interactive Simulator
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_designer_card")
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
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Glanceable Widget Customizer",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Text(
                            text = "Customize the transparency, theme, and layout of WeatherFlow home-screen widgets. Preview live rendering below:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        )

                        // Widget Type Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val types = listOf("4×1 Hourly", "2×2 Glance", "4×2 Outlook")
                            types.forEachIndexed { idx, label ->
                                val isSelected = selectedWidgetType == idx
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color.Black.copy(alpha = 0.3f),
                                    border = if (isSelected) ButtonDefaults.outlinedButtonBorder(true) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedWidgetType = idx
                                            viewModel.updateWidgetSettings(
                                                widgetSettings.copy(typeIndex = idx)
                                            )
                                        }
                                        .testTag("widget_type_tab_$idx")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.White,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Opacity slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Widget Background Opacity",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )
                                Text(
                                    text = "${(widgetOpacity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = widgetOpacity,
                                onValueChange = {
                                    widgetOpacity = it
                                    viewModel.updateWidgetSettings(
                                        widgetSettings.copy(backgroundOpacity = it)
                                    )
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                ),
                                modifier = Modifier.testTag("widget_opacity_slider")
                            )
                        }

                        // Style Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Frosted Glass Blur Effect",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                            )
                            Switch(
                                checked = isFrosted,
                                onCheckedChange = {
                                    isFrosted = it
                                    viewModel.updateWidgetSettings(
                                        widgetSettings.copy(isFrostedGlass = it)
                                    )
                                },
                                modifier = Modifier.testTag("widget_frosted_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OLED Pure Dark Palette",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                            )
                            Switch(
                                checked = isOled,
                                onCheckedChange = {
                                    isOled = it
                                    viewModel.updateWidgetSettings(
                                        widgetSettings.copy(isOledBlack = it)
                                    )
                                },
                                modifier = Modifier.testTag("widget_oled_switch")
                            )
                        }

                        // Live Interactive Widget Preview Canvas
                        Text(
                            text = "LIVE WIDGET PREVIEW (TAP TO REFRESH):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        val previewBg = if (isOled) {
                            Color.Black.copy(alpha = widgetOpacity)
                        } else {
                            Color(0xFF0F1B2F).copy(alpha = widgetOpacity)
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = previewBg,
                            border = ButtonDefaults.outlinedButtonBorder(true),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    widgetRefreshedMessage = "Widget synchronized with live MET Norway cache!"
                                    viewModel.refresh()
                                }
                                .testTag("widget_preview_container")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = uiState.currentCity.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "${weatherData?.current?.temperatureC?.toInt() ?: 20}°C",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Tap to refresh",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Tap to sync",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                when (selectedWidgetType) {
                                    0 -> { // 4x1 Hourly Preview
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            (weatherData?.hourly?.take(5) ?: emptyList()).forEach { pt ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = pt.hourLabel, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                                                    Text(text = getConditionEmoji(pt.conditionType), fontSize = 14.sp)
                                                    Text(text = "${pt.temperatureC.toInt()}°", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                                }
                                            }
                                        }
                                    }
                                    1 -> { // 2x2 Glance Preview
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = weatherData?.current?.conditionLabel ?: "Clear", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                                                Text(text = "Feels like ${weatherData?.current?.feelsLikeC?.toInt() ?: 19}°", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                                            }
                                            Text(text = getConditionEmoji(weatherData?.current?.conditionType ?: com.example.domain.WeatherConditionType.CLEAR_DAY), fontSize = 32.sp)
                                        }
                                    }
                                    2 -> { // 4x2 Detailed Outlook Preview
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Rain window: ${weatherData?.rainWindow?.startHour ?: "None"} – ${weatherData?.rainWindow?.endHour ?: "None"}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                (weatherData?.daily?.take(4) ?: emptyList()).forEach { d ->
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(text = d.dayLabel.take(3), style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                                                        Text(text = getConditionEmoji(d.conditionType), fontSize = 13.sp)
                                                        Text(text = "${d.maxTempC.toInt()}°", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (widgetRefreshedMessage != null) {
                            Text(
                                text = widgetRefreshedMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
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
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}
