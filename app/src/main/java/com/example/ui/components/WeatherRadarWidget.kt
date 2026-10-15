package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CurrentWeather
import com.example.domain.RainWindow
import kotlinx.coroutines.delay
import kotlin.math.*

enum class RadarLayer {
    PRECIPITATION,
    CLOUDS,
    WIND_VECTORS
}

@Composable
fun WeatherRadarWidget(
    current: CurrentWeather,
    rainWindow: RainWindow,
    cityName: String,
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var timeOffsetIndex by remember { mutableIntStateOf(3) } // 0: -45m, 1: -30m, 2: -15m, 3: NOW, 4: +30m, 5: +60m, 6: +90m
    var isPlaying by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) } // 0.8f .. 1.4f

    val timeLabels = listOf("-45m", "-30m", "-15m", "LIVE", "+30m", "+60m", "+90m")

    // Automatic playback loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1200)
            timeOffsetIndex = (timeOffsetIndex + 1) % timeLabels.size
        }
    }

    // Sweep angle continuous rotation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1324).copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .testTag("weather_radar_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Live indicator, and Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isPlaying) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                    )
                    Text(
                        text = "LIVE DOPPLER RADAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .clickable { zoomLevel = (zoomLevel + 0.2f).let { if (it > 1.4f) 0.8f else it } }
                            .testTag("radar_zoom_toggle")
                    ) {
                        Text(
                            text = "${(zoomLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("radar_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Radar" else "Play Radar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Radar Scope Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF030B17))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("radar_scope_canvas")
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (min(size.width, size.height) / 2f - 16f) * zoomLevel

                    // Draw 4 Range Rings
                    val ringSteps = 4
                    for (i in 1..ringSteps) {
                        val r = maxRadius * (i.toFloat() / ringSteps)
                        drawCircle(
                            color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Crosshair grid lines
                    drawLine(
                        color = Color(0xFF1E3A8A).copy(alpha = 0.45f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF1E3A8A).copy(alpha = 0.45f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Cardinal Markers
                    // Synthetic precipitation echo cells moving along wind vector
                    val windAngleRad = Math.toRadians(current.windDirectionDeg)
                    val timeDrift = (timeOffsetIndex - 3) * 18f
                    val driftX = (sin(windAngleRad) * timeDrift).toFloat()
                    val driftY = (-cos(windAngleRad) * timeDrift).toFloat()

                    val baseRainIntensity = if (rainWindow.hasRain) {
                        (rainWindow.peakProbability / 100f).coerceIn(0.35f, 0.95f)
                    } else {
                        (current.cloudCoverPercent / 100f * 0.4f).coerceIn(0.1f, 0.45f)
                    }

                    when (selectedLayer) {
                        RadarLayer.PRECIPITATION -> {
                            // Draw 5 realistic radar precipitation blobs
                            val blobOffsets = listOf(
                                Offset(-35f, -25f),
                                Offset(45f, -40f),
                                Offset(-60f, 35f),
                                Offset(20f, 50f),
                                Offset(-10f, 10f)
                            )
                            val blobSizes = listOf(48f, 65f, 52f, 40f, 35f)

                            blobOffsets.forEachIndexed { idx, baseOffset ->
                                val blobCenter = Offset(
                                    center.x + (baseOffset.x + driftX) * zoomLevel,
                                    center.y + (baseOffset.y + driftY) * zoomLevel
                                )
                                val radius = blobSizes[idx] * baseRainIntensity * zoomLevel

                                val blobColor = when {
                                    idx == 1 && baseRainIntensity > 0.6f -> Color(0xFFEF4444).copy(alpha = 0.75f) // Heavy Red
                                    idx == 0 && baseRainIntensity > 0.4f -> Color(0xFFF59E0B).copy(alpha = 0.70f) // Moderate Amber
                                    else -> Color(0xFF10B981).copy(alpha = 0.60f) // Light Green
                                }

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(blobColor, blobColor.copy(alpha = 0.15f), Color.Transparent),
                                        center = blobCenter,
                                        radius = radius
                                    ),
                                    center = blobCenter,
                                    radius = radius
                                )
                            }
                        }

                        RadarLayer.CLOUDS -> {
                            // Cloud density infrared simulation
                            val cloudAlpha = (current.cloudCoverPercent / 100f * 0.65f).coerceIn(0.15f, 0.75f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFE2E8F0).copy(alpha = cloudAlpha),
                                        Color(0xFF94A3B8).copy(alpha = cloudAlpha * 0.5f),
                                        Color.Transparent
                                    ),
                                    center = Offset(center.x + driftX * 0.5f, center.y + driftY * 0.5f),
                                    radius = maxRadius * 0.9f
                                ),
                                center = Offset(center.x + driftX * 0.5f, center.y + driftY * 0.5f),
                                radius = maxRadius * 0.9f
                            )
                        }

                        RadarLayer.WIND_VECTORS -> {
                            // Wind streamline arrows
                            val step = 40f * zoomLevel
                            for (x in (-maxRadius.toInt()..maxRadius.toInt() step step.toInt())) {
                                for (y in (-maxRadius.toInt()..maxRadius.toInt() step step.toInt())) {
                                    val pt = Offset(center.x + x, center.y + y)
                                    if ((pt - center).getDistance() < maxRadius) {
                                        val len = 14f * zoomLevel
                                        val endPt = Offset(
                                            pt.x + (sin(windAngleRad) * len).toFloat(),
                                            pt.y - (cos(windAngleRad) * len).toFloat()
                                        )
                                        drawLine(
                                            color = Color(0xFF38BDF8).copy(alpha = 0.65f),
                                            start = pt,
                                            end = endPt,
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Rotating Radar Sweep Line with phosphor glow
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())
                    val sweepEnd = Offset(
                        center.x + (cos(sweepRad) * maxRadius).toFloat(),
                        center.y + (sin(sweepRad) * maxRadius).toFloat()
                    )

                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 2.dp.toPx()
                    )

                    // User / Center city location marker
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = 6.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = center
                    )
                }

                // Scope Corner HUD Details
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = cityName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Range: 80 km",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )
                }

                // Time slice badge on scope
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = timeLabels[timeOffsetIndex],
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // dBZ Color scale legend at bottom of canvas
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Light (15 dBZ)", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Moderate (35 dBZ)", color = Color(0xFFF59E0B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Heavy (50 dBZ)", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Severe (65 dBZ)", color = Color(0xFF8B5CF6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Layer Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadarLayerChip(
                    label = "Precipitation",
                    icon = Icons.Default.WaterDrop,
                    isSelected = selectedLayer == RadarLayer.PRECIPITATION,
                    onClick = { selectedLayer = RadarLayer.PRECIPITATION },
                    modifier = Modifier.weight(1f).testTag("radar_layer_precip")
                )
                RadarLayerChip(
                    label = "Clouds",
                    icon = Icons.Default.Cloud,
                    isSelected = selectedLayer == RadarLayer.CLOUDS,
                    onClick = { selectedLayer = RadarLayer.CLOUDS },
                    modifier = Modifier.weight(1f).testTag("radar_layer_clouds")
                )
                RadarLayerChip(
                    label = "Wind Stream",
                    icon = Icons.Default.Air,
                    isSelected = selectedLayer == RadarLayer.WIND_VECTORS,
                    onClick = { selectedLayer = RadarLayer.WIND_VECTORS },
                    modifier = Modifier.weight(1f).testTag("radar_layer_wind")
                )
            }

            // Time Travel Slider across past & future
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    timeLabels.forEachIndexed { index, label ->
                        val isSelected = index == timeOffsetIndex
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier
                                .clickable { timeOffsetIndex = index }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("radar_time_chip_$index")
                        )
                    }
                }

                Slider(
                    value = timeOffsetIndex.toFloat(),
                    onValueChange = { timeOffsetIndex = it.roundToInt() },
                    valueRange = 0f..6f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF38BDF8),
                        activeTrackColor = Color(0xFF0284C7),
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("radar_time_slider")
                )
            }
        }
    }
}

@Composable
private fun RadarLayerChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder(true) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            )
        }
    }
}
