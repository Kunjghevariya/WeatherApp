package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.WeatherConditionType
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.getConditionEmoji

enum class WidgetTheme(val displayName: String, val gradient: List<Color>, val borderColor: Color) {
    FROSTED("Frosted Glass", listOf(Color(0xFF1E293B).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.9f)), Color(0xFF38BDF8).copy(alpha = 0.3f)),
    OLED("Obsidian OLED", listOf(Color(0xFF000000), Color(0xFF050505)), Color(0xFF334155)),
    AURORA("Cosmic Aurora", listOf(Color(0xFF1E1B4B).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.9f)), Color(0xFFA855F7).copy(alpha = 0.4f)),
    CYBER("Cyber Neon", listOf(Color(0xFF082F49).copy(alpha = 0.9f), Color(0xFF0C1929).copy(alpha = 0.95f)), Color(0xFF06B6D4).copy(alpha = 0.45f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsHubScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val userSettings = uiState.userSettings
    val current = weatherData?.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("weather_widget_cache", android.content.Context.MODE_PRIVATE) }
    val initialTheme = when (prefs.getString("widget_theme", "frosted")) {
        "oled", "obsidian" -> WidgetTheme.OLED
        "aurora", "cosmic" -> WidgetTheme.AURORA
        "cyber" -> WidgetTheme.CYBER
        else -> WidgetTheme.FROSTED
    }
    val initialOpacity = prefs.getFloat("widget_opacity", 0.85f)
    val initialForm = prefs.getInt("widget_form_factor", 1)

    var selectedWidgetIndex by remember { mutableIntStateOf(initialForm) } // 0: 1x1, 1: 4x1, 2: 2x2, 3: 4x2, 4: 3x3 Radar
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var widgetOpacity by remember { mutableFloatStateOf(initialOpacity) }
    var showAddedConfirmation by remember { mutableStateOf(false) }

    val displayTemp = current?.let {
        if (userSettings.tempUnit == "F") "${((it.temperatureC * 9 / 5) + 32).toInt()}°"
        else "${it.temperatureC.toInt()}°"
    } ?: "21°"

    val displayFeelsLike = current?.let {
        if (userSettings.tempUnit == "F") "${((it.feelsLikeC * 9 / 5) + 32).toInt()}°"
        else "${it.feelsLikeC.toInt()}°"
    } ?: "20°"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android Widgets Studio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("widgets_hub_back_button")
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
                .testTag("widgets_hub_scroll"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = ButtonDefaults.outlinedButtonBorder(true),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                text = "HOME SCREEN WIDGET SUITE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 1.1.sp
                                )
                            )
                        }
                        Text(
                            text = "Preview and customize high-fidelity Android widgets with live MET Norway atmospheric telemetry, transparent glass styles, and dynamic layouts.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            // Widget Size Selector Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SELECT WIDGET FORM FACTOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sizes = listOf("1x1 Capsule", "4x1 Pill", "2x2 Radial", "4x2 Matrix", "3x3 Radar")
                        sizes.forEachIndexed { index, label ->
                            val isSelected = selectedWidgetIndex == index
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0284C7) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedWidgetIndex = index
                                        com.example.widget.WidgetManager.saveCustomization(
                                            context,
                                            selectedTheme.name.lowercase(),
                                            widgetOpacity,
                                            index
                                        )
                                    }
                                    .testTag("widget_size_tab_$index")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Live Interactive Preview Box
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE HOMESCREEN PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${(widgetOpacity * 100).toInt()}% Opacity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Simulated Android Home Screen Wall & Widget Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF042F2E), Color(0xFF0F172A), Color(0xFF1E1B4B))
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val backgroundBrush = Brush.linearGradient(
                            selectedTheme.gradient.map { it.copy(alpha = widgetOpacity) }
                        )

                        when (selectedWidgetIndex) {
                            0 -> {
                                // 1x1 Lock Screen Capsule
                                Surface(
                                    shape = RoundedCornerShape(28.dp),
                                    color = Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder(true),
                                    modifier = Modifier
                                        .size(110.dp)
                                        .background(backgroundBrush, RoundedCornerShape(28.dp))
                                        .testTag("preview_widget_1x1")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = getConditionEmoji(current?.conditionType ?: WeatherConditionType.CLEAR_DAY), fontSize = 28.sp)
                                        Text(
                                            text = displayTemp,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = uiState.currentCity.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // 4x1 Smart Hourly Pill
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder(true),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundBrush, RoundedCornerShape(24.dp))
                                        .testTag("preview_widget_4x1")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(text = getConditionEmoji(current?.conditionType ?: WeatherConditionType.CLEAR_DAY), fontSize = 32.sp)
                                            Column {
                                                Text(
                                                    text = uiState.currentCity.name,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = current?.conditionLabel ?: "Clear",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = displayTemp,
                                            style = MaterialTheme.typography.displaySmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 32.sp
                                            )
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // 2x2 Dual Radial Glance
                                Surface(
                                    shape = RoundedCornerShape(26.dp),
                                    color = Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder(true),
                                    modifier = Modifier
                                        .size(190.dp)
                                        .background(backgroundBrush, RoundedCornerShape(26.dp))
                                        .testTag("preview_widget_2x2")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = uiState.currentCity.name,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(text = getConditionEmoji(current?.conditionType ?: WeatherConditionType.CLEAR_DAY), fontSize = 22.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = displayTemp,
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 42.sp
                                                )
                                            )
                                            Text(
                                                text = "Feels like $displayFeelsLike",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("💧 ${current?.humidityPercent ?: 65}%", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("💨 ${current?.windSpeedKmh?.toInt() ?: 12} km/h", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // 4x2 Deep Weather Matrix
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder(true),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundBrush, RoundedCornerShape(24.dp))
                                        .testTag("preview_widget_4x2")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${uiState.currentCity.name}, ${uiState.currentCity.country}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = current?.conditionLabel ?: "Clear",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                            Text(
                                                text = displayTemp,
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 38.sp
                                                )
                                            )
                                        }

                                        // Mini Hourly Strip inside 4x2 widget
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            weatherData?.hourly?.take(5)?.forEach { hp ->
                                                val hTemp = if (userSettings.tempUnit == "F") "${((hp.temperatureC * 9 / 5) + 32).toInt()}°" else "${hp.temperatureC.toInt()}°"
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(hp.hourLabel, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                    Text(getConditionEmoji(hp.conditionType), fontSize = 16.sp)
                                                    Text(hTemp, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            4 -> {
                                // 3x3 Doppler Radar Widget
                                Surface(
                                    shape = RoundedCornerShape(26.dp),
                                    color = Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder(true),
                                    modifier = Modifier
                                        .size(240.dp)
                                        .background(backgroundBrush, RoundedCornerShape(26.dp))
                                        .testTag("preview_widget_3x3_radar")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "RADAR: ${uiState.currentCity.name.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF38BDF8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text("LIVE", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFF030B17)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("⚡ Doppler Storm Sweep Active", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(displayTemp, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Rain chance: ${weatherData?.rainWindow?.peakProbability ?: 0}%", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Theme Options
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "WIDGET COLOR THEME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetTheme.values().forEach { theme ->
                            val isSelected = selectedTheme == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0284C7) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTheme = theme
                                        com.example.widget.WidgetManager.saveCustomization(
                                            context,
                                            theme.name.lowercase(),
                                            widgetOpacity,
                                            selectedWidgetIndex
                                        )
                                        showAddedConfirmation = true
                                    }
                                    .testTag("widget_theme_${theme.name.lowercase()}")
                            ) {
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Background Opacity Slider
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Transparency / Glass Opacity",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${(widgetOpacity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            )
                        }

                        Slider(
                            value = widgetOpacity,
                            onValueChange = {
                                widgetOpacity = it
                                com.example.widget.WidgetManager.saveCustomization(
                                    context,
                                    selectedTheme.name.lowercase(),
                                    it,
                                    selectedWidgetIndex
                                )
                            },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF0284C7),
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("widget_hub_opacity_slider")
                        )
                    }
                }
            }

            // Action Buttons: Add / Pin Widget & Apply to active widgets
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val formName = when (selectedWidgetIndex) {
                        0 -> "1x1 Quick Capsule"
                        1 -> "4x1 Horizontal Pill"
                        else -> "4x2 Detailed Matrix"
                    }

                    Button(
                        onClick = {
                            com.example.widget.WidgetManager.saveCustomization(
                                context,
                                selectedTheme.name.lowercase(),
                                widgetOpacity,
                                selectedWidgetIndex
                            )
                            com.example.widget.WidgetManager.requestPinWidget(context, selectedWidgetIndex)
                            showAddedConfirmation = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_widget_to_homescreen_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddHome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add $formName to Home Screen",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            com.example.widget.WidgetManager.saveCustomization(
                                context,
                                selectedTheme.name.lowercase(),
                                widgetOpacity,
                                selectedWidgetIndex
                            )
                            showAddedConfirmation = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_widget_theme_button")
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply ${selectedTheme.displayName} to Active Widgets",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Success Confirmation Banner
            if (showAddedConfirmation) {
                item {
                    val formName = when (selectedWidgetIndex) {
                        0 -> "1x1 Quick Capsule"
                        1 -> "4x1 Horizontal Pill"
                        else -> "4x2 Detailed Forecast Matrix"
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = ButtonDefaults.outlinedButtonBorder(true),
                        modifier = Modifier.fillMaxWidth().testTag("widget_added_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Column {
                                Text(
                                    text = "Widget Successfully Configured!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$formName with ${selectedTheme.displayName} (${(widgetOpacity * 100).toInt()}% opacity) is saved and applied to your launcher.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
