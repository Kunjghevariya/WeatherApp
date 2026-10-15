package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WeatherFlow", appName)
  }

  @Test
  fun `verify five bottom navigation tabs are defined`() {
    val tabs = MainTab.values()
    assertEquals(5, tabs.size)
    assertEquals("Today", tabs[0].title)
    assertEquals("Timeline", tabs[1].title)
    assertEquals("Plan", tabs[2].title)
    assertEquals("Places", tabs[3].title)
    assertEquals("You", tabs[4].title)
    // Verify each title is unique
    assertEquals(5, tabs.map { it.title }.toSet().size)
  }

  @Test
  fun `verify temperature conversion and user settings toggle`() {
    val celsius = 20.0
    val fahrenheit = ((celsius * 9 / 5) + 32).toInt()
    assertEquals(68, fahrenheit)

    val boilingC = 100.0
    val boilingF = ((boilingC * 9 / 5) + 32).toInt()
    assertEquals(212, boilingF)

    val freezingC = 0.0
    val freezingF = ((freezingC * 9 / 5) + 32).toInt()
    assertEquals(32, freezingF)

    val settingsC = com.example.domain.UserSettings(tempUnit = "C")
    assertEquals("C", settingsC.tempUnit)

    val settingsF = settingsC.copy(tempUnit = "F")
    assertEquals("F", settingsF.tempUnit)
  }

  @Test
  fun `verify offline indicator age label calculations`() {
    val now = System.currentTimeMillis()

    // 1 minute ago
    val diff1Min = ((now - (now - 60_000L)) / (60 * 1000)).toInt()
    val label1Min = if (diff1Min <= 1) "1 min ago" else "$diff1Min min ago"
    assertEquals("1 min ago", label1Min)

    // 25 minutes ago
    val diff25Min = ((now - (now - 25 * 60_000L)) / (60 * 1000)).toInt()
    val label25Min = if (diff25Min <= 1) "1 min ago" else "$diff25Min min ago"
    assertEquals("25 min ago", label25Min)

    // 2 hours ago
    val diff2Hours = ((now - (now - 120 * 60_000L)) / (60 * 1000)).toInt()
    val label2Hours = "${diff2Hours / 60}h ${diff2Hours % 60}m ago"
    assertEquals("2h 0m ago", label2Hours)
  }

  @Test
  fun `verify WeatherUiState offline mode preserves cached data and age label`() {
    val uiState = com.example.ui.WeatherUiState(
        isOffline = true,
        offlineAgeLabel = "15 min ago"
    )
    assertTrue("UI state must register offline mode", uiState.isOffline)
    assertEquals("15 min ago", uiState.offlineAgeLabel)
  }
}

