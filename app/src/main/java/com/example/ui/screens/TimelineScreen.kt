package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
fun TimelineScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val hourlyList = weatherData?.hourly ?: emptyList()
    val tempUnit = uiState.userSettings.tempUnit
    val windUnit = uiState.userSettings.windUnit

    var inspectedHourIndex by remember { mutableStateOf(uiState.selectedTimeTravelHourIndex ?: 0) }
    val inspectedPoint = hourlyList.getOrNull(inspectedHourIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "24-Hour Timeline & Chart",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("timeline_back_button")
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
                .testTag("timeline_screen_list"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Inspector Banner for tapped hour
            if (inspectedPoint != null) {
                item {
                    val displayTemp = if (tempUnit == "F") {
                        "${((inspectedPoint.temperatureC * 9 / 5) + 32).toInt()}°F"
                    } else {
                        "${inspectedPoint.temperatureC.toInt()}°C"
                    }

                    val displayWind = when (windUnit) {
                        "mph" -> "${(inspectedPoint.windSpeedKmh * 0.621371).toInt()} mph"
                        "ms" -> "${(inspectedPoint.windSpeedKmh / 3.6).toInt()} m/s"
                        else -> "${inspectedPoint.windSpeedKmh.toInt()} km/h"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.35f),
                        border = ButtonDefaults.outlinedButtonBorder(true),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inspected_hour_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.25f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = WeatherIconService.getStandardizedIcon(inspectedPoint.symbolCode),
                                                contentDescription = "Inspected Condition",
                                                tint = WeatherIconService.getIconTint(inspectedPoint.symbolCode),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("timeline_inspected_weather_icon")
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Forecast for ${inspectedPoint.hourLabel}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = if (inspectedPoint.isDaylight) "Daylight hours" else "Night conditions",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBAE6FD))
                                        )
                                    }
                                }

                                Text(
                                    text = displayTemp,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Precipitation", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBAE6FD)))
                                    Text(
                                        text = "${inspectedPoint.precipProbability}% (${String.format(java.util.Locale.US, "%.1f", inspectedPoint.precipAmountMm)} mm)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Wind", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBAE6FD)))
                                    Text(
                                        text = "$displayWind ${inspectedPoint.windCardinal}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Humidity", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBAE6FD)))
                                    Text(
                                        text = "${inspectedPoint.humidityPercent}%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sunrise & Sunset Markers Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Sunrise", tint = Color(0xFFFBBF24))
                            Column {
                                Text(text = "Sunrise", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                                Text(
                                    text = weatherData?.astronomy?.sunriseTime ?: "--:--",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                        }

                        VerticalDivider(
                            modifier = Modifier.height(30.dp),
                            thickness = 1.dp,
                            color = Color.White.copy(alpha = 0.15f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NightsStay, contentDescription = "Sunset", tint = Color(0xFF818CF8))
                            Column {
                                Text(text = "Sunset", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                                Text(
                                    text = weatherData?.astronomy?.sunsetTime ?: "--:--",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // 24 Hour Chronological List with Tap-to-Inspect
            item {
                Text(
                    text = "HOURLY SEQUENCE (TAP ANY HOUR TO INSPECT)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            itemsIndexed(hourlyList.take(24)) { index, point ->
                val isSelected = inspectedHourIndex == index
                val itemBg = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFF132238).copy(alpha = 0.82f)

                val displayTemp = if (tempUnit == "F") {
                    "${((point.temperatureC * 9 / 5) + 32).toInt()}°"
                } else {
                    "${point.temperatureC.toInt()}°"
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = itemBg,
                    border = if (isSelected) ButtonDefaults.outlinedButtonBorder(true) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            inspectedHourIndex = index
                            viewModel.setTimeTravelHour(index)
                        }
                        .testTag("timeline_hour_row_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = point.hourLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.width(52.dp)
                            )
                            Icon(
                                imageVector = WeatherIconService.getStandardizedIcon(point.symbolCode),
                                contentDescription = point.conditionType.name,
                                tint = WeatherIconService.getIconTint(point.symbolCode),
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("timeline_row_icon_$index")
                            )
                            Column {
                                Text(
                                    text = displayTemp,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${point.windCardinal} ${point.windSpeedKmh.toInt()} km/h",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Rain probability & volume
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (point.precipProbability > 0) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${point.precipProbability}%",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                )
                            } else {
                                Text(
                                    text = "0%",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(point.windDirectionDeg.toFloat())
                            )
                        }
                    }
                }
            }
        }
    }
}
