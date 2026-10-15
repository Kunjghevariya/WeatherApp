package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.widget.WidgetManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WeatherWidgetsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `saveCustomization correctly saves theme, opacity and form factor to SharedPreferences`() {
        WidgetManager.saveCustomization(
            context = context,
            themeName = "cyber",
            opacity = 0.75f,
            formFactorIndex = 1
        )

        val prefs = context.getSharedPreferences("weather_widget_cache", Context.MODE_PRIVATE)
        assertEquals("cyber", prefs.getString("widget_theme", null))
        assertEquals(0.75f, prefs.getFloat("widget_opacity", 0f), 0.01f)
        assertEquals(1, prefs.getInt("widget_form_factor", -1))
    }

    @Test
    fun `getTelemetry retrieves fallback cached telemetry when live weather is null`() {
        val prefs = context.getSharedPreferences("weather_widget_cache", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("city", "Mumbai")
            .putString("temp", "28°")
            .putString("high", "32°")
            .putString("low", "24°")
            .putString("condition", "Partly Cloudy")
            .putString("widget_theme", "obsidian")
            .putFloat("widget_opacity", 0.9f)
            .apply()

        val telemetry = WidgetManager.getTelemetry(
            context = context,
            weather = null,
            cityName = "Mumbai",
            tempUnit = "C"
        )

        assertEquals("Mumbai", telemetry.city)
        assertEquals("28°", telemetry.temp)
        assertEquals("32°", telemetry.high)
        assertEquals("24°", telemetry.low)
        assertEquals("Partly Cloudy", telemetry.condition)
        assertEquals("obsidian", telemetry.theme)
        assertEquals(0.9f, telemetry.opacity, 0.01f)
    }

    @Test
    fun `all widget layout RemoteViews inflate successfully without missing view ids`() {
        val container = android.widget.FrameLayout(context)

        // 1. Detailed Matrix
        val detailedViews = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_weather)
        detailedViews.setTextViewText(com.example.R.id.widget_city_name, "Surat")
        detailedViews.setTextViewText(com.example.R.id.widget_temperature, "30°")
        val inflatedDetailed = detailedViews.apply(context, container)
        assertNotNull(inflatedDetailed)

        // 2. Pill Widget
        val pillViews = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_weather_pill)
        pillViews.setTextViewText(com.example.R.id.widget_city_name, "Surat")
        pillViews.setTextViewText(com.example.R.id.widget_temperature, "30°")
        val inflatedPill = pillViews.apply(context, container)
        assertNotNull(inflatedPill)

        // 3. Quick Capsule Widget
        val quickViews = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_weather_quick)
        quickViews.setTextViewText(com.example.R.id.widget_city_name, "Surat")
        quickViews.setTextViewText(com.example.R.id.widget_temperature, "30°")
        val inflatedQuick = quickViews.apply(context, container)
        assertNotNull(inflatedQuick)
    }
}
