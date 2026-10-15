package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

/**
 * WeatherRadarWidget provides an atmospheric scope visualization and radar roadmap.
 * Explicitly distinguishes numerical weather prediction model fields from observed ground Doppler telemetry,
 * ensuring full meteorological honesty without fabricating synthetic sensor blobs.
 */
@Composable
fun WeatherRadarWidget(
    current: CurrentWeather,
    rainWindow: RainWindow,
    cityName: String,
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var timeOffsetIndex by remember { mutableIntStateOf(3) } // 0: -45m, 1: -30m, 2: -15m, 3: NOW, 4: +30m, 5: +60m, 6: +90m
    var isPlaying by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var showRoadmapDialog by remember { mutableStateOf(false) }

    val timeLabels = listOf("-45m", "-30m", "-15m", "LIVE", "+30m", "+60m", "+90m")

    // Playback loop (only runs when active)
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1400)
            timeOffsetIndex = (timeOffsetIndex + 1) % timeLabels.size
        }
    }

    // Sweep angle animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
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
            // Header: Title, Telemetry indicator, and Info button
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
                            .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8), CircleShape)
                    )
                    Text(
                        text = "ATMOSPHERIC SCOPE & RADAR",
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
                    IconButton(
                        onClick = { showRoadmapDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Radar Data Source Info",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

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

                    // 4 Range Rings (25km, 50km, 75km, 100km range)
                    val ringSteps = 4
                    for (i in 1..ringSteps) {
                        val r = maxRadius * (i.toFloat() / ringSteps)
                        drawCircle(
                            color = Color(0xFF1E3A8A).copy(alpha = 0.35f),
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

                    // Draw rotating sweep line
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())
                    val sweepEnd = Offset(
                        (center.x + maxRadius * cos(sweepRad)).toFloat(),
                        (center.y + maxRadius * sin(sweepRad)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Wind Vector arrow from center
                    val windRad = Math.toRadians(current.windDirectionDeg - 90.0)
                    val arrowLen = (current.windSpeedKmh * 1.8 * zoomLevel.toDouble()).coerceIn(20.0, maxRadius.toDouble() * 0.75)
                    val arrowEnd = Offset(
                        (center.x + (arrowLen * cos(windRad)).toFloat()),
                        (center.y + (arrowLen * sin(windRad)).toFloat())
                    )
                    drawLine(
                        color = Color(0xFF0284C7),
                        start = center,
                        end = arrowEnd,
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Draw city center marker
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 4.dp.toPx(),
                        center = center
                    )
                }

                // Scope Overlay Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
                        text = when (selectedLayer) {
                            RadarLayer.PRECIPITATION -> if (rainWindow.hasRain) "Forecast Rain: ${rainWindow.peakProbability}% peak" else "NWP Field: Dry (0 mm/h)"
                            RadarLayer.CLOUDS -> "Cloud Cover: ${current.cloudCoverPercent}%"
                            RadarLayer.WIND_VECTORS -> "Wind: ${current.windSpeedKmh.toInt()} km/h (${current.windCardinal})"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp
                        )
                    )
                }

                // Station Telemetry Status Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(0.5.dp, Color(0xFF64748B)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GROUND DOPPLER TELEMETRY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                        Text(
                            text = "Regional station feed requires commercial radar integration. Displaying NWP model wind & precipitation vector.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }

                // Timestamp pill at top right
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
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

                // Range rings legend at bottom of canvas
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("25 km", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("50 km", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("75 km", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("100 km Max Range", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                    label = "Wind Vector",
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

    if (showRoadmapDialog) {
        AlertDialog(
            onDismissRequest = { showRoadmapDialog = false },
            title = {
                Text("Radar & Satellite Telemetry Roadmap", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Why is ground Doppler radar telemetry not streaming directly?",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF38BDF8))
                    )
                    Text(
                        text = "Live ground Doppler radar relies on terrestrial station networks (e.g. US NEXRAD, UK Met Office, DWD). Free weather APIs like MET Norway provide numerical weather prediction (NWP) model outputs rather than raw radar sweeps.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "To maintain meteorological integrity, WeatherFlow refuses to fake Doppler echoes with arbitrary canvas blobs.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "A production radar tile provider integration (RainViewer / NOAA Open Data) is slated for an upcoming release following commercial licensing verification.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoadmapDialog = false }) {
                    Text("Understood")
                }
            }
        )
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
