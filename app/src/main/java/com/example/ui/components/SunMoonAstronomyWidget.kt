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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AstronomyData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunMoonAstronomyWidget(
    astronomy: AstronomyData,
    modifier: Modifier = Modifier
) {
    val animatedSunProgress by animateFloatAsState(
        targetValue = astronomy.sunProgressFraction,
        animationSpec = tween(1200),
        label = "SunProgress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .testTag("sun_moon_astronomy_widget")
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
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sun and Moon",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SUN & MOON TRACKER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${astronomy.dayLengthHours}h ${astronomy.dayLengthMinutes}m daylight",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Solar Arc Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sun_arc_canvas")
                ) {
                    val width = size.width
                    val height = size.height
                    val horizonY = height - 25f

                    // Horizon line
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(20f, horizonY),
                        end = Offset(width - 20f, horizonY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Sun Arc Path (semi-ellipse)
                    val arcPath = Path()
                    val arcWidth = width - 80f
                    val arcHeight = height - 45f
                    val startX = 40f

                    arcPath.moveTo(startX, horizonY)
                    // Cubic or parametric ellipse
                    val steps = 60
                    for (i in 0..steps) {
                        val t = i.toFloat() / steps
                        val angle = PI - (t * PI)
                        val x = (width / 2f) + (arcWidth / 2f) * cos(angle).toFloat()
                        val y = horizonY - (arcHeight * sin(angle).toFloat())
                        if (i == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
                    }

                    // Background dashed arc
                    drawPath(
                        path = arcPath,
                        color = Color(0xFF475569),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )

                    // Active Sun Arc gradient overlay up to progress
                    val activePath = Path()
                    val activeSteps = (steps * animatedSunProgress).toInt().coerceIn(1, steps)
                    for (i in 0..activeSteps) {
                        val t = i.toFloat() / steps
                        val angle = PI - (t * PI)
                        val x = (width / 2f) + (arcWidth / 2f) * cos(angle).toFloat()
                        val y = horizonY - (arcHeight * sin(angle).toFloat())
                        if (i == 0) activePath.moveTo(x, y) else activePath.lineTo(x, y)
                    }

                    drawPath(
                        path = activePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF97316), Color(0xFFF59E0B), Color(0xFFFDE047))
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Sun Pin calculation
                    val sunAngle = PI - (animatedSunProgress * PI)
                    val sunX = (width / 2f) + (arcWidth / 2f) * cos(sunAngle).toFloat()
                    val sunY = horizonY - (arcHeight * sin(sunAngle).toFloat())

                    // Sun Outer Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(sunX, sunY),
                            radius = 20.dp.toPx()
                        ),
                        center = Offset(sunX, sunY),
                        radius = 20.dp.toPx()
                    )

                    // Sun Core
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = 7.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                }

                // Sunrise & Sunset Labels on Horizon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Sunrise",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Text(
                            text = astronomy.sunriseTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Solar Noon",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Text(
                            text = astronomy.solarNoonTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sunset",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Text(
                            text = astronomy.sunsetTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Photography Golden Hour & Blue Hour Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Photography Light",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "GOLDEN HOUR (EVENING)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = astronomy.goldenHourEvening,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BLUE HOUR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF818CF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = astronomy.blueHourEvening,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Moon Phase Information Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = astronomy.moonPhaseEmoji,
                            fontSize = 32.sp
                        )
                        Column {
                            Text(
                                text = astronomy.moonPhaseName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "${astronomy.moonIlluminationPercent}% illuminated",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Next Full Moon",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "in ${astronomy.nextFullMoonDays} days",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
