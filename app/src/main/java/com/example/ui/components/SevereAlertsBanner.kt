package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AlertSeverity
import com.example.domain.SevereAlert

@Composable
fun SevereAlertsBanner(
    alerts: List<SevereAlert>,
    onDismissAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    var selectedAlertForDialog by remember { mutableStateOf<SevereAlert?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        alerts.forEach { alert ->
            val (badgeBg, badgeText) = when (alert.severity) {
                AlertSeverity.WARNING -> Pair(Color(0xFFEF4444), "OFFICIAL WARNING")
                AlertSeverity.WATCH -> Pair(Color(0xFFF59E0B), "WATCH ADVISORY")
                AlertSeverity.ADVISORY -> Pair(Color(0xFF3B82F6), "WEATHER ADVISORY")
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedAlertForDialog = alert }
                    .testTag("severe_alert_banner_${alert.id}"),
                shape = RoundedCornerShape(16.dp),
                color = badgeBg.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Severe Alert Indicator",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                ),
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            if (alert.expiresTime.isNotEmpty()) {
                                Text(
                                    text = "Expires ${alert.expiresTime}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = alert.headline,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap to read safety actions & instructions • ${alert.sourceAgency}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { onDismissAlert(alert.id) },
                        modifier = Modifier.size(28.dp).testTag("dismiss_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss warning alert banner",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Safety Instructions Dialog
    selectedAlertForDialog?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlertForDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = when (alert.severity) {
                            AlertSeverity.WARNING -> Color(0xFFEF4444)
                            AlertSeverity.WATCH -> Color(0xFFF59E0B)
                            AlertSeverity.ADVISORY -> Color(0xFF3B82F6)
                        }
                    )
                    Text(
                        text = alert.event,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = alert.headline,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "OFFICIAL INSTRUCTIONS:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = alert.instruction,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        text = "Affected Area: ${alert.area}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Text(
                        text = "Source: ${alert.sourceAgency}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Text(
                        text = "Notice: WeatherFlow aggregates official public weather authority advisories. A forecast does not guarantee personal safety. Always comply with local civil emergency directions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedAlertForDialog = null },
                    modifier = Modifier.testTag("close_alert_dialog_button")
                ) {
                    Text("Understood")
                }
            }
        )
    }
}
