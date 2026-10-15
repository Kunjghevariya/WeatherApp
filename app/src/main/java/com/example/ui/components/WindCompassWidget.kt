package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CurrentWeather
import com.example.domain.WindDynamics
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindCompassWidget(
    current: CurrentWeather,
    windDynamics: WindDynamics,
    windUnit: String,
    modifier: Modifier = Modifier
) {
    val displaySpeed = when (windUnit) {
        "mph" -> "${(current.windSpeedKmh * 0.621371).toInt()} mph"
        "ms" -> "${(current.windSpeedKmh / 3.6).toInt()} m/s"
        else -> "${current.windSpeedKmh.toInt()} km/h"
    }

    val displayGust = when (windUnit) {
        "mph" -> "${(windDynamics.gustSpeedKmh * 0.621371).toInt()} mph"
        "ms" -> "${(windDynamics.gustSpeedKmh / 3.6).toInt()} m/s"
        else -> "${windDynamics.gustSpeedKmh.toInt()} km/h"
    }

    val animatedRotation by animateFloatAsState(
        targetValue = current.windDirectionDeg.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "WindNeedle"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1929).copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .testTag("wind_compass_widget")
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
                        contentDescription = "Wind Direction",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "WIND DYNAMICS & COMPASS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "Beaufort F${windDynamics.beaufortScale}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF7DD3FC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Central Compass + Speed metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive 360 Compass Dial
                Box(
                    modifier = Modifier.size(125.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 6.dp.toPx()

                        // Outer ring
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 12 Compass Tick Marks (every 30 degrees)
                        for (i in 0 until 12) {
                            val angleRad = Math.toRadians((i * 30).toDouble())
                            val innerR = if (i % 3 == 0) radius - 10.dp.toPx() else radius - 6.dp.toPx()
                            val start = Offset(
                                center.x + (innerR * sin(angleRad)).toFloat(),
                                center.y - (innerR * cos(angleRad)).toFloat()
                            )
                            val end = Offset(
                                center.x + (radius * sin(angleRad)).toFloat(),
                                center.y - (radius * cos(angleRad)).toFloat()
                            )
                            drawLine(
                                color = if (i % 3 == 0) Color(0xFF38BDF8) else Color(0xFF475569),
                                start = start,
                                end = end,
                                strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }
                    }

                    // Cardinal Labels (N, S, E, W)
                    Text(
                        text = "N",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp)
                    )
                    Text(
                        text = "S",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
                    )
                    Text(
                        text = "E",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                    )
                    Text(
                        text = "W",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 2.dp)
                    )

                    // Directional Pointer Arrow (Points towards direction wind is blowing to)
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Wind Arrow",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(animatedRotation)
                    )

                    // Center Hub dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                // Wind Velocity & Heading Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displaySpeed,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = "Gusting to $displayGust",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "Heading ${current.windCardinal} (${current.windDirectionDeg.toInt()}°)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Beaufort Scale description
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = windDynamics.beaufortDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Activity Impact Chips (Drone flight, Cycling resistance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("DRONE FLIGHT", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(windDynamics.droneFlyingSafety, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("CYCLING EFFORT", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(windDynamics.cyclingImpact, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
