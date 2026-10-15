package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.CityItem
import com.example.domain.CuratedCities
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<CityItem>>(CuratedCities.defaultList) }

    LaunchedEffect(searchQuery) {
        searchResults = CuratedCities.searchWorldwide(searchQuery)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            locationStatusMessage = "Detecting real GPS location..."
            viewModel.requestAndApplyGpsLocation(
                context = context,
                onFailed = { locationStatusMessage = it }
            )
        } else {
            locationStatusMessage = "Location permission denied. You can select any city manually below."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cities & Locations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("places_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("places_screen_list"),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // One-tap Current Location Action
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                locationStatusMessage = "Detecting real GPS location..."
                                viewModel.requestAndApplyGpsLocation(
                                    context = context,
                                    onFailed = { locationStatusMessage = it }
                                )
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        .testTag("use_current_location_button")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF38BDF8), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "My Location",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Current Location",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "One-tap GPS coordinates (rounded to 4 decimals for privacy)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            if (locationStatusMessage != null) {
                item {
                    Text(
                        text = locationStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Search input field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search 40+ worldwide cities (e.g. London, Tokyo)...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF38BDF8)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_search_input")
                )
            }

            // Saved / Favorite Cities Section
            item {
                Text(
                    text = "SAVED CITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            items(uiState.savedCities) { city ->
                val isCurrent = city.id == uiState.currentCity.id
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCurrent) Color(0xFF0284C7).copy(alpha = 0.4f) else Color(0xFF132238).copy(alpha = 0.85f),
                    border = if (isCurrent) ButtonDefaults.outlinedButtonBorder(true) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectCity(city)
                            onBack()
                        }
                        .testTag("saved_city_item_${city.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = if (isCurrent) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "${city.country} • Lat: ${String.format(java.util.Locale.US, "%.2f", city.latitude)}, Lon: ${String.format(java.util.Locale.US, "%.2f", city.longitude)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.savedCities.size > 1) {
                                IconButton(
                                    onClick = { viewModel.removeSavedCity(city.id) },
                                    modifier = Modifier.testTag("remove_city_${city.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove from saved",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Directory List (Curated Cities)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchQuery.isEmpty()) "POPULAR GLOBAL DESTINATIONS" else "MATCHING CITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            items(searchResults) { item ->
                val isSaved = uiState.savedCities.any { it.id == item.id }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectCity(item)
                            if (!isSaved) viewModel.addCityToSaved(item)
                            onBack()
                        }
                        .testTag("search_result_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = item.country,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        if (!isSaved) {
                            TextButton(
                                onClick = { viewModel.addCityToSaved(item) },
                                modifier = Modifier.testTag("add_favorite_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = "Bookmark",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }
            }
        }
    }
}
