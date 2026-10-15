package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel

data class CardMetadata(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val CARD_REGISTRY = mapOf(
    "radar" to CardMetadata("radar", "Live Doppler Radar", "Real-time precipitation sweep & storm tracking", Icons.Default.Radar),
    "air_health" to CardMetadata("air_health", "Air Quality & Health", "AQI, PM2.5, respiratory index & exercise safety", Icons.Default.Air),
    "time_travel" to CardMetadata("time_travel", "Time Travel Scrubber", "Scrub through 24 hours of simulated conditions", Icons.Default.Timeline),
    "rain_window" to CardMetadata("rain_window", "Rain Window & Probability", "Precipitation timing, intensity, and start/stop", Icons.Default.WaterDrop),
    "wardrobe" to CardMetadata("wardrobe", "Smart Wardrobe Advisor", "Outfits, umbrella necessity, and commute gear", Icons.Default.Checkroom),
    "hourly" to CardMetadata("hourly", "Hourly Sequence", "Step-by-step 24-hour temperature and conditions", Icons.Default.Schedule),
    "activity" to CardMetadata("activity", "Activity Planner", "Suitability scores for walking, running, cycling", Icons.Default.DirectionsRun),
    "sun_moon" to CardMetadata("sun_moon", "Celestial Astronomy", "Sunrise, sunset, golden hour, and moon illumination", Icons.Default.WbSunny),
    "wind_pressure" to CardMetadata("wind_pressure", "Wind Dynamics Compass", "Beaufort scale, directional arrows, and drone safety", Icons.Default.Explore),
    "barometer" to CardMetadata("barometer", "Barometric Pressure & Fronts", "3-hour pressure trends and weather pattern shifts", Icons.Default.Speed),
    "metrics" to CardMetadata("metrics", "Meteorological Grid", "Humidity, UV index, cloud cover, dew point", Icons.Default.GridView),
    "daily" to CardMetadata("daily", "7-Day Outlook", "Long-range multi-day weather trends", Icons.Default.DateRange)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCustomizationSheet(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit
) {
    val userSettings = uiState.userSettings
    val currentOrder = userSettings.cardOrder
    val hiddenCards = userSettings.hiddenCards

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF64748B)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Customize Home Screen",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Reorder, toggle, and style your weather dashboard cards",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = { viewModel.resetHomeCards() }) {
                    Text("Reset", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Style selector
            Text(
                text = "CARD VISUAL STYLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "atmospheric" to "Atmospheric Glass",
                    "compact" to "Minimal Flat",
                    "oled" to "Obsidian OLED"
                ).forEach { (styleKey, label) ->
                    val isSelected = userSettings.cardStyle == styleKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setCardStyle(styleKey) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cards reorder and visibility list
            Text(
                text = "CARDS & WIDGETS (${currentOrder.size - hiddenCards.size} VISIBLE)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(currentOrder) { index, cardId ->
                    val meta = CARD_REGISTRY[cardId] ?: CardMetadata(cardId, cardId.replace("_", " ").capitalize(), "", Icons.Default.Widgets)
                    val isVisible = !hiddenCards.contains(cardId)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isVisible) Color(0xFF1E293B) else Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isVisible) Color(0xFF334155) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reorder buttons
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (index > 0) {
                                    IconButton(
                                        onClick = { viewModel.moveCard(index, index - 1) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                if (index < currentOrder.size - 1) {
                                    IconButton(
                                        onClick = { viewModel.moveCard(index, index + 1) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = meta.icon,
                                contentDescription = null,
                                tint = if (isVisible) Color(0xFF38BDF8) else Color(0xFF475569),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = meta.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isVisible) Color.White else Color(0xFF64748B)
                                    )
                                )
                                if (meta.subtitle.isNotEmpty()) {
                                    Text(
                                        text = meta.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isVisible) Color(0xFF94A3B8) else Color(0xFF475569),
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Switch(
                                checked = isVisible,
                                onCheckedChange = { viewModel.toggleCardVisibility(cardId) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0284C7),
                                    uncheckedThumbColor = Color(0xFF64748B),
                                    uncheckedTrackColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Apply & Save Dashboard", fontWeight = FontWeight.Bold)
            }
        }
    }
}
