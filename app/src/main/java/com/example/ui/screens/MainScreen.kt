package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel

/**
 * Standardized navigation tab definitions for the five primary destinations:
 * Today, Timeline, Plan, Places, and You.
 */
enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TODAY("Today", Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    TIMELINE("Timeline", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    PLAN("Plan", Icons.AutoMirrored.Filled.DirectionsRun, Icons.AutoMirrored.Outlined.DirectionsRun),
    PLACES("Places", Icons.Filled.LocationOn, Icons.Outlined.LocationOn),
    YOU("You", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * NavigationHost component that switches between the five tabs using the state variable [currentTab],
 * ensuring each tab points to its respective UI component:
 * - Today -> HomeScreen
 * - Timeline -> TimelineScreen
 * - Plan -> PlannerScreen
 * - Places -> PlacesScreen
 * - You -> YouScreen
 */
@Composable
fun NavigationHost(
    currentTab: MainTab,
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onNavigateToTab: (MainTab) -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onNavigateToTrustCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("navigation_host")
    ) {
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize(),
            label = "NavigationHostTabTransition"
        ) { tab ->
            when (tab) {
                MainTab.TODAY -> HomeScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNavigateToPlaces = { onNavigateToTab(MainTab.PLACES) },
                    onNavigateToTrustCenter = onNavigateToTrustCenter,
                    onNavigateToPlanner = { onNavigateToTab(MainTab.PLAN) },
                    onOpenSettings = onOpenSettings,
                    onNavigateToWidgets = onNavigateToWidgets
                )
                MainTab.TIMELINE -> TimelineScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { onNavigateToTab(MainTab.TODAY) }
                )
                MainTab.PLAN -> PlannerScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { onNavigateToTab(MainTab.TODAY) }
                )
                MainTab.PLACES -> PlacesScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { onNavigateToTab(MainTab.TODAY) }
                )
                MainTab.YOU -> YouScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { onNavigateToTab(MainTab.TODAY) },
                    onNavigateToWidgets = onNavigateToWidgets,
                    onOpenSettings = onOpenSettings,
                    onNavigateToTrustCenter = onNavigateToTrustCenter
                )
            }
        }
    }
}

/**
 * MainScreen component using Scaffold and NavigationBar to implement the five-tab structure:
 * Today, Timeline, Plan, Places, and You with a simple state variable [currentTab] driving the [NavigationHost].
 */
@Composable
fun MainScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
    initialTab: MainTab = MainTab.TODAY,
    onTabSelected: (MainTab) -> Unit = {}
) {
    // Simple state variable to switch between the five tabs
    var currentTab by remember { mutableStateOf(initialTab) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isWidgetsHubOpen by remember { mutableStateOf(false) }
    var isTrustCenterOpen by remember { mutableStateOf(false) }

    // Handle system back navigation:
    // If in sub-screens (WidgetsHub, TrustCenter) close it, or return to TODAY tab from any other tab
    BackHandler(enabled = isWidgetsHubOpen || isTrustCenterOpen || currentTab != MainTab.TODAY) {
        if (isWidgetsHubOpen) {
            isWidgetsHubOpen = false
        } else if (isTrustCenterOpen) {
            isTrustCenterOpen = false
        } else {
            currentTab = MainTab.TODAY
            onTabSelected(MainTab.TODAY)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Color.Transparent,
        bottomBar = {
            if (!isWidgetsHubOpen && !isTrustCenterOpen) {
                NavigationBar(
                    containerColor = Color(0xFF0B1325).copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                        .testTag("main_bottom_nav_bar")
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                onTabSelected(tab)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFF38BDF8),
                                selectedTextColor = Color(0xFF38BDF8),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier
                                .testTag("nav_item_${tab.name.lowercase()}")
                                .testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isWidgetsHubOpen) {
            WidgetsHubScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { isWidgetsHubOpen = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else if (isTrustCenterOpen) {
            TrustCenterScreen(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { isTrustCenterOpen = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            NavigationHost(
                currentTab = currentTab,
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToTab = { tab ->
                    currentTab = tab
                    onTabSelected(tab)
                },
                onOpenSettings = { isSettingsOpen = true },
                onNavigateToWidgets = { isWidgetsHubOpen = true },
                onNavigateToTrustCenter = { isTrustCenterOpen = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        if (isSettingsOpen) {
            SettingsDialog(
                settings = uiState.userSettings,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onDismiss = { isSettingsOpen = false },
                onOpenCustomization = {
                    isSettingsOpen = false
                    viewModel.setCustomizingHome(true)
                },
                onSendTestNotification = { type ->
                    viewModel.sendTestNotification(type)
                }
            )
        }
    }
}
