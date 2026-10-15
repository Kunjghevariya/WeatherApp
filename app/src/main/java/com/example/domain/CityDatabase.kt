package com.example.domain

data class CityItem(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String
)

object CuratedCities {
    val defaultList = listOf(
        CityItem("london_gb", "London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        CityItem("new_york_us", "New York", "United States", 40.7128, -74.0060, "America/New_York"),
        CityItem("tokyo_jp", "Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
        CityItem("paris_fr", "Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
        CityItem("sydney_au", "Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        CityItem("oslo_no", "Oslo", "Norway", 59.9139, 10.7522, "Europe/Oslo"),
        CityItem("berlin_de", "Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        CityItem("san_francisco_us", "San Francisco", "United States", 37.7749, -122.4194, "America/Los_Angeles"),
        CityItem("singapore_sg", "Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        CityItem("toronto_ca", "Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        CityItem("mumbai_in", "Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        CityItem("dubai_ae", "Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
        CityItem("seoul_kr", "Seoul", "South Korea", 37.5665, 126.9780, "Asia/Seoul"),
        CityItem("rio_br", "Rio de Janeiro", "Brazil", -22.9068, -43.1729, "America/Sao_Paulo"),
        CityItem("cape_town_za", "Cape Town", "South Africa", -33.9249, 18.4241, "Africa/Johannesburg"),
        CityItem("chicago_us", "Chicago", "United States", 41.8781, -87.6298, "America/Chicago"),
        CityItem("los_angeles_us", "Los Angeles", "United States", 34.0522, -118.2437, "America/Los_Angeles"),
        CityItem("seattle_us", "Seattle", "United States", 47.6062, -122.3321, "America/Los_Angeles"),
        CityItem("miami_us", "Miami", "United States", 25.7617, -80.1918, "America/New_York"),
        CityItem("madrid_es", "Madrid", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
        CityItem("rome_it", "Rome", "Italy", 41.9028, 12.4964, "Europe/Rome"),
        CityItem("amsterdam_nl", "Amsterdam", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam"),
        CityItem("stockholm_se", "Stockholm", "Sweden", 59.3293, 18.0686, "Europe/Stockholm"),
        CityItem("zurich_ch", "Zurich", "Switzerland", 47.3769, 8.5417, "Europe/Zurich"),
        CityItem("cairo_eg", "Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        CityItem("bangkok_th", "Bangkok", "Thailand", 13.7563, 100.5018, "Asia/Bangkok"),
        CityItem("buenos_aires_ar", "Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires"),
        CityItem("auckland_nz", "Auckland", "New Zealand", -36.8485, 174.7633, "Pacific/Auckland"),
        CityItem("reykjavik_is", "Reykjavik", "Iceland", 64.1466, -21.9426, "Atlantic/Reykjavik"),
        CityItem("honolulu_us", "Honolulu", "United States", 21.3069, -157.8583, "Pacific/Honolulu")
    )

    fun search(query: String): List<CityItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return defaultList.take(12)
        return defaultList.filter {
            it.name.lowercase().contains(q) || it.country.lowercase().contains(q)
        }
    }

    suspend fun searchWorldwide(query: String): List<CityItem> {
        val q = query.trim()
        if (q.isEmpty()) return defaultList.take(12)
        val onlineResults = com.example.data.api.GeocodingClient.searchCityWorldwide(q)
        if (onlineResults.isNotEmpty()) {
            return onlineResults
        }
        return search(q)
    }
}

