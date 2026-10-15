package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DataFreshness
import com.example.domain.FreshnessStatus

@Composable
fun FreshnessBanner(
    freshness: DataFreshness,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onTrustCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (freshness.status) {
        FreshnessStatus.LIVE -> Color(0xFF10B981)      // Emerald Green
        FreshnessStatus.CACHED -> Color(0xFFF59E0B)    // Amber
        FreshnessStatus.REFRESHING -> Color(0xFF38BDF8)// Cyan
        FreshnessStatus.STALE -> Color(0xFFEF4444)     // Soft Red
    }

    val rotationAnim = rememberInfiniteTransition(label = "RefreshSpin")
    val angle by if (isRefreshing) {
        rotationAnim.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SpinAngle"
        )
    } else {
        androidx.compose.runtime.rememberUpdatedState(0f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrustCenterClick() }
            .testTag("freshness_banner"),
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.28f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )

                Column {
                    Text(
                        text = if (isRefreshing) "Refreshing forecast..." else freshness.latencyOrAgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${freshness.providerName} • ${freshness.licenseNotice}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onTrustCenterClick,
                    modifier = Modifier.size(36.dp).testTag("trust_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Forecast Trust & Source Details",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.size(36.dp).testTag("refresh_weather_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh live weather",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(angle)
                    )
                }
            }
        }
    }
}
