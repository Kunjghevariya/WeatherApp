package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AirQualityData

@Composable
fun AirQualityHealthWidget(
    airQuality: AirQualityData,
    modifier: Modifier = Modifier
) {
    if (!airQuality.isAvailable) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A).copy(alpha = 0.85f)),
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF64748B).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .testTag("air_quality_health_widget")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Air Quality Icon",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AIR QUALITY & HEALTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.1.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "UNAVAILABLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.30f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = airQuality.healthAdvisory,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                        Text(
                            text = "WeatherFlow does not display fabricated particulate figures. Sensor observations or atmospheric models are required.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "Provider: ${airQuality.dataSource}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
        return
    }

    val aqiColor = Color(airQuality.categoryColorHex)
    val animatedProgress by animateFloatAsState(
        targetValue = (airQuality.aqi / 300f).coerceIn(0.05f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "AqiProgress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A).copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, aqiColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .testTag("air_quality_health_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Air Quality Icon",
                        tint = aqiColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AIR QUALITY & HEALTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = aqiColor,
                            letterSpacing = 1.1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = aqiColor.copy(alpha = 0.2f),
                    border = ButtonDefaults.outlinedButtonBorder(true)
                ) {
                    Text(
                        text = airQuality.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = aqiColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Central AQI Gauge + Exercise score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Inactive background track (240 degree sweep)
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active gradient arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFFF59E0B),
                                    Color(0xFFEF4444),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            startAngle = 150f,
                            sweepAngle = 240f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${airQuality.aqi}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = "US AQI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Health Summary & Outdoor Exercise Score
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Primary Pollutant:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                        Text(
                            text = airQuality.primaryPollutant,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Outdoor Exercise Score Bar
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Outdoor Cardio Safety",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 11.sp)
                            )
                            Text(
                                text = "${airQuality.outdoorExerciseScore}/100",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (airQuality.outdoorExerciseScore > 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { airQuality.outdoorExerciseScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (airQuality.outdoorExerciseScore > 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Detailed Pollutants Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PollutantPill("PM2.5", airQuality.pm25?.let { "$it µg/m³" } ?: "N/A", Modifier.weight(1f))
                PollutantPill("PM10", airQuality.pm10?.let { "$it µg/m³" } ?: "N/A", Modifier.weight(1f))
                PollutantPill("O₃", airQuality.o3?.let { "$it µg/m³" } ?: "N/A", Modifier.weight(1f))
                PollutantPill("NO₂", airQuality.no2?.let { "$it µg/m³" } ?: "N/A", Modifier.weight(1f))
            }

            // Health Guidance Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "HEALTH & SENSITIVE GROUPS ADVISORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Text(
                        text = airQuality.healthAdvisory,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                    if (airQuality.sensitiveGroupsAdvisory.isNotEmpty()) {
                        Text(
                            text = "• ${airQuality.sensitiveGroupsAdvisory}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Data Source: ${airQuality.dataSource}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PollutantPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
