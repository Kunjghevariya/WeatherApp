package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.repository.WeatherFetchResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1. Detailed Atmospheric Matrix Widget (4x2 / 3x2)
 */
class WeatherAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WidgetManager.updateDetailedWidget(context, appWidgetManager, appWidgetId, null, "WeatherFlow", "C")
        }
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            weather: WeatherFetchResult?,
            cityName: String,
            tempUnit: String
        ) {
            WidgetManager.updateAllWidgets(context, weather, cityName, tempUnit)
        }

        fun requestPinWidget(context: Context, formFactorIndex: Int = 1): Boolean {
            return WidgetManager.requestPinWidget(context, formFactorIndex)
        }
    }
}

/**
 * 2. Horizontal Pill Widget (4x1)
 */
class WeatherPillWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WidgetManager.updatePillWidget(context, appWidgetManager, appWidgetId, null, "WeatherFlow", "C")
        }
    }
}

/**
 * 3. Compact Quick Glance Capsule Widget (1x1 / 2x1)
 */
class WeatherQuickWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WidgetManager.updateQuickWidget(context, appWidgetManager, appWidgetId, null, "WeatherFlow", "C")
        }
    }
}

/**
 * Central Widget Manager managing multiple widget forms, live telemetry, and custom user styling.
 */
object WidgetManager {

    private const val PREFS_NAME = "weather_widget_cache"

    data class WidgetTelemetry(
        val city: String,
        val temp: String,
        val high: String,
        val low: String,
        val condition: String,
        val rainChance: Int,
        val windSpeed: Int,
        val updateTime: String,
        val theme: String,
        val opacity: Float
    )

    private fun getBackgroundDrawable(theme: String): Int {
        return when (theme.lowercase(Locale.ROOT)) {
            "oled", "obsidian" -> R.drawable.widget_background_oled
            "aurora", "cosmic" -> R.drawable.widget_background_aurora
            "cyber" -> R.drawable.widget_background_cyber
            else -> R.drawable.widget_background_frosted
        }
    }

    fun getTelemetry(
        context: Context,
        weather: WeatherFetchResult?,
        cityName: String,
        tempUnit: String
    ): WidgetTelemetry {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = weather?.current
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        return if (current != null) {
            val dTemp = if (tempUnit == "F") "${((current.temperatureC * 9 / 5) + 32).toInt()}°"
            else "${current.temperatureC.toInt()}°"
            val dHigh = if (tempUnit == "F") "${((current.maxTempTodayC * 9 / 5) + 32).toInt()}°"
            else "${current.maxTempTodayC.toInt()}°"
            val dLow = if (tempUnit == "F") "${((current.minTempTodayC * 9 / 5) + 32).toInt()}°"
            else "${current.minTempTodayC.toInt()}°"
            val cond = current.conditionLabel
            val rain = weather.hourly.firstOrNull()?.precipProbability ?: 0
            val wind = current.windSpeedKmh.toInt()
            val upTime = "Updated $timeFmt"
            val theme = prefs.getString("widget_theme", "frosted") ?: "frosted"
            val opacity = prefs.getFloat("widget_opacity", 0.85f)

            // Cache data
            prefs.edit()
                .putString("city", cityName)
                .putString("temp", dTemp)
                .putString("high", dHigh)
                .putString("low", dLow)
                .putString("condition", cond)
                .putInt("rain", rain)
                .putInt("wind", wind)
                .putString("time", upTime)
                .apply()

            WidgetTelemetry(
                city = cityName,
                temp = dTemp,
                high = dHigh,
                low = dLow,
                condition = cond,
                rainChance = rain,
                windSpeed = wind,
                updateTime = upTime,
                theme = theme,
                opacity = opacity
            )
        } else {
            val dCity = prefs.getString("city", cityName) ?: cityName
            val dTemp = prefs.getString("temp", "--°") ?: "--°"
            val dHigh = prefs.getString("high", "--°") ?: "--°"
            val dLow = prefs.getString("low", "--°") ?: "--°"
            val cond = prefs.getString("condition", "WeatherFlow") ?: "WeatherFlow"
            val rain = prefs.getInt("rain", 0)
            val wind = prefs.getInt("wind", 0)
            val upTime = prefs.getString("time", "Updated $timeFmt") ?: "Updated $timeFmt"
            val theme = prefs.getString("widget_theme", "frosted") ?: "frosted"
            val opacity = prefs.getFloat("widget_opacity", 0.85f)

            WidgetTelemetry(
                city = dCity,
                temp = dTemp,
                high = dHigh,
                low = dLow,
                condition = cond,
                rainChance = rain,
                windSpeed = wind,
                updateTime = upTime,
                theme = theme,
                opacity = opacity
            )
        }
    }

    private fun createLaunchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun updateDetailedWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weather: WeatherFetchResult?,
        cityName: String,
        tempUnit: String
    ) {
        val t = getTelemetry(context, weather, cityName, tempUnit)
        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        views.setInt(R.id.widget_root, "setBackgroundResource", getBackgroundDrawable(t.theme))
        views.setTextViewText(R.id.widget_city_name, t.city)
        views.setTextViewText(R.id.widget_temperature, t.temp)
        views.setTextViewText(R.id.widget_condition, t.condition)
        views.setTextViewText(R.id.widget_high_low, "H: ${t.high}  L: ${t.low}")
        views.setTextViewText(R.id.widget_updated_time, t.updateTime)
        views.setTextViewText(R.id.widget_details, "Rain Risk: ${t.rainChance}% • Wind: ${t.windSpeed} km/h")
        views.setOnClickPendingIntent(R.id.widget_root, createLaunchPendingIntent(context))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun updatePillWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weather: WeatherFetchResult?,
        cityName: String,
        tempUnit: String
    ) {
        val t = getTelemetry(context, weather, cityName, tempUnit)
        val views = RemoteViews(context.packageName, R.layout.widget_weather_pill)

        views.setInt(R.id.widget_root, "setBackgroundResource", getBackgroundDrawable(t.theme))
        views.setTextViewText(R.id.widget_city_name, t.city)
        views.setTextViewText(R.id.widget_condition, t.condition)
        views.setTextViewText(R.id.widget_updated_time, t.updateTime)
        views.setTextViewText(R.id.widget_temperature, t.temp)
        views.setTextViewText(R.id.widget_high_low, "H: ${t.high}  L: ${t.low}")
        views.setTextViewText(R.id.widget_details, "Rain: ${t.rainChance}%")
        views.setOnClickPendingIntent(R.id.widget_root, createLaunchPendingIntent(context))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun updateQuickWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weather: WeatherFetchResult?,
        cityName: String,
        tempUnit: String
    ) {
        val t = getTelemetry(context, weather, cityName, tempUnit)
        val views = RemoteViews(context.packageName, R.layout.widget_weather_quick)

        views.setInt(R.id.widget_root, "setBackgroundResource", getBackgroundDrawable(t.theme))
        views.setTextViewText(R.id.widget_city_name, t.city)
        views.setTextViewText(R.id.widget_temperature, t.temp)
        views.setTextViewText(R.id.widget_condition, t.condition)
        views.setTextViewText(R.id.widget_high_low, "H: ${t.high} L: ${t.low}")
        views.setOnClickPendingIntent(R.id.widget_root, createLaunchPendingIntent(context))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun updateAllWidgets(
        context: Context,
        weather: WeatherFetchResult? = null,
        cityName: String = "WeatherFlow",
        tempUnit: String = "C"
    ) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // 1. Detailed Matrix widgets
            val detailedProvider = ComponentName(context, WeatherAppWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(detailedProvider)) {
                updateDetailedWidget(context, appWidgetManager, id, weather, cityName, tempUnit)
            }

            // 2. Pill widgets
            val pillProvider = ComponentName(context, WeatherPillWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(pillProvider)) {
                updatePillWidget(context, appWidgetManager, id, weather, cityName, tempUnit)
            }

            // 3. Quick Capsule widgets
            val quickProvider = ComponentName(context, WeatherQuickWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(quickProvider)) {
                updateQuickWidget(context, appWidgetManager, id, weather, cityName, tempUnit)
            }
        } catch (_: Exception) {
            // Safe handling if widgets not added
        }
    }

    fun saveCustomization(
        context: Context,
        themeName: String,
        opacity: Float,
        formFactorIndex: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("widget_theme", themeName)
            .putFloat("widget_opacity", opacity)
            .putInt("widget_form_factor", formFactorIndex)
            .apply()

        // Immediately push custom styling to all placed widgets on the user's home screen
        updateAllWidgets(context)
    }

    fun requestPinWidget(context: Context, formFactorIndex: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val providerClass = when (formFactorIndex) {
                0 -> WeatherQuickWidgetProvider::class.java
                1 -> WeatherPillWidgetProvider::class.java
                else -> WeatherAppWidgetProvider::class.java
            }
            val myProvider = ComponentName(context, providerClass)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val pinnedWidgetCallbackIntent = Intent(context, providerClass)
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0,
                    pinnedWidgetCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                return appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            }
        }
        return false
    }
}
