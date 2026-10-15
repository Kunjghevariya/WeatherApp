package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.domain.UserSettings

@Composable
fun SettingsDialog(
    settings: UserSettings,
    onSettingsChanged: (UserSettings) -> Unit,
    onDismiss: () -> Unit,
    onOpenCustomization: () -> Unit = {},
    onSendTestNotification: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "App Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Mode: Day and Night wise proper theme change
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "THEME & DAY/NIGHT ADAPTATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UnitOption(
                            label = "Auto (Solar Cycle)",
                            isSelected = settings.themeMode == "auto",
                            onClick = { onSettingsChanged(settings.copy(themeMode = "auto")) },
                            modifier = Modifier.weight(1f)
                        )
                        UnitOption(
                            label = "Always Day",
                            isSelected = settings.themeMode == "day",
                            onClick = { onSettingsChanged(settings.copy(themeMode = "day")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UnitOption(
                            label = "Always Night",
                            isSelected = settings.themeMode == "night",
                            onClick = { onSettingsChanged(settings.copy(themeMode = "night")) },
                            modifier = Modifier.weight(1f)
                        )
                        UnitOption(
                            label = "System Default",
                            isSelected = settings.themeMode == "system",
                            onClick = { onSettingsChanged(settings.copy(themeMode = "system")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Home Customization Shortcut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCustomization() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Customize Home Dashboard",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Reorder cards, toggle radar, air quality, etc.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Temperature unit
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TEMPERATURE UNIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitOption(
                            label = "Celsius (°C)",
                            isSelected = settings.tempUnit == "C",
                            onClick = { onSettingsChanged(settings.copy(tempUnit = "C")) },
                            modifier = Modifier.weight(1f).testTag("unit_temp_c")
                        )
                        UnitOption(
                            label = "Fahrenheit (°F)",
                            isSelected = settings.tempUnit == "F",
                            onClick = { onSettingsChanged(settings.copy(tempUnit = "F")) },
                            modifier = Modifier.weight(1f).testTag("unit_temp_f")
                        )
                    }
                }

                // Wind unit
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "WIND SPEED UNIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitOption(
                            label = "km/h",
                            isSelected = settings.windUnit == "kmh",
                            onClick = { onSettingsChanged(settings.copy(windUnit = "kmh")) },
                            modifier = Modifier.weight(1f).testTag("unit_wind_kmh")
                        )
                        UnitOption(
                            label = "mph",
                            isSelected = settings.windUnit == "mph",
                            onClick = { onSettingsChanged(settings.copy(windUnit = "mph")) },
                            modifier = Modifier.weight(1f).testTag("unit_wind_mph")
                        )
                        UnitOption(
                            label = "m/s",
                            isSelected = settings.windUnit == "ms",
                            onClick = { onSettingsChanged(settings.copy(windUnit = "ms")) },
                            modifier = Modifier.weight(1f).testTag("unit_wind_ms")
                        )
                    }
                }

                // Precipitation unit
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PRECIPITATION UNIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitOption(
                            label = "Millimeters (mm)",
                            isSelected = settings.precipUnit == "mm",
                            onClick = { onSettingsChanged(settings.copy(precipUnit = "mm")) },
                            modifier = Modifier.weight(1f).testTag("unit_precip_mm")
                        )
                        UnitOption(
                            label = "Inches (in)",
                            isSelected = settings.precipUnit == "in",
                            onClick = { onSettingsChanged(settings.copy(precipUnit = "in")) },
                            modifier = Modifier.weight(1f).testTag("unit_precip_in")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Notifications System Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOTIFICATIONS SYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Daily Morning Briefing", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Atmospheric conditions summary and outfit advice",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = settings.dailyBriefingEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(dailyBriefingEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Rain & Storm Alerts", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Timely heads-up before rain begins at your location",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = settings.rainAlertsEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(rainAlertsEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Severe Weather Warnings", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "High-priority watches and storm safety alerts",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = settings.severeAlertsEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(severeAlertsEnabled = it)) }
                        )
                    }

                    // Test Notification Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSendTestNotification("briefing") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Briefing", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onSendTestNotification("rain") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Rain Alert", fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Accessibility: Reduced Motion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reduced Motion",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Disables particle animations & drifting atmospheric clouds",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = settings.reducedMotion,
                        onCheckedChange = { onSettingsChanged(settings.copy(reducedMotion = it)) },
                        modifier = Modifier.testTag("switch_reduced_motion")
                    )
                }

                // Accessibility: High Contrast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High Contrast Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Boosts text sharpness against atmospheric backgrounds",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = settings.highContrast,
                        onCheckedChange = { onSettingsChanged(settings.copy(highContrast = it)) },
                        modifier = Modifier.testTag("switch_high_contrast")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("done_settings_button")) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun UnitOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder(true) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            )
        }
    }
}
