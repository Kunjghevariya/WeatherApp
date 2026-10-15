package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton network client and factory for WeatherService and Moshi JSON parsing.
 */
object WeatherNetworkClient {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val okHttpClient: OkHttpClient by lazy {
        WeatherService.createOkHttpClient()
    }

    val weatherService: WeatherService by lazy {
        createWeatherService()
    }

    fun createWeatherService(
        baseUrl: String = WeatherService.BASE_URL,
        client: OkHttpClient = okHttpClient,
        jsonMoshi: Moshi = moshi
    ): WeatherService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(jsonMoshi))
            .build()
            .create(WeatherService::class.java)
    }
}
