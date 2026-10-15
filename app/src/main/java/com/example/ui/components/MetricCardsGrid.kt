package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CurrentWeather

@Composable
fun MetricCardsGrid(
    current: CurrentWeather,
    windUnit: String,
    tempUnit: String,
    modifier: Modifier = Modifier
) {
    val displayWind = when (windUnit) {
        "mph" -> "${(current.windSpeedKmh * 0.621371).toInt()} mph"
        "ms" -> "${(current.windSpeedKmh / 3.6).toInt()} m/s"
        else -> "${current.windSpeedKmh.toInt()} km/h"
    }

    val displayDew = if (tempUnit == "F") {
        "${((current.dewPointC * 9 / 5) + 32).toInt()}°F"
    } else {
        "${current.dewPointC.toInt()}°C"
    }

    val uvRiskLabel = when {
        current.uvIndex < 3 -> "Low"
        current.uvIndex < 6 -> "Moderate"
        current.uvIndex < 8 -> "High"
        current.uvIndex < 11 -> "Very High"
        else -> "Extreme"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metric_cards_grid"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = "WIND",
                value = "$displayWind ${current.windCardinal}",
                subtitle = "Direction ${current.windDirectionDeg.toInt()}°",
                icon = Icons.Default.Navigation,
                iconRotation = current.windDirectionDeg.toFloat(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "HUMIDITY",
                value = "${current.humidityPercent}%",
                subtitle = "Dew point $displayDew",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = "UV INDEX",
                value = String.format(java.util.Locale.US, "%.1f", current.uvIndex),
                subtitle = "$uvRiskLabel exposure risk",
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "PRESSURE",
                value = "${current.airPressureHpa.toInt()} hPa",
                subtitle = "Sea-level barometric",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = "CLOUD COVER",
                value = "${current.cloudCoverPercent}%",
                subtitle = if (current.cloudCoverPercent > 70) "Overcast sky" else "Clear sky breaks",
                icon = Icons.Default.Cloud,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "ATMOSPHERE",
                value = current.conditionLabel,
                subtitle = "Instant surface observation",
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconRotation: Float = 0f,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF132238).copy(alpha = 0.82f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(iconRotation)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            )
        }
    }
}
