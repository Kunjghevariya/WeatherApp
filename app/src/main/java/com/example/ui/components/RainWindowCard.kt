package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.domain.RainWindow

@Composable
fun RainWindowCard(
    rainWindow: RainWindow,
    precipUnit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rain_window_planner_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF132238).copy(alpha = 0.85f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Precipitation Planner Icon",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Rain Start & Stop Planner",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Upcoming Precipitation Windows",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Confidence badge
                val confidenceBg = if (rainWindow.confidenceRating.contains("High")) {
                    Color(0xFF065F46) // Deep Emerald
                } else {
                    Color(0xFF78350F) // Deep Amber
                }
                Text(
                    text = rainWindow.confidenceRating,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .background(confidenceBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Headline
            Text(
                text = rainWindow.summaryHeadline,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            )

            // Visual metrics bar
            if (rainWindow.hasRain) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Expected Window",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                        Text(
                            text = "${rainWindow.startHour} – ${rainWindow.endHour}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Peak Probability",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                        Text(
                            text = "${rainWindow.peakProbability}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Estimated Volume",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                        val displayAmount = if (precipUnit == "in") {
                            String.format(java.util.Locale.US, "%.2f in", rainWindow.totalEstimatedMm * 0.03937)
                        } else {
                            String.format(java.util.Locale.US, "%.1f mm", rainWindow.totalEstimatedMm)
                        }
                        Text(
                            text = displayAmount,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }

            // Detailed reasoning
            Text(
                text = rainWindow.detailedReasoning,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            )

            // Transparency disclaimer
            Text(
                text = "Honest Forecast: Timings reflect MET Norway hourly numerical prediction steps. Localized convective showers may start slightly before or after hourly marks.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            )
        }
    }
}
