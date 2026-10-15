package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BarometerTrend

@Composable
fun BarometerTrendWidget(
    trend: BarometerTrend,
    modifier: Modifier = Modifier
) {
    val trendColor = when {
        trend.change3hHpa < -1.5 -> Color(0xFFEF4444)
        trend.change3hHpa < -0.4 -> Color(0xFFF59E0B)
        trend.change3hHpa > 1.5 -> Color(0xFF38BDF8)
        trend.change3hHpa > 0.4 -> Color(0xFF10B981)
        else -> Color(0xFF94A3B8)
    }

    val trendIcon = when {
        trend.change3hHpa < -0.4 -> Icons.AutoMirrored.Filled.TrendingDown
        trend.change3hHpa > 0.4 -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1626).copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, trendColor.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .testTag("barometer_trend_widget")
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
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Barometer",
                        tint = trendColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "BAROMETRIC PRESSURE & FRONTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = trendColor,
                            letterSpacing = 1.1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = trendColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = trend.trendLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = trendColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Current Pressure & 3-hour delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${trend.pressureHpa.toInt()} hPa",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = "Sea-level corrected barometric baseline",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "3-HR RATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (trend.change3hHpa > 0) "+${trend.change3hHpa} hPa" else "${trend.change3hHpa} hPa",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = trendColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            // Visual Scale Bar (980 hPa to 1040 hPa)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val fraction = ((trend.pressureHpa - 980.0) / 60.0).coerceIn(0.0, 1.0).toFloat()
                        // Track
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                        // Active pin
                        drawCircle(
                            color = trendColor,
                            radius = 6.dp.toPx(),
                            center = Offset(size.width * fraction, size.height / 2f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Low (980)", color = Color(0xFF64748B), fontSize = 10.sp)
                    Text("Normal (1013)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("High (1040)", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }

            // Front Forecast Implication
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = trend.shortTermImplication,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
