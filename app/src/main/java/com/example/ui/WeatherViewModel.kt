package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedCityEntity
import com.example.data.local.UserPreferencesEntity
import com.example.data.local.WeatherDatabase
import com.example.data.repository.WeatherFetchResult
import com.example.data.repository.WeatherRepository
import com.example.domain.*
import com.example.database.ForecastMetadata
import com.example.notification.NotificationManager
import com.example.widget.WeatherAppWidgetProvider
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class WidgetCustomization(
    val typeIndex: Int = 0,             // 0: 4x1 Hourly, 1: 2x2 Glance, 2: 4x2 Detailed
    val backgroundOpacity: Float = 0.85f, // 0.2f .. 1.0f
    val isFrostedGlass: Boolean = true,
    val isOledBlack: Boolean = false,
    val showFreshnessBadge: Boolean = true
)

val UNSET_CITY = CityItem(
    id = "unselected",
    name = "Select Location",
    country = "",
    latitude = 0.0,
    longitude = 0.0,
    timezoneId = "UTC"
)

data class WeatherUiState(
    val currentCity: CityItem = UNSET_CITY,
    val savedCities: List<CityItem> = emptyList(),
    val weatherData: WeatherFetchResult? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val offlineAgeLabel: String = "",
    val errorMessage: String? = null,
    val userSettings: UserSettings = UserSettings(),
    val selectedTimeTravelHourIndex: Int? = null, // null means current live time
    val selectedActivity: ActivityType = ActivityType.WALKING,
    val activeAlerts: List<SevereAlert> = emptyList(),
    val widgetSettings: WidgetCustomization = WidgetCustomization(),
    val isSimulatedDemoAlertActive: Boolean = false,
    val isOnboarding: Boolean = true,
    val isCustomizingHome: Boolean = false
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getInstance(application)
    private val weatherDao = database.weatherDao()
    private val forecastDao = database.forecastDao()
    private val repository = WeatherRepository(weatherDao, forecastDao)

    // NotificationManager that monitors the ForecastMetadata table for severe alerts
    val notificationManager = NotificationManager(application, forecastDao)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Start reactive monitoring of ForecastMetadata for severe alerts
        notificationManager.startMonitoring(viewModelScope)

        // Load initial settings
        viewModelScope.launch {
            weatherDao.getUserPreferences().collect { pref ->
                if (pref != null) {
                    val orderList = if (pref.cardOrder.isNotBlank()) {
                        pref.cardOrder.split(",").filter { it.isNotBlank() }
                    } else DEFAULT_HOME_CARDS

                    val hiddenSet = if (pref.hiddenCards.isNotBlank()) {
                        pref.hiddenCards.split(",").filter { it.isNotBlank() }.toSet()
                    } else emptySet()

                    val settings = UserSettings(
                        tempUnit = pref.tempUnit,
                        windUnit = pref.windUnit,
                        precipUnit = pref.precipUnit,
                        reducedMotion = pref.reducedMotion,
                        highContrast = pref.highContrast,
                        idealTempMin = pref.idealTempMin,
                        idealTempMax = pref.idealTempMax,
                        maxWindTolerance = pref.maxWindComfort,
                        maxRainTolerance = pref.maxRainComfort,
                        themeMode = pref.themeMode,
                        cardOrder = orderList,
                        hiddenCards = hiddenSet,
                        cardStyle = pref.cardStyle,
                        isOnboardingCompleted = pref.isOnboardingCompleted,
                        useGpsLocation = pref.useGpsLocation,
                        notificationsEnabled = pref.notificationsEnabled,
                        dailyBriefingEnabled = pref.dailyBriefingEnabled,
                        rainAlertsEnabled = pref.rainAlertsEnabled,
                        severeAlertsEnabled = pref.severeAlertsEnabled
                    )

                    val isFirstSync = _uiState.value.userSettings.cardOrder == DEFAULT_HOME_CARDS
                    _uiState.update {
                        it.copy(
                            userSettings = settings,
                            isOnboarding = !pref.isOnboardingCompleted
                        )
                    }

                    // If onboarding is complete and we already have a chosen city but no weather loaded yet, load it now
                    if (pref.isOnboardingCompleted && _uiState.value.currentCity.id != "unselected" && _uiState.value.weatherData == null) {
                        loadWeatherForCity(_uiState.value.currentCity, forceRefresh = false)
                    }
                } else {
                    _uiState.update { it.copy(isOnboarding = true) }
                }
            }
        }

        // Load saved cities
        viewModelScope.launch {
            weatherDao.getSavedCities().collect { list ->
                if (list.isNotEmpty()) {
                    val converted = list.map {
                        CityItem(it.id, it.name, it.country, it.latitude, it.longitude, it.timezoneId)
                    }
                    val isFirstLoad = _uiState.value.currentCity.id == "unselected"
                    val activeCity = if (isFirstLoad) {
                        list.firstOrNull { it.isCurrentLocation }?.let {
                            CityItem(it.id, it.name, it.country, it.latitude, it.longitude, it.timezoneId)
                        } ?: converted.first()
                    } else {
                        _uiState.value.currentCity
                    }

                    _uiState.update {
                        it.copy(
                            savedCities = converted,
                            currentCity = activeCity,
                            isOnboarding = !_uiState.value.userSettings.isOnboardingCompleted && list.isEmpty()
                        )
                    }

                    if (activeCity.id != "unselected" && _uiState.value.weatherData == null) {
                        loadWeatherForCity(activeCity, forceRefresh = false)
                    }
                }
            }
        }
    }

    fun completeOnboardingWithCity(city: CityItem, useGps: Boolean) {
        viewModelScope.launch {
            val updatedSettings = _uiState.value.userSettings.copy(
                isOnboardingCompleted = true,
                useGpsLocation = useGps
            )
            updateSettings(updatedSettings)

            val count = _uiState.value.savedCities.size
            weatherDao.insertCity(
                SavedCityEntity(
                    id = city.id,
                    name = city.name,
                    country = city.country,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezoneId = city.timezoneId,
                    isCurrentLocation = useGps,
                    orderIndex = count
                )
            )

            _uiState.update {
                it.copy(
                    currentCity = city,
                    isOnboarding = false
                )
            }

            loadWeatherForCity(city, forceRefresh = true)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestAndApplyGpsLocation(context: Context, onFailed: (String) -> Unit = {}) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    var cityName = "GPS Location"
                    var countryName = "Local Area"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            var resolvedCity: String? = null
                            var resolvedCountry: String? = null
                            geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                                val addr = addresses.firstOrNull()
                                resolvedCity = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                                resolvedCountry = addr?.countryName
                            }
                            // wait shortly for async geocoder
                            var waited = 0
                            while (resolvedCity == null && waited < 10) {
                                kotlinx.coroutines.delay(100)
                                waited++
                            }
                            cityName = resolvedCity ?: "GPS Location"
                            countryName = resolvedCountry ?: "Local Area"
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            val addr = addresses?.firstOrNull()
                            cityName = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "GPS Location"
                            countryName = addr?.countryName ?: "Local Area"
                        }
                    } catch (_: Exception) {
                        cityName = "GPS Location"
                        countryName = "Local Area"
                    }

                    val gpsCity = CityItem(
                        id = "gps_${String.format(Locale.US, "%.3f_%.3f", loc.latitude, loc.longitude)}",
                        name = cityName,
                        country = countryName,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        timezoneId = java.util.TimeZone.getDefault().id
                    )

                    withContext(Dispatchers.Main) {
                        completeOnboardingWithCity(gpsCity, useGps = true)
                    }
                }
            } else {
                onFailed("GPS position is currently unavailable. Please turn on Location services or choose a city.")
            }
        }.addOnFailureListener {
            onFailed("Failed to access location: ${it.localizedMessage}")
        }
    }

    fun loadWeatherForCity(city: CityItem, forceRefresh: Boolean = false) {
        if (city.id == "unselected" || city.latitude == 0.0 && city.longitude == 0.0) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentCity = city,
                    isLoading = it.weatherData == null,
                    isRefreshing = it.weatherData != null,
                    errorMessage = null,
                    selectedTimeTravelHourIndex = null
                )
            }

            try {
                val result = repository.getWeatherData(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    cityName = city.name,
                    timezoneId = city.timezoneId,
                    forceRefresh = forceRefresh
                )

                val isOffline = result.isOffline || result.freshness.status == FreshnessStatus.STALE
                val ageText = if (result.cachedAgeLabel.isNotEmpty()) {
                    result.cachedAgeLabel
                } else {
                    result.freshness.latencyOrAgeText
                }

                _uiState.update { current ->
                    val finalAlerts = if (current.isSimulatedDemoAlertActive) {
                        listOf(createDemoAlert(city.name)) + result.alerts
                    } else {
                        result.alerts
                    }
                    current.copy(
                        weatherData = result,
                        isOffline = isOffline,
                        offlineAgeLabel = ageText,
                        activeAlerts = finalAlerts,
                        isLoading = false,
                        isRefreshing = false
                    )
                }

                // Update home screen widget
                WeatherAppWidgetProvider.updateAllWidgets(
                    context = getApplication(),
                    weather = result,
                    cityName = city.name,
                    tempUnit = _uiState.value.userSettings.tempUnit
                )
            } catch (e: Exception) {
                _uiState.update { current ->
                    val cached = current.weatherData
                    val ageText = cached?.cachedAgeLabel?.ifEmpty { cached.freshness.latencyOrAgeText }
                        ?: "Cached snapshot"
                    current.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                        offlineAgeLabel = ageText,
                        errorMessage = "Could not update weather: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.currentCity.id != "unselected") {
            loadWeatherForCity(_uiState.value.currentCity, forceRefresh = true)
        }
    }

    fun selectCity(city: CityItem) {
        if (_uiState.value.currentCity.id != city.id) {
            loadWeatherForCity(city, forceRefresh = false)
        }
    }

    fun addCityToSaved(city: CityItem) {
        viewModelScope.launch {
            val count = _uiState.value.savedCities.size
            weatherDao.insertCity(
                SavedCityEntity(city.id, city.name, city.country, city.latitude, city.longitude, city.timezoneId, false, count)
            )
        }
    }

    fun removeSavedCity(cityId: String) {
        viewModelScope.launch {
            weatherDao.deleteCity(cityId)
        }
    }

    fun setTimeTravelHour(index: Int?) {
        _uiState.update { it.copy(selectedTimeTravelHourIndex = index) }
    }

    fun selectActivity(activity: ActivityType) {
        _uiState.update { it.copy(selectedActivity = activity) }
    }

    fun updateSettings(newSettings: UserSettings) {
        _uiState.update { it.copy(userSettings = newSettings) }
        viewModelScope.launch {
            weatherDao.saveUserPreferences(
                UserPreferencesEntity(
                    id = 1,
                    tempUnit = newSettings.tempUnit,
                    windUnit = newSettings.windUnit,
                    precipUnit = newSettings.precipUnit,
                    reducedMotion = newSettings.reducedMotion,
                    highContrast = newSettings.highContrast,
                    idealTempMin = newSettings.idealTempMin,
                    idealTempMax = newSettings.idealTempMax,
                    maxWindComfort = newSettings.maxWindTolerance,
                    maxRainComfort = newSettings.maxRainTolerance,
                    themeMode = newSettings.themeMode,
                    cardOrder = newSettings.cardOrder.joinToString(","),
                    hiddenCards = newSettings.hiddenCards.joinToString(","),
                    cardStyle = newSettings.cardStyle,
                    isOnboardingCompleted = newSettings.isOnboardingCompleted,
                    useGpsLocation = newSettings.useGpsLocation,
                    notificationsEnabled = newSettings.notificationsEnabled,
                    dailyBriefingEnabled = newSettings.dailyBriefingEnabled,
                    rainAlertsEnabled = newSettings.rainAlertsEnabled,
                    severeAlertsEnabled = newSettings.severeAlertsEnabled
                )
            )
        }
    }

    fun toggleTemperatureUnit() {
        val current = _uiState.value.userSettings.tempUnit
        val newUnit = if (current == "F") "C" else "F"
        val updated = _uiState.value.userSettings.copy(tempUnit = newUnit)
        updateSettings(updated)
        // Refresh widget immediately with updated unit
        _uiState.value.weatherData?.let {
            WeatherAppWidgetProvider.updateAllWidgets(
                getApplication(),
                it,
                _uiState.value.currentCity.name,
                newUnit
            )
        }
    }

    fun setTemperatureUnit(unit: String) {
        if (_uiState.value.userSettings.tempUnit != unit) {
            val updated = _uiState.value.userSettings.copy(tempUnit = unit)
            updateSettings(updated)
            _uiState.value.weatherData?.let {
                WeatherAppWidgetProvider.updateAllWidgets(
                    getApplication(),
                    it,
                    _uiState.value.currentCity.name,
                    unit
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        updateSettings(_uiState.value.userSettings.copy(themeMode = mode))
    }

    fun toggleCardVisibility(cardKey: String) {
        val currentHidden = _uiState.value.userSettings.hiddenCards.toMutableSet()
        if (currentHidden.contains(cardKey)) {
            currentHidden.remove(cardKey)
        } else {
            currentHidden.add(cardKey)
        }
        updateSettings(_uiState.value.userSettings.copy(hiddenCards = currentHidden))
    }

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val currentOrder = _uiState.value.userSettings.cardOrder.toMutableList()
        if (fromIndex in currentOrder.indices && toIndex in currentOrder.indices) {
            val item = currentOrder.removeAt(fromIndex)
            currentOrder.add(toIndex, item)
            updateSettings(_uiState.value.userSettings.copy(cardOrder = currentOrder))
        }
    }

    fun resetHomeCards() {
        updateSettings(
            _uiState.value.userSettings.copy(
                cardOrder = DEFAULT_HOME_CARDS,
                hiddenCards = emptySet(),
                cardStyle = "atmospheric"
            )
        )
    }

    fun setCardStyle(style: String) {
        updateSettings(_uiState.value.userSettings.copy(cardStyle = style))
    }

    fun setCustomizingHome(isCustomizing: Boolean) {
        _uiState.update { it.copy(isCustomizingHome = isCustomizing) }
    }

    fun sendTestNotification(type: String) {
        val current = _uiState.value.weatherData?.current
        val isFahrenheit = _uiState.value.userSettings.tempUnit == "F"
        val cityName = _uiState.value.currentCity.name.ifEmpty { "Your Location" }
        val temp = current?.let {
            val t = if (isFahrenheit) (it.temperatureC * 9 / 5) + 32 else it.temperatureC
            "${t.toInt()}°"
        } ?: "24°C"
        val condition = current?.conditionLabel ?: "Clear"
        val highLow = current?.let {
            val h = if (isFahrenheit) (it.maxTempTodayC * 9 / 5) + 32 else it.maxTempTodayC
            val l = if (isFahrenheit) (it.minTempTodayC * 9 / 5) + 32 else it.minTempTodayC
            "H: ${h.toInt()}°  L: ${l.toInt()}°"
        } ?: "H: 28°  L: 19°"

        notificationManager.sendTestNotification(
            type = type,
            cityName = cityName,
            temp = temp,
            condition = condition,
            highLow = highLow
        )
    }

    fun dismissAlert(alertId: String) {
        _uiState.update { state ->
            val updated = state.activeAlerts.filter { it.id != alertId }
            state.copy(
                activeAlerts = updated,
                isSimulatedDemoAlertActive = if (alertId == "demo_alert") false else state.isSimulatedDemoAlertActive
            )
        }
    }

    fun toggleDemoAlert() {
        _uiState.update { state ->
            val isNowActive = !state.isSimulatedDemoAlertActive
            val demoAlert = createDemoAlert(state.currentCity.name)
            val newAlerts = if (isNowActive) {
                listOf(demoAlert) + state.activeAlerts.filter { it.id != "demo_alert" }
            } else {
                state.activeAlerts.filter { it.id != "demo_alert" }
            }

            viewModelScope.launch {
                try {
                    val lat = state.currentCity.latitude
                    val lon = state.currentCity.longitude
                    val roundedLat = String.format(Locale.US, "%.4f", lat).toDouble()
                    val roundedLon = String.format(Locale.US, "%.4f", lon).toDouble()
                    val locationKey = "${roundedLat}_${roundedLon}"

                    forecastDao.insertMetadata(
                        ForecastMetadata(
                            locationKey = locationKey,
                            cityName = state.currentCity.name,
                            latitude = roundedLat,
                            longitude = roundedLon,
                            lastUpdated = System.currentTimeMillis(),
                            source = "Emergency Alert System",
                            licenseNotice = "Public Domain",
                            cacheStatus = "LIVE",
                            alertType = if (isNowActive) "severe" else null,
                            alertHeadline = if (isNowActive) demoAlert.headline else null,
                            alertSeverity = if (isNowActive) "WARNING" else null,
                            alertId = if (isNowActive) demoAlert.id else null,
                            alertDescription = if (isNowActive) demoAlert.instruction else null
                        )
                    )
                } catch (_: Exception) {}
            }

            state.copy(
                isSimulatedDemoAlertActive = isNowActive,
                activeAlerts = newAlerts
            )
        }
    }

    fun updateWidgetSettings(customization: WidgetCustomization) {
        _uiState.update { it.copy(widgetSettings = customization) }
    }

    private fun createDemoAlert(cityName: String): SevereAlert {
        return SevereAlert(
            id = "demo_alert",
            event = "[TEST SIMULATION] Flood Watch Test",
            headline = "[TEST] Simulated Flood Watch for $cityName Basin",
            severity = AlertSeverity.WATCH,
            area = "$cityName Metropolitan Area (Simulated Test)",
            effectiveTime = "Active Test Session",
            expiresTime = "Test Session",
            instruction = "This is a non-emergency simulated banner for in-app test verification. No official weather emergency is active.",
            sourceAgency = "In-App Simulation Test Mode"
        )
    }

    fun setCustomLocation(lat: Double, lon: Double, name: String, country: String) {
        val customCity = CityItem(
            id = "loc_${String.format(Locale.US, "%.2f_%.2f", lat, lon)}",
            name = name,
            country = country,
            latitude = lat,
            longitude = lon,
            timezoneId = java.util.TimeZone.getDefault().id
        )
        completeOnboardingWithCity(customCity, useGps = false)
    }
}
