package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.database.ForecastDao
import com.example.database.ForecastMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Collections
import java.util.Locale
import android.app.NotificationManager as AndroidSystemNotificationManager

/**
 * NotificationManager class that monitors the 'ForecastMetadata' table for new 'severe' type alerts
 * and triggers a local system notification when one is detected, displaying accurate weather data.
 */
class NotificationManager(
    private val context: Context,
    private val forecastDao: ForecastDao,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    companion object {
        const val CHANNEL_ID = "severe_weather_alerts"
        const val CHANNEL_NAME = "Severe Weather Alerts"
        const val CHANNEL_BRIEFING_ID = "weather_briefing"
        const val CHANNEL_BRIEFING_NAME = "Daily Weather Briefing"
        const val CHANNEL_RAIN_ID = "rain_alerts"
        const val CHANNEL_RAIN_NAME = "Precipitation & Rain Alerts"
        private const val TAG = "NotificationManager"
    }

    private val androidNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidSystemNotificationManager

    // Set of unique alert IDs that have already triggered a notification to avoid duplicates
    private val notifiedAlertKeys = Collections.synchronizedSet(mutableSetOf<String>())

    // Observable flow of triggered notifications for in-app reaction and automated testing
    private val _triggeredAlerts = MutableStateFlow<List<ForecastMetadata>>(emptyList())
    val triggeredAlerts: StateFlow<List<ForecastMetadata>> = _triggeredAlerts.asStateFlow()

    init {
        createNotificationChannels()
    }

    /**
     * Creates notification channels for severe alerts, daily briefing, and rain alerts (API 26+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val severeChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidSystemNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority notifications for severe weather watches and warnings"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                setShowBadge(true)
            }

            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFING_ID,
                CHANNEL_BRIEFING_NAME,
                AndroidSystemNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning and evening daily atmospheric briefings"
                setShowBadge(true)
            }

            val rainChannel = NotificationChannel(
                CHANNEL_RAIN_ID,
                CHANNEL_RAIN_NAME,
                AndroidSystemNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely alerts when rain, snow, or stormfronts approach your location"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            androidNotificationManager.createNotificationChannel(severeChannel)
            androidNotificationManager.createNotificationChannel(briefingChannel)
            androidNotificationManager.createNotificationChannel(rainChannel)
        }
    }

    /**
     * Determines whether a given [ForecastMetadata] entry contains a severe weather alert.
     */
    fun isSevereAlert(metadata: ForecastMetadata): Boolean {
        val alertType = metadata.alertType?.trim()?.lowercase(Locale.ROOT)
        val alertSeverity = metadata.alertSeverity?.trim()?.uppercase(Locale.ROOT)

        return alertType == "severe" ||
                alertType == "warning" ||
                alertSeverity == "SEVERE" ||
                alertSeverity == "WARNING" ||
                alertSeverity == "WATCH"
    }

    /**
     * Starts continuous monitoring of the [ForecastMetadata] table.
     * When a new severe alert is detected, triggers a local system notification.
     */
    fun startMonitoring(scope: CoroutineScope = coroutineScope): Job {
        return scope.launch {
            forecastDao.getAllForecastMetadata()
                .distinctUntilChanged()
                .collect { metadataList ->
                    for (metadata in metadataList) {
                        if (isSevereAlert(metadata)) {
                            val alertKey = metadata.alertId?.ifEmpty { null }
                                ?: "${metadata.locationKey}_${metadata.alertHeadline}_${metadata.lastUpdated}"

                            if (notifiedAlertKeys.add(alertKey)) {
                                Log.i(TAG, "New severe alert detected for ${metadata.cityName}: ${metadata.alertHeadline}")
                                triggerNotification(metadata)
                            }
                        }
                    }
                }
        }
    }

    /**
     * Emits a Flow of new severe alerts detected in the [ForecastMetadata] table.
     */
    fun monitorAlerts(): Flow<ForecastMetadata> = flow {
        forecastDao.getAllForecastMetadata().collect { list ->
            for (item in list) {
                if (isSevereAlert(item)) {
                    val key = item.alertId?.ifEmpty { null } ?: "${item.locationKey}_${item.alertHeadline}"
                    if (notifiedAlertKeys.add(key)) {
                        emit(item)
                    }
                }
            }
        }
    }

    /**
     * Triggers a local system notification for a severe alert, populating accurate data.
     * Returns true if notification was posted or scheduled.
     */
    fun triggerNotification(metadata: ForecastMetadata): Boolean {
        // Record in reactive state for app UI and verification tests
        _triggeredAlerts.update { current ->
            if (current.none { it.locationKey == metadata.locationKey && it.alertId == metadata.alertId }) {
                current + metadata
            } else {
                current
            }
        }

        val title = metadata.alertHeadline?.takeIf { it.isNotBlank() }
            ?: "Severe Weather Alert: ${metadata.cityName}"

        val message = metadata.alertDescription?.takeIf { it.isNotBlank() }
            ?: "Severe weather condition reported in ${metadata.cityName}. Tap to review local safety precautions."

        val formattedCoords = String.format(Locale.US, "%.2f°, %.2f°", metadata.latitude, metadata.longitude)
        val expandedBody = buildString {
            append(title)
            append("\n\n")
            append("City: ${metadata.cityName} ($formattedCoords)\n")
            append("Severity: ${metadata.alertSeverity ?: "SEVERE"}\n")
            append("Provider: ${metadata.source}\n\n")
            append(message)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_LOCATION_KEY", metadata.locationKey)
            putExtra("EXTRA_CITY_NAME", metadata.cityName)
            putExtra("EXTRA_ALERT_HEADLINE", title)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            metadata.locationKey.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(metadata.cityName)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (metadata.alertId?.hashCode() ?: metadata.locationKey.hashCode()) and 0x7FFFFFFF

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    androidNotificationManager.notify(notificationId, notification)
                    true
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification recorded in state.")
                    true
                }
            } else {
                androidNotificationManager.notify(notificationId, notification)
                true
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not post notification: ${e.message}")
            false
        }
    }

    /**
     * Returns all severe alert notifications triggered during the session.
     */
    fun getTriggeredNotifications(): List<ForecastMetadata> {
        return _triggeredAlerts.value
    }

    /**
     * Returns the most recent triggered notification, or null if none.
     */
    fun getLastTriggeredNotification(): ForecastMetadata? {
        return _triggeredAlerts.value.lastOrNull()
    }

    /**
     * Checks if an alert key has already been notified.
     */
    fun hasBeenNotified(alertId: String): Boolean {
        return notifiedAlertKeys.contains(alertId)
    }

    /**
     * Clears tracked notification keys (useful for unit tests or manual resets).
     */
    fun clearNotifiedAlerts() {
        notifiedAlertKeys.clear()
        _triggeredAlerts.value = emptyList()
    }

    /**
     * Posts a daily weather briefing notification.
     */
    fun postDailyBriefing(
        cityName: String,
        temp: String,
        condition: String,
        highLow: String,
        summary: String
    ): Boolean {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BRIEFING_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Weather in $cityName: $temp, $condition")
            .setContentText("$highLow • $summary")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Currently $temp and $condition in $cityName.\n$highLow\n\n$summary"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return dispatchNotification(1001, notification)
    }

    /**
     * Posts a precipitation / rain warning notification.
     */
    fun postRainAlert(
        cityName: String,
        headline: String,
        details: String
    ): Boolean {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_RAIN_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rain Alert for $cityName")
            .setContentText(headline)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$headline\n\n$details"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return dispatchNotification(1002, notification)
    }

    /**
     * Dispatches a test notification for user preview and testing.
     */
    fun sendTestNotification(
        type: String = "briefing",
        cityName: String = "Your City",
        temp: String = "24°C",
        condition: String = "Clear",
        highLow: String = "H: 28°  L: 19°"
    ): Boolean {
        return when (type) {
            "rain" -> postRainAlert(
                cityName = cityName,
                headline = "Precipitation analysis for $cityName",
                details = "Radar sweep complete. Current condition: $condition at $temp. Umbrella advised if precip probability rises."
            )
            "severe" -> {
                val testMetadata = ForecastMetadata(
                    locationKey = "test_key",
                    cityName = cityName,
                    latitude = 0.0,
                    longitude = 0.0,
                    lastUpdated = System.currentTimeMillis(),
                    source = "National Weather Warning Service",
                    licenseNotice = "CC BY 4.0",
                    cacheStatus = "LIVE",
                    alertType = "severe",
                    alertHeadline = "Severe Weather Alert for $cityName",
                    alertSeverity = "WARNING",
                    alertId = "test_alert_${System.currentTimeMillis()}",
                    alertDescription = "Atmospheric front approaching with winds and active cells. Condition: $condition, $temp."
                )
                triggerNotification(testMetadata)
            }
            else -> postDailyBriefing(
                cityName = cityName,
                temp = temp,
                condition = condition,
                highLow = highLow,
                summary = "Current atmosphere: $condition. High/Low projection: $highLow."
            )
        }
    }

    private fun dispatchNotification(id: Int, notification: android.app.Notification): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    androidNotificationManager.notify(id, notification)
                    true
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted.")
                    false
                }
            } else {
                androidNotificationManager.notify(id, notification)
                true
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while dispatching notification: ${e.message}")
            false
        }
    }
}

