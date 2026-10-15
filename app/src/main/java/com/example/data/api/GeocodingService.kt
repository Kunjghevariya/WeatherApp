package com.example.data.api

import com.example.domain.CityItem
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeocodingLocation(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    @Json(name = "timezone") val timezoneId: String? = "UTC"
)

data class GeocodingResponse(
    val results: List<GeocodingLocation>? = null
)

interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

object GeocodingClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val api: GeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApi::class.java)
    }

    suspend fun searchCityWorldwide(query: String): List<CityItem> {
        if (query.trim().length < 2) return emptyList()
        return try {
            val response = api.searchLocations(name = query.trim())
            response.results?.map { loc ->
                val displayCountry = listOfNotNull(loc.admin1, loc.country).joinToString(", ")
                CityItem(
                    id = "geo_${loc.id}",
                    name = loc.name,
                    country = if (displayCountry.isNotEmpty()) displayCountry else (loc.country ?: "Global"),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timezoneId = loc.timezoneId ?: "UTC"
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
