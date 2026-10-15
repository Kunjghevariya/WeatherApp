package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ActivityRecommendation
import com.example.domain.ActivityType

@Composable
fun ActivityPlannerCard(
    recommendations: List<ActivityRecommendation>,
    selectedActivity: ActivityType,
    onActivitySelected: (ActivityType) -> Unit,
    onTunePreferencesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRec = recommendations.firstOrNull { it.activity == selectedActivity }
        ?: recommendations.firstOrNull()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_planner_card"),
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Activity Planner Icon",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Best Time to Go Outside",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Personalized Activity Suitability",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                TextButton(
                    onClick = onTunePreferencesClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("tune_preferences_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tune preferences",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Customize",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8))
                    )
                }
            }

            // Activity Preset Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("activity_chips_row")
            ) {
                items(ActivityType.values()) { act ->
                    val isSelected = act == selectedActivity
                    val chipBg = if (isSelected) Color(0xFF0284C7) else Color.Black.copy(alpha = 0.25f)
                    val textColor = if (isSelected) Color.White else Color(0xFFCBD5E1)

                    Row(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(20.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onActivitySelected(act) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("activity_chip_${act.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = getActivityEmoji(act),
                            fontSize = 14.sp
                        )
                        Text(
                            text = act.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Recommendation Summary for Selected Activity
            if (currentRec != null) {
                val ratingColor = when (currentRec.ratingLabel) {
                    "Optimal" -> Color(0xFF10B981)
                    "Good" -> Color(0xFF38BDF8)
                    "Moderate" -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Recommended Window",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                                )
                                Text(
                                    text = currentRec.bestWindowLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 17.sp
                                    )
                                )
                            }

                            // Score gauge badge
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(ratingColor, CircleShape)
                                    )
                                    Text(
                                        text = "${currentRec.scorePercent}% • ${currentRec.ratingLabel}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ratingColor,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "Suitability Index",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                )
                            }
                        }

                        Text(
                            text = currentRec.currentSuitabilityText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Factors breakdown
                        Text(
                            text = "FACTORS INFLUENCING THIS WINDOW:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentRec.factors.forEach { factor ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (factor.isPositive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = factor.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (factor.isPositive) Color(0xFFCBD5E1) else Color(0xFFFDE68A),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getActivityEmoji(act: ActivityType): String {
    return when (act) {
        ActivityType.WALKING -> "🚶"
        ActivityType.RUNNING -> "🏃"
        ActivityType.CYCLING -> "🚴"
        ActivityType.DOG_WALKING -> "🐕"
        ActivityType.OUTDOOR_LAUNDRY -> "🧺"
        ActivityType.COMMUTE -> "🚗"
        ActivityType.GARDENING -> "🌱"
        ActivityType.PHOTOGRAPHY -> "📸"
    }
}
