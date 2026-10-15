package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service interface for MET Norway Locationforecast API (Locationforecast 2.0).
 * Base URL: https://api.met.no/
 * Endpoint: weatherapi/locationforecast/2.0/compact
 */
interface WeatherService {

    /**
     * Fetch compact location forecast for a given latitude and longitude.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getLocationForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT
    ): WeatherResponse

    /**
     * Alias for getLocationForecast targeting the compact endpoint.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getCompactForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT
    ): WeatherResponse

    /**
     * General forecast query method.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT
    ): WeatherResponse

    /**
     * Complete location forecast endpoint with extended parameters.
     */
    @GET("weatherapi/locationforecast/2.0/complete")
    suspend fun getCompleteForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT
    ): WeatherResponse

    /**
     * Fetch forecast wrapped in Retrofit Response for HTTP status (e.g. 304 Not Modified)
     * and response headers inspection.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getLocationForecastResponse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): Response<WeatherResponse>

    /**
     * Alias for compact forecast returning Retrofit Response.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getCompactForecastResponse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): Response<WeatherResponse>

    /**
     * Non-coroutine Call for callback-based usage or specific queueing tests.
     */
    @GET("weatherapi/locationforecast/2.0/compact")
    fun getLocationForecastCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int? = null,
        @Header("User-Agent") userAgent: String = DEFAULT_USER_AGENT
    ): Call<WeatherResponse>

    companion object {
        const val BASE_URL = "https://api.met.no/"
        const val DEFAULT_USER_AGENT = "WeatherFlow/1.0 (kunj.ghevariya@gmail.com; android-applet)"

        fun createMoshi(): Moshi {
            return Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }

        fun createOkHttpClient(userAgent: String = DEFAULT_USER_AGENT): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                    if (original.header("User-Agent").isNullOrBlank()) {
                        requestBuilder.header("User-Agent", userAgent)
                    }
                    chain.proceed(requestBuilder.build())
                }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
        }

        fun create(
            baseUrl: String = BASE_URL,
            okHttpClient: OkHttpClient? = null,
            moshi: Moshi? = null
        ): WeatherService {
            val client = okHttpClient ?: createOkHttpClient()
            val jsonConverter = moshi ?: createMoshi()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(jsonConverter))
                .build()
                .create(WeatherService::class.java)
        }
    }
}
