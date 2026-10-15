package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.domain.ActivityCalculator
import com.example.domain.WeatherConditionType
import com.example.domain.WeatherIconService
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.*

enum class WeatherCategoryFilter(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("All", Icons.Default.GridView),
    RADAR("Radar", Icons.Default.Radar),
    AIR_HEALTH("Air & Health", Icons.Default.Air),
    SUN_MOON("Sun & Moon", Icons.Default.WbSunny),
    WIND_PRESSURE("Wind & Fronts", Icons.Default.Speed),
    WARDROBE("Outfit", Icons.Default.Checkroom),
    TIMELINE("Timeline", Icons.Default.Schedule)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onNavigateToPlaces: () -> Unit,
    onNavigateToTrustCenter: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToWidgets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weatherData = uiState.weatherData
    val userSettings = uiState.userSettings
    var selectedFilter by remember { mutableStateOf(WeatherCategoryFilter.ALL) }

    // Calculate effective displayed conditions (either live or scrubbed via Time Travel)
    val timeTravelPoint = uiState.selectedTimeTravelHourIndex?.let {
        weatherData?.hourly?.getOrNull(it)
    }

    val displayTemp = timeTravelPoint?.let {
        if (userSettings.tempUnit == "F") "${((it.temperatureC * 9 / 5) + 32).toInt()}°"
        else "${it.temperatureC.toInt()}°"
    } ?: weatherData?.current?.let {
        if (userSettings.tempUnit == "F") "${((it.temperatureC * 9 / 5) + 32).toInt()}°"
        else "${it.temperatureC.toInt()}°"
    } ?: "--°"

    val displayFeelsLike = weatherData?.current?.let {
        if (userSettings.tempUnit == "F") "${((it.feelsLikeC * 9 / 5) + 32).toInt()}°"
        else "${it.feelsLikeC.toInt()}°"
    } ?: "--°"

    val displayHigh = weatherData?.current?.let {
        if (userSettings.tempUnit == "F") "${((it.maxTempTodayC * 9 / 5) + 32).toInt()}°"
        else "${it.maxTempTodayC.toInt()}°"
    } ?: "--°"

    val displayLow = weatherData?.current?.let {
        if (userSettings.tempUnit == "F") "${((it.minTempTodayC * 9 / 5) + 32).toInt()}°"
        else "${it.minTempTodayC.toInt()}°"
    } ?: "--°"

    val conditionType = timeTravelPoint?.conditionType
        ?: weatherData?.current?.conditionType
        ?: WeatherConditionType.CLEAR_DAY

    val conditionLabel = timeTravelPoint?.let {
        getConditionLabelFromType(it.conditionType)
    } ?: weatherData?.current?.conditionLabel ?: "Loading..."

    val symbolCode = timeTravelPoint?.symbolCode
        ?: weatherData?.current?.symbolCode
        ?: "clearsky_day"

    val nextChange = weatherData?.current?.nextChangeDescription ?: "Updating latest models..."

    val activityRecommendations = if (weatherData != null) {
        ActivityCalculator.evaluateActivities(
            current = weatherData.current,
            hourly = weatherData.hourly,
            userSettings = userSettings
        )
    } else emptyList()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier
            .fillMaxSize()
            .testTag("swipe_refresh_layout")
            .testTag("pull_to_refresh_box")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_scroll"),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Top Bar: City Selector, Unit Toggle, Widgets Hub, Settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // City button opens places
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    modifier = Modifier
                        .clickable { onNavigateToPlaces() }
                        .testTag("current_city_header_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${uiState.currentCity.name}, ${uiState.currentCity.country}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change City",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick °C / °F Unit Switcher
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.28f),
                        modifier = Modifier
                            .clickable {
                                val nextUnit = if (userSettings.tempUnit == "C") "F" else "C"
                                viewModel.updateSettings(userSettings.copy(tempUnit = nextUnit))
                            }
                            .testTag("quick_temp_unit_toggle")
                    ) {
                        Text(
                            text = if (userSettings.tempUnit == "C") "°C ⇄ °F" else "°F ⇄ °C",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    // Customize Home button
                    IconButton(
                        onClick = { viewModel.setCustomizingHome(true) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("home_customize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = "Customize Dashboard",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Widgets Studio button
                    IconButton(
                        onClick = onNavigateToWidgets,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("home_widgets_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Android Widgets Hub",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // App Settings button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "App Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share Weather Snapshot
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val current = weatherData?.current
                            val temp = if (userSettings.tempUnit == "F") {
                                "${((current?.temperatureC ?: 0.0) * 9 / 5 + 32).toInt()}°F"
                            } else {
                                "${current?.temperatureC?.toInt() ?: 0}°C"
                            }
                            val feelsLike = if (userSettings.tempUnit == "F") {
                                "${((current?.feelsLikeC ?: 0.0) * 9 / 5 + 32).toInt()}°F"
                            } else {
                                "${current?.feelsLikeC?.toInt() ?: 0}°C"
                            }
                            val city = "${uiState.currentCity.name}, ${uiState.currentCity.country}"
                            val cond = current?.conditionLabel ?: "Clear Sky"
                            val aqi = weatherData?.airQuality?.aqi ?: 0
                            val shareText = "🌤️ Weather in $city: $temp (Feels like $feelsLike), $cond. Air Quality: $aqi US AQI. Real-time atmospheric forecasts via WeatherFlow (O Square Apps):\nhttps://weatherflow-3f663.web.app/"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Weather Snapshot")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("home_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Weather",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Offline Mode Indicator (appears when network request fails)
        if (uiState.isOffline || weatherData?.isOffline == true) {
            item {
                val ageText = when {
                    uiState.offlineAgeLabel.isNotEmpty() -> uiState.offlineAgeLabel
                    !weatherData?.cachedAgeLabel.isNullOrEmpty() -> weatherData!!.cachedAgeLabel
                    weatherData != null -> weatherData.freshness.latencyOrAgeText
                    else -> "Cached snapshot"
                }
                OfflineIndicatorBanner(
                    ageLabel = ageText,
                    onRetryClick = { viewModel.refresh() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. Data Freshness Banner
        if (weatherData != null) {
            item {
                FreshnessBanner(
                    freshness = weatherData.freshness,
                    isRefreshing = uiState.isRefreshing,
                    onRefreshClick = { viewModel.refresh() },
                    onTrustCenterClick = onNavigateToTrustCenter
                )
            }
        }

        // 3. Severe Weather Alerts Banner (if active)
        if (uiState.activeAlerts.isNotEmpty()) {
            item {
                SevereAlertsBanner(
                    alerts = uiState.activeAlerts,
                    onDismissAlert = { viewModel.dismissAlert(it) }
                )
            }
        }

        // 4. Hero Section: Weather Display OR Loading OR Unavailable State
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_hero_section"),
                contentAlignment = Alignment.Center
            ) {
                if (weatherData != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Condition Material Icon + Label + Emoji
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black.copy(alpha = 0.28f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = WeatherIconService.getStandardizedIcon(symbolCode),
                                    contentDescription = conditionLabel,
                                    tint = WeatherIconService.getIconTint(symbolCode),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("home_weather_material_icon")
                                )
                                Text(
                                    text = conditionLabel,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = getConditionEmoji(conditionType),
                                    fontSize = 24.sp
                                )
                            }
                        }

                        // Primary Large Temperature
                        Text(
                            text = displayTemp,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 72.sp
                            )
                        )

                        // High / Low & Feels Like
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Feels like $displayFeelsLike",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.5f))
                            )
                            Text(
                                text = "H: $displayHigh  L: $displayLow",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Next Expected Weather Change pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.28f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Next: $nextChange",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else if (uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Retrieving meteorological models...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        )
                    }
                } else {
                    // Weather data unavailable and not currently loading (e.g. network failure with empty cache)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Forecast Unavailable",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Forecast Data Unavailable",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unable to establish contact with weather services and no offline forecast is cached. Please check your network connection.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Retry", fontSize = 13.sp)
                                }
                                OutlinedButton(
                                    onClick = onNavigateToPlaces,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Select City", color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Category Filter Chips (Horizontal Scrollable Strip)
        if (weatherData != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherCategoryFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF0284C7) else Color.Black.copy(alpha = 0.28f),
                            border = if (isSelected) ButtonDefaults.outlinedButtonBorder(true) else null,
                            modifier = Modifier
                                .clickable { selectedFilter = filter }
                                .testTag("filter_chip_${filter.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = filter.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Dashboard Cards (Configurable order & visibility)
        userSettings.cardOrder.forEach { cardId ->
            if (!userSettings.hiddenCards.contains(cardId)) {
                when (cardId) {
                    "radar" -> {
                        if (weatherData != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.RADAR)) {
                            item(key = "card_radar") {
                                WeatherRadarWidget(
                                    current = weatherData.current,
                                    rainWindow = weatherData.rainWindow,
                                    cityName = uiState.currentCity.name
                                )
                            }
                        }
                    }
                    "air_health" -> {
                        if (weatherData?.airQuality != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.AIR_HEALTH)) {
                            item(key = "card_air_health") {
                                AirQualityHealthWidget(airQuality = weatherData.airQuality)
                            }
                        }
                    }
                    "time_travel" -> {
                        if (weatherData != null && weatherData.hourly.isNotEmpty() && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.TIMELINE)) {
                            item(key = "card_time_travel") {
                                TimeTravelScrubber(
                                    hourlyList = weatherData.hourly,
                                    selectedHourIndex = uiState.selectedTimeTravelHourIndex,
                                    tempUnit = userSettings.tempUnit,
                                    onHourChanged = { viewModel.setTimeTravelHour(it) }
                                )
                            }
                        }
                    }
                    "rain_window" -> {
                        if (weatherData != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.RADAR || selectedFilter == WeatherCategoryFilter.TIMELINE)) {
                            item(key = "card_rain_window") {
                                RainWindowCard(
                                    rainWindow = weatherData.rainWindow,
                                    precipUnit = userSettings.precipUnit
                                )
                            }
                        }
                    }
                    "wardrobe" -> {
                        if (weatherData?.wardrobe != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.WARDROBE)) {
                            item(key = "card_wardrobe") {
                                SmartWardrobeWidget(wardrobe = weatherData.wardrobe)
                            }
                        }
                    }
                    "hourly" -> {
                        if (weatherData != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.TIMELINE)) {
                            item(key = "card_hourly") {
                                HourlyTimelineCard(
                                    hourlyList = weatherData.hourly,
                                    selectedHourIndex = uiState.selectedTimeTravelHourIndex,
                                    tempUnit = userSettings.tempUnit,
                                    windUnit = userSettings.windUnit,
                                    onHourSelected = { viewModel.setTimeTravelHour(it) }
                                )
                            }
                        }
                    }
                    "activity" -> {
                        if (activityRecommendations.isNotEmpty() && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.TIMELINE)) {
                            item(key = "card_activity") {
                                ActivityPlannerCard(
                                    recommendations = activityRecommendations,
                                    selectedActivity = uiState.selectedActivity,
                                    onActivitySelected = { viewModel.selectActivity(it) },
                                    onTunePreferencesClick = onNavigateToPlanner
                                )
                            }
                        }
                    }
                    "sun_moon" -> {
                        if (weatherData?.astronomy != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.SUN_MOON)) {
                            item(key = "card_sun_moon") {
                                SunMoonAstronomyWidget(astronomy = weatherData.astronomy)
                            }
                        }
                    }
                    "wind_pressure" -> {
                        if (weatherData?.windDynamics != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.WIND_PRESSURE)) {
                            item(key = "card_wind_pressure") {
                                WindCompassWidget(
                                    current = weatherData.current,
                                    windDynamics = weatherData.windDynamics,
                                    windUnit = userSettings.windUnit
                                )
                            }
                        }
                    }
                    "barometer" -> {
                        if (weatherData?.barometerTrend != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.WIND_PRESSURE)) {
                            item(key = "card_barometer") {
                                BarometerTrendWidget(trend = weatherData.barometerTrend)
                            }
                        }
                    }
                    "metrics" -> {
                        if (weatherData != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.WIND_PRESSURE)) {
                            item(key = "card_metrics") {
                                MetricCardsGrid(
                                    current = weatherData.current,
                                    windUnit = userSettings.windUnit,
                                    tempUnit = userSettings.tempUnit
                                )
                            }
                        }
                    }
                    "daily" -> {
                        if (weatherData != null && (selectedFilter == WeatherCategoryFilter.ALL || selectedFilter == WeatherCategoryFilter.TIMELINE)) {
                            item(key = "card_daily") {
                                DailyForecastCard(
                                    dailyList = weatherData.daily,
                                    tempUnit = userSettings.tempUnit
                                )
                            }
                        }
                    }
                }
            }
        }

        // 18. Trust & Attribution Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { onNavigateToTrustCenter() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Forecast data provided by MET Norway under CC BY 4.0",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Tap to view Forecast Trust & Transparency Center",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

    if (uiState.isCustomizingHome) {
        HomeCustomizationSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.setCustomizingHome(false) }
        )
    }
}

private fun getConditionLabelFromType(type: WeatherConditionType): String {
    return when (type) {
        WeatherConditionType.CLEAR_DAY -> "Clear Sky"
        WeatherConditionType.CLEAR_NIGHT -> "Clear Night"
        WeatherConditionType.PARTLY_CLOUDY_DAY -> "Partly Cloudy"
        WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Partly Cloudy"
        WeatherConditionType.CLOUDY -> "Overcast"
        WeatherConditionType.LIGHT_RAIN -> "Light Rain"
        WeatherConditionType.HEAVY_RAIN -> "Heavy Rain"
        WeatherConditionType.THUNDERSTORM -> "Thunderstorms"
        WeatherConditionType.SNOW -> "Snow"
        WeatherConditionType.SLEET -> "Sleet"
        WeatherConditionType.FOG -> "Fog"
        WeatherConditionType.WINDY -> "Breezy"
    }
}
