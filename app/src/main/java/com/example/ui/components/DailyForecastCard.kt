package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DailyPoint
import com.example.domain.WeatherIconService

@Composable
fun DailyForecastCard(
    dailyList: List<DailyPoint>,
    tempUnit: String,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    val minGlobal = dailyList.minOfOrNull { it.minTempC } ?: 10.0
    val maxGlobal = dailyList.maxOfOrNull { it.maxTempC } ?: 30.0
    val totalRange = (maxGlobal - minGlobal).coerceAtLeast(1.0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF132238).copy(alpha = 0.85f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "7-Day Outlook",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dailyList.forEachIndexed { idx, day ->
                    val minFormatted = if (tempUnit == "F") {
                        "${((day.minTempC * 9 / 5) + 32).toInt()}°"
                    } else {
                        "${day.minTempC.toInt()}°"
                    }

                    val maxFormatted = if (tempUnit == "F") {
                        "${((day.maxTempC * 9 / 5) + 32).toInt()}°"
                    } else {
                        "${day.maxTempC.toInt()}°"
                    }

                    // Progress bar offsets for temperature range
                    val leftFraction = ((day.minTempC - minGlobal) / totalRange).toFloat().coerceIn(0f, 0.8f)
                    val widthFraction = ((day.maxTempC - day.minTempC) / totalRange).toFloat().coerceIn(0.2f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_item_$idx"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Day label & date
                        Column(modifier = Modifier.width(78.dp)) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = day.dateDisplay,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Condition icon + rain chance
                        Row(
                            modifier = Modifier.width(80.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = WeatherIconService.getStandardizedIcon(day.symbolCode),
                                contentDescription = day.conditionLabel,
                                tint = WeatherIconService.getIconTint(day.symbolCode),
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("daily_icon_$idx")
                            )
                            if (day.precipProbability > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "${day.precipProbability}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Min temp
                        Text(
                            text = minFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.width(32.dp)
                        )

                        // Visual horizontal temp bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(widthFraction)
                                    .background(
                                        Color(0xFF38BDF8),
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                        }

                        // Max temp
                        Text(
                            text = maxFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .width(36.dp)
                                .padding(start = 8.dp)
                        )
                    }

                    if (idx < dailyList.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
