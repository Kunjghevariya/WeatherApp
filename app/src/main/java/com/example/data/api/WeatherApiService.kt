package com.example.data.api

import com.example.data.model.AirQualityResponse
import com.example.data.model.MetWeatherResponse
import com.example.data.model.NwsAlertsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MetWeatherApi {
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getLocationForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Header("User-Agent") userAgent: String = "WeatherFlow/1.0 (kunj.ghevariya@gmail.com; android-applet)",
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): Response<MetWeatherResponse>
}

interface NwsAlertsApi {
    @GET("alerts/active")
    suspend fun getActiveAlerts(
        @Query("point") point: String,
        @Header("User-Agent") userAgent: String = "WeatherFlow/1.0 (kunj.ghevariya@gmail.com; android-applet)"
    ): Response<NwsAlertsResponse>
}

interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide,ozone"
    ): Response<AirQualityResponse>
}

object NetworkClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val metWeatherApi: MetWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.met.no/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MetWeatherApi::class.java)
    }

    val nwsAlertsApi: NwsAlertsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.weather.gov/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NwsAlertsApi::class.java)
    }

    val airQualityApi: AirQualityApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AirQualityApi::class.java)
    }
}

