package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.HourlyPoint

@Composable
fun TimeTravelScrubber(
    hourlyList: List<HourlyPoint>,
    selectedHourIndex: Int?,
    tempUnit: String,
    onHourChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIndex = selectedHourIndex ?: 0
    val currentPoint = hourlyList.getOrNull(activeIndex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("time_travel_scrubber"),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F1B2F).copy(alpha = 0.82f),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time Travel Clock",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Time Travel Slider",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }

                if (selectedHourIndex != null) {
                    TextButton(
                        onClick = { onHourChanged(null) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("reset_time_travel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to live time",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back to Live",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8))
                        )
                    }
                }
            }

            // Slider to scrub across 24 hours
            val maxHours = (hourlyList.size.coerceAtMost(24) - 1).coerceAtLeast(1)
            Slider(
                value = activeIndex.toFloat(),
                onValueChange = { onHourChanged(it.toInt()) },
                valueRange = 0f..maxHours.toFloat(),
                steps = maxHours - 1,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("time_travel_slider")
            )

            // Scrubber details snapshot
            if (currentPoint != null) {
                val displayTemp = if (tempUnit == "F") {
                    "${((currentPoint.temperatureC * 9 / 5) + 32).toInt()}°F"
                } else {
                    "${currentPoint.temperatureC.toInt()}°C"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedHourIndex == null) "Current: Now" else "Projected: ${currentPoint.hourLabel}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getConditionEmoji(currentPoint.conditionType),
                            fontSize = 16.sp
                        )
                        Text(
                            text = displayTemp,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Rain: ${currentPoint.precipProbability}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }
    }
}
