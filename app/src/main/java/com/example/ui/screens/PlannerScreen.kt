package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.domain.ActivityCalculator
import com.example.domain.ActivityType
import com.example.domain.WeatherIconService
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.getActivityEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val settings = uiState.userSettings

    var minTemp by remember(settings.idealTempMin) { mutableStateOf(settings.idealTempMin) }
    var maxTemp by remember(settings.idealTempMax) { mutableStateOf(settings.idealTempMax) }
    var maxWind by remember(settings.maxWindTolerance) { mutableStateOf(settings.maxWindTolerance) }
    var maxRain by remember(settings.maxRainTolerance) { mutableStateOf(settings.maxRainTolerance) }

    val effectiveSettings = settings.copy(
        idealTempMin = minTemp,
        idealTempMax = maxTemp,
        maxWindTolerance = maxWind,
        maxRainTolerance = maxRain
    )

    val recommendations = if (weatherData != null) {
        ActivityCalculator.evaluateActivities(
            current = weatherData.current,
            hourly = weatherData.hourly,
            userSettings = effectiveSettings
        )
    } else emptyList()

    val currentRec = recommendations.firstOrNull { it.activity == uiState.selectedActivity }
        ?: recommendations.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Best Time to Go Outside",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("planner_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("planner_screen_list"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Weather-Aware Decision Engine",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        )
                        Text(
                            text = "Stop manually guessing temperature, wind, and rain risk. Select your activity below to see personalized suitability scores and recommended time windows.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }

            // Activity preset selector chips
            item {
                Text(
                    text = "SELECT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("planner_activity_chips")
                ) {
                    items(ActivityType.values()) { act ->
                        val isSelected = act == uiState.selectedActivity
                        val chipBg = if (isSelected) Color(0xFF0284C7) else Color(0xFF132238).copy(alpha = 0.8f)
                        val textColor = if (isSelected) Color.White else Color(0xFFCBD5E1)

                        Row(
                            modifier = Modifier
                                .background(chipBg, RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.selectActivity(act) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("planner_chip_${act.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = getActivityEmoji(act), fontSize = 16.sp)
                            Text(
                                text = act.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // Current Recommendation Card
            if (currentRec != null) {
                item {
                    val ratingColor = when (currentRec.ratingLabel) {
                        "Optimal" -> Color(0xFF10B981)
                        "Good" -> Color(0xFF38BDF8)
                        "Moderate" -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF132238).copy(alpha = 0.88f),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_recommendation_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    Text(text = getActivityEmoji(currentRec.activity), fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = currentRec.activity.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 18.sp
                                            )
                                        )
                                        Text(
                                            text = "Best Window: ${currentRec.bestWindowLabel}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ratingColor.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(ratingColor, CircleShape)
                                        )
                                        Text(
                                            text = "${currentRec.scorePercent}% ${currentRec.ratingLabel}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ratingColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currentRec.currentSuitabilityText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp
                                )
                            )

                            // MET Norway Current Condition Context with Material Icon
                            val currentSymbol = weatherData?.current?.symbolCode ?: "clearsky_day"
                            val conditionName = weatherData?.current?.conditionLabel ?: "Clear Sky"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.28f),
                                border = ButtonDefaults.outlinedButtonBorder(true),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("planner_weather_context_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = WeatherIconService.getStandardizedIcon(currentSymbol),
                                        contentDescription = conditionName,
                                        tint = WeatherIconService.getIconTint(currentSymbol),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("planner_weather_icon")
                                    )
                                    Column {
                                        Text(
                                            text = "MET Norway Outlook: $conditionName",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Recommended Window: ${currentRec.bestWindowLabel}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Text(
                                text = "FORECAST FACTORS BREAKDOWN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                currentRec.factors.forEach { factor ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (factor.isPositive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = factor.text,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (factor.isPositive) Color(0xFFCBD5E1) else Color(0xFFFDE68A),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Personal Preference Tuning Panel
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF132238).copy(alpha = 0.88f),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_preference_tuning_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Your Weather Comfort Limits",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.updateSettings(effectiveSettings)
                                },
                                modifier = Modifier.testTag("save_preferences_button")
                            ) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Ideal Min Temp Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Min Comfortable Temperature",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )
                                val isF = settings.tempUnit == "F"
                                val displayMin = if (isF) "${((minTemp * 9 / 5) + 32).toInt()}°F" else "${minTemp.toInt()}°C"
                                Text(
                                    text = displayMin,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = minTemp,
                                onValueChange = { minTemp = it },
                                valueRange = 0f..25f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }

                        // Ideal Max Temp Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Max Comfortable Temperature",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )
                                val displayMax = if (settings.tempUnit == "F") "${((maxTemp * 9 / 5) + 32).toInt()}°F" else "${maxTemp.toInt()}°C"
                                Text(
                                    text = displayMax,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = maxTemp,
                                onValueChange = { maxTemp = it },
                                valueRange = 18f..38f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }

                        // Max Wind Comfort Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Wind Tolerance Limit",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )
                                Text(
                                    text = "${maxWind.toInt()} km/h",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = maxWind,
                                onValueChange = { maxWind = it },
                                valueRange = 10f..50f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }

                        // Max Rain Tolerance Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rain Probability Tolerance",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )
                                Text(
                                    text = "${maxRain.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = maxRain,
                                onValueChange = { maxRain = it },
                                valueRange = 0f..50f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
