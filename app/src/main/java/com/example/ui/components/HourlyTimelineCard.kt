package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.HourlyPoint
import com.example.domain.WeatherConditionType
import com.example.domain.WeatherIconService

@Composable
fun HourlyTimelineCard(
    hourlyList: List<HourlyPoint>,
    selectedHourIndex: Int?,
    tempUnit: String,
    windUnit: String,
    onHourSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_timeline_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF132238).copy(alpha = 0.85f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-Hour Timeline",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = if (selectedHourIndex != null) "Hour selected • Tap to inspect" else "Tap hour to inspect",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("hourly_timeline_row")
            ) {
                itemsIndexed(hourlyList.take(24)) { index, point ->
                    val isSelected = selectedHourIndex == index

                    val displayTemp = if (tempUnit == "F") {
                        "${((point.temperatureC * 9 / 5) + 32).toInt()}°"
                    } else {
                        "${point.temperatureC.toInt()}°"
                    }

                    val displayWind = when (windUnit) {
                        "mph" -> "${(point.windSpeedKmh * 0.621371).toInt()} mph"
                        "ms" -> "${(point.windSpeedKmh / 3.6).toInt()} m/s"
                        else -> "${point.windSpeedKmh.toInt()} km/h"
                    }

                    val itemBg = if (isSelected) {
                        Color(0xFF0284C7).copy(alpha = 0.45f)
                    } else {
                        Color.Black.copy(alpha = 0.22f)
                    }

                    val borderColor = if (isSelected) Color(0xFF38BDF8) else Color.Transparent

                    Column(
                        modifier = Modifier
                            .width(68.dp)
                            .background(itemBg, RoundedCornerShape(16.dp))
                            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable { onHourSelected(index) }
                            .padding(vertical = 12.dp, horizontal = 6.dp)
                            .testTag("hourly_item_$index"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Hour label
                        Text(
                            text = if (index == 0) "Now" else point.hourLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )

                        // Standardized Material weather icon converted from MET Norway symbolCode
                        Icon(
                            imageVector = WeatherIconService.getStandardizedIcon(point.symbolCode),
                            contentDescription = point.conditionType.name,
                            tint = WeatherIconService.getIconTint(point.symbolCode),
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("hourly_icon_$index")
                        )

                        // Temperature
                        Text(
                            text = displayTemp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )

                        // Rain probability bar or indicator
                        if (point.precipProbability > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Rain",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${point.precipProbability}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "0%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Wind direction arrow & speed
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Wind direction",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(10.dp)
                                    .rotate(point.windDirectionDeg.toFloat())
                            )
                            Text(
                                text = displayWind,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getConditionEmoji(type: WeatherConditionType): String {
    return when (type) {
        WeatherConditionType.CLEAR_DAY -> "☀️"
        WeatherConditionType.CLEAR_NIGHT -> "🌙"
        WeatherConditionType.PARTLY_CLOUDY_DAY -> "⛅"
        WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "☁️"
        WeatherConditionType.CLOUDY -> "☁️"
        WeatherConditionType.LIGHT_RAIN -> "🌦️"
        WeatherConditionType.HEAVY_RAIN -> "🌧️"
        WeatherConditionType.THUNDERSTORM -> "⛈️"
        WeatherConditionType.SNOW -> "❄️"
        WeatherConditionType.SLEET -> "🌨️"
        WeatherConditionType.FOG -> "🌫️"
        WeatherConditionType.WINDY -> "💨"
    }
}
