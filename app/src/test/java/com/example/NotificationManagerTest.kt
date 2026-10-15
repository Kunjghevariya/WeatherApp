package com.example

import android.app.NotificationManager as AndroidSystemNotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.database.ForecastDao
import com.example.database.ForecastMetadata
import com.example.data.local.WeatherDatabase
import com.example.notification.NotificationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationManagerTest {

    private lateinit var context: Context
    private lateinit var database: WeatherDatabase
    private lateinit var forecastDao: ForecastDao
    private lateinit var notificationManager: NotificationManager
    private lateinit var sysNotificationManager: AndroidSystemNotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private var monitoringJob: Job? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        forecastDao = database.forecastDao()

        sysNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidSystemNotificationManager
        shadowNotificationManager = Shadows.shadowOf(sysNotificationManager)

        notificationManager = NotificationManager(context, forecastDao, testScope)
    }

    @After
    fun tearDown() {
        monitoringJob?.cancel()
        notificationManager.clearNotifiedAlerts()
        database.close()
    }

    @Test
    fun `isSevereAlert correctly identifies severe and non-severe metadata`() {
        val severeByType = ForecastMetadata(
            locationKey = "test_1",
            cityName = "Miami",
            latitude = 25.7617,
            longitude = -80.1918,
            alertType = "severe",
            alertHeadline = "Hurricane Warning",
            alertSeverity = "WARNING"
        )
        assertTrue("alertType severe must be recognized", notificationManager.isSevereAlert(severeByType))

        val severeBySeverity = ForecastMetadata(
            locationKey = "test_2",
            cityName = "Tampa",
            latitude = 27.9506,
            longitude = -82.4572,
            alertType = "warning",
            alertHeadline = "Tornado Warning",
            alertSeverity = "WARNING"
        )
        assertTrue("alertSeverity WARNING must be recognized", notificationManager.isSevereAlert(severeBySeverity))

        val watchAlert = ForecastMetadata(
            locationKey = "test_3",
            cityName = "Orlando",
            latitude = 28.5383,
            longitude = -81.3792,
            alertType = "advisory",
            alertHeadline = "Severe Thunderstorm Watch",
            alertSeverity = "WATCH"
        )
        assertTrue("alertSeverity WATCH must be recognized as severe", notificationManager.isSevereAlert(watchAlert))

        val normalMetadata = ForecastMetadata(
            locationKey = "test_4",
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            alertType = null,
            alertSeverity = null
        )
        assertFalse("Normal weather metadata should not be severe", notificationManager.isSevereAlert(normalMetadata))
    }

    @Test
    fun `notificationManager triggers local system notification when severe alert is detected in ForecastMetadata`() = runTest(testDispatcher) {
        monitoringJob = notificationManager.startMonitoring(backgroundScope)

        val severeMetadata = ForecastMetadata(
            locationKey = "51.5074_-0.1278",
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            lastUpdated = System.currentTimeMillis(),
            alertType = "severe",
            alertHeadline = "Gale Force Wind Warning",
            alertSeverity = "WARNING",
            alertId = "gale_warning_001",
            alertDescription = "Damaging gusts up to 90 km/h expected. Stay indoors and secure flying debris."
        )

        // Insert into ForecastMetadata table
        forecastDao.insertMetadata(severeMetadata)
        testScheduler.advanceUntilIdle()

        // Verify that notification was triggered and accurate data is displayed
        val triggeredList = notificationManager.getTriggeredNotifications()
        assertTrue("At least one notification must be triggered", triggeredList.isNotEmpty())

        val lastNotified = notificationManager.getLastTriggeredNotification()
        assertNotNull("Triggered metadata must not be null", lastNotified)
        assertEquals("London", lastNotified?.cityName)
        assertEquals("Gale Force Wind Warning", lastNotified?.alertHeadline)
        assertEquals("severe", lastNotified?.alertType)
        assertEquals("WARNING", lastNotified?.alertSeverity)
        assertEquals("gale_warning_001", lastNotified?.alertId)

        // Verify system notification in shadow manager
        val allNotifications = shadowNotificationManager.allNotifications
        assertTrue("Shadow notification manager must contain posted notification", allNotifications.isNotEmpty())

        val notification = allNotifications.first()
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()

        assertEquals("Gale Force Wind Warning", title)
        assertTrue(text?.contains("Damaging gusts") == true || text?.contains("London") == true)
    }

    @Test
    fun `notificationManager does not trigger notification for normal non-severe weather updates`() = runTest(testDispatcher) {
        monitoringJob = notificationManager.startMonitoring(backgroundScope)

        val normalMetadata = ForecastMetadata(
            locationKey = "40.7128_-74.0060",
            cityName = "New York",
            latitude = 40.7128,
            longitude = -74.0060,
            lastUpdated = System.currentTimeMillis(),
            alertType = null,
            alertHeadline = null,
            alertSeverity = null
        )

        forecastDao.insertMetadata(normalMetadata)
        testScheduler.advanceUntilIdle()

        val triggered = notificationManager.getTriggeredNotifications()
        assertTrue("No notification should trigger for calm weather", triggered.isEmpty())
        assertEquals(0, shadowNotificationManager.allNotifications.size)
    }

    @Test
    fun `notificationManager deduplicates repeated emissions for identical alert id`() = runTest(testDispatcher) {
        monitoringJob = notificationManager.startMonitoring(backgroundScope)

        val alert = ForecastMetadata(
            locationKey = "test_dedup",
            cityName = "Oslo",
            latitude = 59.9139,
            longitude = 10.7522,
            lastUpdated = System.currentTimeMillis(),
            alertType = "severe",
            alertHeadline = "Blizzard Warning",
            alertSeverity = "WARNING",
            alertId = "blizzard_unique_101",
            alertDescription = "Heavy snow and zero visibility."
        )

        // Insert first time
        forecastDao.insertMetadata(alert)
        testScheduler.advanceUntilIdle()
        assertEquals(1, notificationManager.getTriggeredNotifications().size)

        // Insert again (e.g. repeated sync or Room update)
        forecastDao.insertMetadata(alert.copy(lastUpdated = System.currentTimeMillis() + 1000))
        testScheduler.advanceUntilIdle()

        // Ensure still only 1 notification was triggered
        assertEquals("Alert should not trigger duplicate notification", 1, notificationManager.getTriggeredNotifications().size)
    }

    @Test
    fun `direct triggerNotification shows accurate data and creates valid notification channel`() {
        val metadata = ForecastMetadata(
            locationKey = "35.6762_139.6503",
            cityName = "Tokyo",
            latitude = 35.6762,
            longitude = 139.6503,
            lastUpdated = System.currentTimeMillis(),
            alertType = "severe",
            alertHeadline = "Typhoon Warning: Extreme Precipitation",
            alertSeverity = "WARNING",
            alertId = "typhoon_tokyo_01",
            alertDescription = "Typhoon approaching coastline. Evacuation advisories in low-lying wards."
        )

        val success = notificationManager.triggerNotification(metadata)
        assertTrue(success)

        val channel = sysNotificationManager.getNotificationChannel(NotificationManager.CHANNEL_ID)
        assertNotNull("Notification channel must be created", channel)
        assertEquals(NotificationManager.CHANNEL_NAME, channel.name)

        val notifications = shadowNotificationManager.allNotifications
        assertTrue(notifications.isNotEmpty())
        val posted = notifications.last()
        assertEquals("Typhoon Warning: Extreme Precipitation", posted.extras.getString("android.title"))
    }
}
