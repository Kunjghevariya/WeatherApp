package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.database.*
import com.example.data.local.*
import com.example.data.model.MetTimeSeriesItem
import com.example.data.model.MetWeatherResponse
import com.example.domain.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class WeatherFetchResult(
    val current: CurrentWeather,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val rainWindow: RainWindow,
    val alerts: List<SevereAlert>,
    val freshness: DataFreshness,
    val airQuality: AirQualityData? = null,
    val astronomy: AstronomyData? = null,
    val wardrobe: WardrobeRecommendation? = null,
    val windDynamics: WindDynamics? = null,
    val barometerTrend: BarometerTrend? = null,
    val isOffline: Boolean = false,
    val cachedAgeLabel: String = ""
)

class WeatherUnavailableException(message: String) : Exception(message)

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val forecastDao: ForecastDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val metAdapter = moshi.adapter(MetWeatherResponse::class.java)

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        cityName: String,
        timezoneId: String = "UTC",
        forceRefresh: Boolean = false
    ): WeatherFetchResult = withContext(Dispatchers.IO) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            throw IllegalArgumentException("Invalid geographic coordinates: ($latitude, $longitude)")
        }

        // Round to 4 decimal places per MET Norway API usage guidelines
        val roundedLat = String.format(Locale.US, "%.4f", latitude).toDouble()
        val roundedLon = String.format(Locale.US, "%.4f", longitude).toDouble()
        val locationKey = "${roundedLat}_${roundedLon}"

        val cachedEntity = weatherDao.getCachedWeather(locationKey)
        val now = System.currentTimeMillis()

        // Cache valid for 30 minutes unless forceRefresh
        val isCacheFresh = cachedEntity != null && (now - cachedEntity.fetchedAtEpochMs < 30 * 60 * 1000)

        if (!forceRefresh && isCacheFresh && cachedEntity != null) {
            try {
                val parsed = metAdapter.fromJson(cachedEntity.jsonContent)
                if (parsed != null) {
                    val alerts = fetchAlertsSafely(roundedLat, roundedLon)
                    return@withContext parseAndBuildResult(
                        response = parsed,
                        freshnessStatus = FreshnessStatus.CACHED,
                        lastFetched = cachedEntity.fetchedAtEpochMs,
                        alerts = alerts,
                        cityName = cityName,
                        locationKey = locationKey,
                        lat = roundedLat,
                        lon = roundedLon,
                        timezoneId = timezoneId
                    )
                }
            } catch (_: Exception) {
                // fall through to network fetch
            }
        }

        // Try network fetch
        try {
            val response = NetworkClient.metWeatherApi.getLocationForecast(
                lat = roundedLat,
                lon = roundedLon,
                ifModifiedSince = cachedEntity?.httpLastModified
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val rawJson = metAdapter.toJson(body)
                val lastMod = response.headers()["Last-Modified"]
                val providerUpdated = body.properties?.meta?.updatedAt ?: ""

                weatherDao.saveCachedWeather(
                    CachedWeatherEntity(
                        locationKey = locationKey,
                        cityName = cityName,
                        jsonContent = rawJson,
                        fetchedAtEpochMs = now,
                        providerUpdatedAt = providerUpdated,
                        httpLastModified = lastMod
                    )
                )

                val alerts = fetchAlertsSafely(roundedLat, roundedLon)
                return@withContext parseAndBuildResult(
                    response = body,
                    freshnessStatus = FreshnessStatus.LIVE,
                    lastFetched = now,
                    alerts = alerts,
                    cityName = cityName,
                    locationKey = locationKey,
                    lat = roundedLat,
                    lon = roundedLon,
                    timezoneId = timezoneId
                )
            } else if (response.code() == 304 && cachedEntity != null) {
                // Not Modified - update timestamp and use cache
                weatherDao.saveCachedWeather(
                    cachedEntity.copy(fetchedAtEpochMs = now)
                )
                val parsed = metAdapter.fromJson(cachedEntity.jsonContent)
                if (parsed != null) {
                    val alerts = fetchAlertsSafely(roundedLat, roundedLon)
                    return@withContext parseAndBuildResult(
                        response = parsed,
                        freshnessStatus = FreshnessStatus.LIVE,
                        lastFetched = now,
                        alerts = alerts,
                        cityName = cityName,
                        locationKey = locationKey,
                        lat = roundedLat,
                        lon = roundedLon,
                        timezoneId = timezoneId
                    )
                }
            }
        } catch (_: Exception) {
            // Network failed - fallback to cache if available
        }

        // Fallback to cache
        if (cachedEntity != null) {
            try {
                val parsed = metAdapter.fromJson(cachedEntity.jsonContent)
                if (parsed != null) {
                    val cachedMetadata = try {
                        forecastDao.getForecastMetadataOnce(locationKey)
                    } catch (_: Exception) { null }

                    val cachedAlerts = if (cachedMetadata?.alertType == "severe") {
                        val sev = when (cachedMetadata.alertSeverity?.uppercase()) {
                            "WARNING" -> AlertSeverity.WARNING
                            "WATCH" -> AlertSeverity.WATCH
                            else -> AlertSeverity.ADVISORY
                        }
                        listOf(
                            SevereAlert(
                                id = cachedMetadata.alertId ?: UUID.randomUUID().toString(),
                                event = cachedMetadata.alertHeadline ?: "Severe Weather Alert",
                                headline = cachedMetadata.alertHeadline ?: "Severe Weather Advisory",
                                severity = sev,
                                area = cityName,
                                effectiveTime = "",
                                expiresTime = "",
                                instruction = cachedMetadata.alertDescription ?: "Cached severe weather warning.",
                                sourceAgency = cachedMetadata.source
                            )
                        )
                    } else emptyList()

                    return@withContext parseAndBuildResult(
                        response = parsed,
                        freshnessStatus = FreshnessStatus.STALE,
                        lastFetched = cachedEntity.fetchedAtEpochMs,
                        alerts = cachedAlerts,
                        cityName = cityName,
                        locationKey = locationKey,
                        lat = roundedLat,
                        lon = roundedLon,
                        timezoneId = timezoneId,
                        isOffline = true
                    )
                }
            } catch (_: Exception) {
                // fall through
            }
        }

        // Both network and cache are unavailable - report honest error rather than fake synthetic data
        throw WeatherUnavailableException("Forecast data is currently unavailable for $cityName. Please connect to the internet and tap refresh.")
    }

    private suspend fun fetchAirQualitySafely(lat: Double, lon: Double): AirQualityData {
        return try {
            val response = NetworkClient.airQualityApi.getAirQuality(lat = lat, lon = lon)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                WeatherAnalyticsCalculator.evaluateAirQuality(body.current)
            } else {
                AirQualityData.unavailable(
                    message = "Air quality observations unavailable for this coordinate.",
                    source = "Open-Meteo Air Quality (CAMS / NOAA)"
                )
            }
        } catch (_: Exception) {
            AirQualityData.unavailable(
                message = "Air quality observations unavailable while offline.",
                source = "Open-Meteo Air Quality (CAMS / NOAA)"
            )
        }
    }

    private suspend fun fetchAlertsSafely(lat: Double, lon: Double): List<SevereAlert> {
        // NWS works for US bounding box (roughly lat 18..72, lon -168..-66)
        if (lat in 18.0..72.0 && lon in -168.0..-66.0) {
            try {
                val point = String.format(Locale.US, "%.4f,%.4f", lat, lon)
                val response = NetworkClient.nwsAlertsApi.getActiveAlerts(point)
                if (response.isSuccessful && response.body() != null) {
                    val features = response.body()!!.features
                    return features.mapNotNull { f ->
                        val p = f.properties ?: return@mapNotNull null
                        val severity = when (p.severity?.uppercase()) {
                            "EXTREME", "SEVERE" -> AlertSeverity.WARNING
                            "MODERATE" -> AlertSeverity.WATCH
                            else -> AlertSeverity.ADVISORY
                        }
                        SevereAlert(
                            id = p.id ?: UUID.randomUUID().toString(),
                            event = p.event ?: "Weather Advisory",
                            headline = p.headline ?: p.event ?: "Advisory in effect",
                            severity = severity,
                            area = p.areaDesc ?: "Local Area",
                            effectiveTime = p.effective ?: "",
                            expiresTime = p.expires ?: "",
                            instruction = p.instruction ?: p.description ?: "Follow official local guidance.",
                            sourceAgency = p.senderName ?: "National Weather Service (NWS)"
                        )
                    }
                }
            } catch (_: Exception) {
                // ignore
            }
        }
        return emptyList()
    }

    private suspend fun parseAndBuildResult(
        response: MetWeatherResponse,
        freshnessStatus: FreshnessStatus,
        lastFetched: Long,
        alerts: List<SevereAlert>,
        cityName: String,
        locationKey: String,
        lat: Double,
        lon: Double,
        timezoneId: String = "UTC",
        isOffline: Boolean = false
    ): WeatherFetchResult {
        val timeseries = response.properties?.timeseries ?: emptyList()
        val providerUpdated = response.properties?.meta?.updatedAt ?: ""

        val hourlyList = timeseries.map { parseHourlyPoint(it, timezoneId) }
        val current = parseCurrentWeather(timeseries, hourlyList)
        val dailyList = parseDailyForecast(timeseries, timezoneId)
        val rainWindow = RainPlanner.calculateRainWindow(hourlyList, providerUpdated)

        val cachedAgeLabel = if (isOffline) formatCachedAge(lastFetched) else ""
        val freshness = DataFreshness(
            status = if (isOffline) FreshnessStatus.STALE else freshnessStatus,
            lastFetchedEpochMs = lastFetched,
            providerUpdatedAtIso = providerUpdated,
            latencyOrAgeText = if (isOffline) "Cached ($cachedAgeLabel)" else formatFreshnessAge(lastFetched, freshnessStatus)
        )

        // Persist structured metadata and hourly forecast entries in Room
        try {
            val severeAlert = alerts.firstOrNull { it.severity == AlertSeverity.WARNING || it.severity == AlertSeverity.WATCH } ?: alerts.firstOrNull()

            val metadataEntity = ForecastMetadata(
                locationKey = locationKey,
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                lastUpdated = lastFetched,
                providerUpdatedAt = providerUpdated,
                source = "MET Norway Locationforecast 2.0",
                licenseNotice = "CC BY 4.0",
                cacheStatus = if (isOffline) "OFFLINE_CACHED" else freshnessStatus.name,
                alertType = if (severeAlert != null) "severe" else null,
                alertHeadline = severeAlert?.headline,
                alertSeverity = severeAlert?.severity?.name,
                alertId = severeAlert?.id,
                alertDescription = severeAlert?.instruction?.ifEmpty { severeAlert.event } ?: severeAlert?.headline
            )

            val isoDateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val hourlyEntities = hourlyList.mapIndexed { idx, pt ->
                val epochMs = try {
                    isoDateParser.parse(pt.timeIso)?.time ?: (lastFetched + idx * 3600_000L)
                } catch (_: Exception) {
                    lastFetched + idx * 3600_000L
                }

                HourlyForecast(
                    locationKey = locationKey,
                    timestamp = epochMs,
                    timestampIso = pt.timeIso,
                    hourLabel = pt.hourLabel,
                    temperature = pt.temperatureC,
                    feelsLike = pt.temperatureC,
                    precipitation = pt.precipAmountMm,
                    precipitationProbability = pt.precipProbability,
                    wind = pt.windSpeedKmh,
                    windDirectionDeg = pt.windDirectionDeg,
                    windCardinal = pt.windCardinal,
                    conditionType = pt.conditionType.name,
                    symbolCode = pt.symbolCode,
                    humidityPercent = pt.humidityPercent,
                    isDaylight = pt.isDaylight
                )
            }

            forecastDao.saveForecast(metadataEntity, hourlyEntities)
        } catch (_: Exception) {
            // Non-critical local persistence failure
        }

        val airQuality = fetchAirQualitySafely(lat, lon)
        val astronomy = WeatherAnalyticsCalculator.calculateAstronomy(lat, lon, timezoneId)
        val wardrobe = WeatherAnalyticsCalculator.calculateWardrobe(current, rainWindow, hourlyList, UserSettings())
        val windDynamics = WeatherAnalyticsCalculator.calculateWindDynamics(current, hourlyList)
        val barometerTrend = WeatherAnalyticsCalculator.calculateBarometerTrend(current, hourlyList)

        return WeatherFetchResult(
            current = current,
            hourly = hourlyList,
            daily = dailyList,
            rainWindow = rainWindow,
            alerts = alerts,
            freshness = freshness,
            airQuality = airQuality,
            astronomy = astronomy,
            wardrobe = wardrobe,
            windDynamics = windDynamics,
            barometerTrend = barometerTrend,
            isOffline = isOffline,
            cachedAgeLabel = cachedAgeLabel
        )
    }

    fun getStoredForecastWithHourly(locationKey: String): kotlinx.coroutines.flow.Flow<ForecastWithHourly?> {
        return forecastDao.getForecastWithHourly(locationKey)
    }

    fun getStoredHourlyEntries(locationKey: String): kotlinx.coroutines.flow.Flow<List<HourlyForecast>> {
        return forecastDao.getHourlyForecasts(locationKey)
    }

    fun getStoredForecastMetadata(locationKey: String): kotlinx.coroutines.flow.Flow<ForecastMetadata?> {
        return forecastDao.getForecastMetadata(locationKey)
    }

    private fun parseHourlyPoint(item: MetTimeSeriesItem, timezoneId: String): HourlyPoint {
        val details = item.data.instant?.details
        val next1 = item.data.next1Hours
        val next6 = item.data.next6Hours

        val temp = details?.airTemperature ?: 20.0
        val windSpeed = (details?.windSpeed ?: 3.0) * 3.6 // m/s to km/h
        val windDeg = details?.windFromDirection ?: 0.0
        val humidity = (details?.relativeHumidity ?: 50.0).roundToInt()
        val pressure = details?.airPressureAtSeaLevel

        val symbol = next1?.summary?.symbolCode
            ?: next6?.summary?.symbolCode
            ?: "clearsky_day"

        val precipProb = (next1?.details?.probabilityOfPrecipitation ?: 0.0).roundToInt()
        val precipMm = next1?.details?.precipitationAmount ?: 0.0

        val (hourLabel, isDay) = formatHourLabel(item.time, timezoneId, symbol)
        val conditionType = mapSymbolToCondition(symbol)

        return HourlyPoint(
            timeIso = item.time,
            hourLabel = hourLabel,
            temperatureC = temp,
            conditionType = conditionType,
            symbolCode = symbol,
            precipProbability = precipProb,
            precipAmountMm = precipMm,
            windSpeedKmh = windSpeed,
            windDirectionDeg = windDeg,
            windCardinal = degreesToCardinal(windDeg),
            humidityPercent = humidity,
            isDaylight = isDay,
            pressureHpa = pressure
        )
    }

    private fun parseCurrentWeather(
        timeseries: List<MetTimeSeriesItem>,
        hourly: List<HourlyPoint>
    ): CurrentWeather {
        val first = timeseries.firstOrNull()
        val details = first?.data?.instant?.details
        val next1 = first?.data?.next1Hours
        val next6 = first?.data?.next6Hours

        val temp = details?.airTemperature ?: 20.0
        val dewPoint = details?.dewPointTemperature ?: (temp - 4.0)
        val windSpeedKmh = (details?.windSpeed ?: 3.0) * 3.6
        val windDeg = details?.windFromDirection ?: 0.0
        val humidity = (details?.relativeHumidity ?: 55.0).roundToInt()
        val cloudCover = (details?.cloudAreaFraction ?: 30.0).roundToInt()
        val uv = details?.uvIndex ?: 2.5
        val pressure = details?.airPressureAtSeaLevel ?: 1013.2

        val symbol = next1?.summary?.symbolCode
            ?: next6?.summary?.symbolCode
            ?: "clearsky_day"

        val conditionType = mapSymbolToCondition(symbol)
        val conditionLabel = mapConditionToDisplayLabel(conditionType, symbol)

        // Calculate today's min and max from first 24h
        val todayHours = hourly.take(24)
        val minTemp = todayHours.minOfOrNull { it.temperatureC } ?: (temp - 3)
        val maxTemp = todayHours.maxOfOrNull { it.temperatureC } ?: (temp + 5)

        // Calculate "Feels Like" temperature (Australian apparent temperature formula approximation)
        val e = (humidity / 100.0) * 6.105 * Math.exp((17.27 * temp) / (237.7 + temp))
        val feelsLike = temp + 0.33 * e - 0.70 * (windSpeedKmh / 3.6) - 4.00

        // Detect next significant weather change in next 18 hours
        val nextChange = detectNextWeatherChange(hourly.take(18), conditionType, temp)

        return CurrentWeather(
            temperatureC = temp,
            feelsLikeC = feelsLike,
            minTempTodayC = minTemp,
            maxTempTodayC = maxTemp,
            conditionType = conditionType,
            conditionLabel = conditionLabel,
            symbolCode = symbol,
            humidityPercent = humidity,
            windSpeedKmh = windSpeedKmh,
            windDirectionDeg = windDeg,
            windCardinal = degreesToCardinal(windDeg),
            cloudCoverPercent = cloudCover,
            uvIndex = uv,
            airPressureHpa = pressure,
            dewPointC = dewPoint,
            nextChangeDescription = nextChange
        )
    }

    private fun detectNextWeatherChange(
        upcoming: List<HourlyPoint>,
        currentCondition: WeatherConditionType,
        currentTemp: Double
    ): String {
        if (upcoming.size < 3) return "Conditions remaining steady"

        // Check for rain onset
        val rainOnset = upcoming.firstOrNull { it.precipProbability >= 40 && it.conditionType in listOf(WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN, WeatherConditionType.THUNDERSTORM) }
        if (rainOnset != null) {
            return "Rain expected around ${rainOnset.hourLabel} (${rainOnset.precipProbability}% chance)"
        }

        // Check for clearing if currently rainy or overcast
        if (currentCondition in listOf(WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN, WeatherConditionType.CLOUDY)) {
            val clearing = upcoming.firstOrNull { it.conditionType == WeatherConditionType.CLEAR_DAY || it.conditionType == WeatherConditionType.PARTLY_CLOUDY_DAY }
            if (clearing != null) {
                return "Skies clearing around ${clearing.hourLabel}"
            }
        }

        // Check for significant temperature change (>= 5°C)
        val maxDiff = upcoming.maxByOrNull { Math.abs(it.temperatureC - currentTemp) }
        if (maxDiff != null && Math.abs(maxDiff.temperatureC - currentTemp) >= 5.0) {
            val diff = (maxDiff.temperatureC - currentTemp).roundToInt()
            return if (diff > 0) {
                "Warming up to ${maxDiff.temperatureC.roundToInt()}°C by ${maxDiff.hourLabel}"
            } else {
                "Cooling down to ${maxDiff.temperatureC.roundToInt()}°C by ${maxDiff.hourLabel}"
            }
        }

        // Check for wind pick up
        val highWind = upcoming.firstOrNull { it.windSpeedKmh >= 30 }
        if (highWind != null) {
            return "Breeze picking up to ${highWind.windSpeedKmh.roundToInt()} km/h around ${highWind.hourLabel}"
        }

        return "Stable conditions expected through the evening"
    }

    private fun parseDailyForecast(timeseries: List<MetTimeSeriesItem>, timezoneId: String): List<DailyPoint> {
        val tz = try { TimeZone.getTimeZone(timezoneId) } catch (_: Exception) { TimeZone.getTimeZone("UTC") }
        val groupedByDate = LinkedHashMap<String, MutableList<MetTimeSeriesItem>>()
        val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = tz
        }
        val dayDisplayFormat = SimpleDateFormat("EEE", Locale.US).apply {
            timeZone = tz
        }
        val monthDayFormat = SimpleDateFormat("MMM d", Locale.US).apply {
            timeZone = tz
        }

        for (item in timeseries) {
            try {
                val d = dateParser.parse(item.time)
                if (d != null) {
                    val key = dayKeyFormat.format(d)
                    groupedByDate.getOrPut(key) { mutableListOf() }.add(item)
                }
            } catch (_: Exception) {}
        }

        var index = 0
        return groupedByDate.map { (dateKey, items) ->
            val temps = items.mapNotNull { it.data.instant?.details?.airTemperature }
            val minTemp = temps.minOrNull() ?: 15.0
            val maxTemp = temps.maxOrNull() ?: 22.0

            val rainChances = items.mapNotNull {
                it.data.next1Hours?.details?.probabilityOfPrecipitation
                    ?: it.data.next6Hours?.details?.probabilityOfPrecipitation
            }
            val maxRainProb = rainChances.maxOrNull()?.roundToInt() ?: 0

            val rainMms = items.mapNotNull {
                it.data.next1Hours?.details?.precipitationAmount
                    ?: it.data.next6Hours?.details?.precipitationAmount
            }
            val totalRainMm = rainMms.sum()

            val middayItem = items.getOrNull(items.size / 2)
            val symbol = middayItem?.data?.next1Hours?.summary?.symbolCode
                ?: middayItem?.data?.next6Hours?.summary?.symbolCode
                ?: "clearsky_day"

            val condType = mapSymbolToCondition(symbol)
            val condLabel = mapConditionToDisplayLabel(condType, symbol)

            val parsedDate = try { dayKeyFormat.parse(dateKey) } catch (_: Exception) { null }
            val dayLabel = when (index) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> if (parsedDate != null) dayDisplayFormat.format(parsedDate) else "Day $index"
            }
            val dateDisplay = if (parsedDate != null) monthDayFormat.format(parsedDate) else dateKey

            index++
            DailyPoint(
                dateIso = dateKey,
                dayLabel = dayLabel,
                dateDisplay = dateDisplay,
                minTempC = minTemp,
                maxTempC = maxTemp,
                conditionType = condType,
                symbolCode = symbol,
                conditionLabel = condLabel,
                precipProbability = maxRainProb,
                precipTotalMm = totalRainMm
            )
        }.take(7)
    }

    private fun mapSymbolToCondition(symbolCode: String): WeatherConditionType {
        val s = symbolCode.lowercase()
        return when {
            s.contains("thunder") -> WeatherConditionType.THUNDERSTORM
            s.contains("heavyrain") -> WeatherConditionType.HEAVY_RAIN
            s.contains("rain") || s.contains("drizzle") -> WeatherConditionType.LIGHT_RAIN
            s.contains("snow") -> WeatherConditionType.SNOW
            s.contains("sleet") -> WeatherConditionType.SLEET
            s.contains("fog") -> WeatherConditionType.FOG
            s.contains("cloudy") || s.contains("overcast") -> WeatherConditionType.CLOUDY
            s.contains("partlycloudy") -> {
                if (s.contains("night")) WeatherConditionType.PARTLY_CLOUDY_NIGHT
                else WeatherConditionType.PARTLY_CLOUDY_DAY
            }
            s.contains("clearsky") || s.contains("fair") -> {
                if (s.contains("night")) WeatherConditionType.CLEAR_NIGHT
                else WeatherConditionType.CLEAR_DAY
            }
            else -> WeatherConditionType.PARTLY_CLOUDY_DAY
        }
    }

    private fun mapConditionToDisplayLabel(type: WeatherConditionType, rawSymbol: String): String {
        return when (type) {
            WeatherConditionType.CLEAR_DAY -> "Clear Sky"
            WeatherConditionType.CLEAR_NIGHT -> "Clear Night"
            WeatherConditionType.PARTLY_CLOUDY_DAY -> "Partly Cloudy"
            WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Partly Cloudy"
            WeatherConditionType.CLOUDY -> "Overcast"
            WeatherConditionType.LIGHT_RAIN -> "Light Rain"
            WeatherConditionType.HEAVY_RAIN -> "Heavy Rain"
            WeatherConditionType.THUNDERSTORM -> "Thunderstorms"
            WeatherConditionType.SNOW -> "Snow Showers"
            WeatherConditionType.SLEET -> "Sleet & Ice"
            WeatherConditionType.FOG -> "Misty Fog"
            WeatherConditionType.WINDY -> "Breezy"
        }
    }

    private fun formatHourLabel(isoTime: String, timezoneId: String, symbolCode: String): Pair<String, Boolean> {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val d = parser.parse(isoTime) ?: return Pair("--", true)
            val tz = try { TimeZone.getTimeZone(timezoneId) } catch (_: Exception) { TimeZone.getTimeZone("UTC") }
            val cal = Calendar.getInstance(tz).apply { time = d }
            val hour24 = cal.get(Calendar.HOUR_OF_DAY)
            val label = String.format(Locale.US, "%02d:00", hour24)
            val isDay = when {
                symbolCode.contains("night") -> false
                symbolCode.contains("day") -> true
                else -> hour24 in 6..20
            }
            Pair(label, isDay)
        } catch (_: Exception) {
            Pair("--", true)
        }
    }

    private fun degreesToCardinal(deg: Double): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((deg % 360) + 22.5) / 45).toInt() % 8
        return directions[index]
    }

    private fun formatFreshnessAge(fetchedEpochMs: Long, status: FreshnessStatus): String {
        if (status == FreshnessStatus.REFRESHING) return "Updating live..."
        val diffMin = ((System.currentTimeMillis() - fetchedEpochMs) / (60 * 1000)).toInt()
        return when {
            diffMin <= 1 -> "Updated just now"
            diffMin < 60 -> "Updated $diffMin min ago"
            else -> "Cached (${diffMin / 60}h ago)"
        }
    }

    fun formatCachedAge(fetchedEpochMs: Long): String {
        val diffMin = ((System.currentTimeMillis() - fetchedEpochMs) / (60 * 1000)).toInt()
        return when {
            diffMin <= 1 -> "1 min ago"
            diffMin < 60 -> "$diffMin min ago"
            diffMin < 1440 -> "${diffMin / 60}h ${diffMin % 60}m ago"
            else -> "${diffMin / 1440}d ago"
        }
    }
}

