package com.example

import com.example.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

class WeatherServiceTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun moshi_parsesMetNorwayLocationforecastGeoJsonCorrectly() {
        val sampleJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [10.7579, 59.9139, 14.0]
          },
          "properties": {
            "meta": {
              "updated_at": "2026-10-13T12:00:00Z",
              "units": {
                "air_pressure_at_sea_level": "hPa",
                "air_temperature": "celsius",
                "cloud_area_fraction": "%",
                "precipitation_amount": "mm",
                "relative_humidity": "%",
                "wind_from_direction": "degrees",
                "wind_speed": "m/s"
              }
            },
            "timeseries": [
              {
                "time": "2026-10-13T13:00:00Z",
                "data": {
                  "instant": {
                    "details": {
                      "air_pressure_at_sea_level": 1014.5,
                      "air_temperature": 18.2,
                      "cloud_area_fraction": 35.0,
                      "dew_point_temperature": 9.4,
                      "relative_humidity": 57.0,
                      "wind_from_direction": 225.0,
                      "wind_speed": 4.8,
                      "wind_speed_of_gust": 7.5,
                      "ultraviolet_index_clear_sky": 3.1
                    }
                  },
                  "next_1_hours": {
                    "summary": {
                      "symbol_code": "partlycloudy_day"
                    },
                    "details": {
                      "precipitation_amount": 0.5,
                      "precipitation_amount_max": 0.8,
                      "precipitation_amount_min": 0.2,
                      "probability_of_precipitation": 30.0
                    }
                  },
                  "next_6_hours": {
                    "summary": {
                      "symbol_code": "rain"
                    },
                    "details": {
                      "precipitation_amount": 2.4,
                      "probability_of_precipitation": 65.0
                    }
                  },
                  "next_12_hours": {
                    "summary": {
                      "symbol_code": "cloudy"
                    }
                  }
                }
              }
            ]
          }
        }
        """.trimIndent()

        val adapter = moshi.adapter(WeatherResponse::class.java)
        val response = adapter.fromJson(sampleJson)

        assertNotNull("Response must not be null", response)
        assertEquals("Feature", response?.type)

        // Geometry
        val geometry = response?.geometry
        assertNotNull(geometry)
        assertEquals("Point", geometry?.type)
        assertEquals(3, geometry?.coordinates?.size)
        assertEquals(10.7579, geometry?.longitude ?: 0.0, 0.0001)
        assertEquals(59.9139, geometry?.latitude ?: 0.0, 0.0001)
        assertEquals(14.0, geometry?.altitude ?: 0.0, 0.0001)

        // Properties and Meta
        val props = response?.properties
        assertNotNull(props)
        assertEquals("2026-10-13T12:00:00Z", props?.meta?.updatedAt)
        assertEquals("celsius", props?.meta?.units?.get("air_temperature"))
        assertEquals("m/s", props?.meta?.units?.get("wind_speed"))

        // Timeseries
        val timeseries = props?.timeseries
        assertNotNull(timeseries)
        assertEquals(1, timeseries?.size)

        val firstItem = timeseries?.first()
        assertEquals("2026-10-13T13:00:00Z", firstItem?.time)

        // Instant Details
        val instantDetails = firstItem?.data?.instant?.details
        assertNotNull(instantDetails)
        assertEquals(18.2, instantDetails?.airTemperature ?: 0.0, 0.01)
        assertEquals(18.2, instantDetails?.temperature ?: 0.0, 0.01)
        assertEquals(4.8, instantDetails?.windSpeed ?: 0.0, 0.01)
        assertEquals(4.8, instantDetails?.wind ?: 0.0, 0.01)
        assertEquals(57.0, instantDetails?.relativeHumidity ?: 0.0, 0.01)
        assertEquals(57.0, instantDetails?.humidity ?: 0.0, 0.01)
        assertEquals(1014.5, instantDetails?.airPressureAtSeaLevel ?: 0.0, 0.01)
        assertEquals(9.4, instantDetails?.dewPointTemperature ?: 0.0, 0.01)
        assertEquals(225.0, instantDetails?.windFromDirection ?: 0.0, 0.01)

        // Next 1 Hours Period Forecast
        val next1 = firstItem?.data?.next1Hours
        assertNotNull(next1)
        assertEquals("partlycloudy_day", next1?.summary?.symbolCode)
        assertEquals("partlycloudy_day", next1?.summary?.symbol)
        assertEquals(0.5, next1?.details?.precipitationAmount ?: 0.0, 0.01)
        assertEquals(0.5, next1?.details?.precipitation ?: 0.0, 0.01)
        assertEquals(30.0, next1?.details?.probabilityOfPrecipitation ?: 0.0, 0.01)
        assertEquals(30.0, next1?.details?.precipitationProbability ?: 0.0, 0.01)

        // Next 6 Hours
        val next6 = firstItem?.data?.next6Hours
        assertNotNull(next6)
        assertEquals("rain", next6?.summary?.symbolCode)
        assertEquals(2.4, next6?.details?.precipitationAmount ?: 0.0, 0.01)
    }

    @Test
    fun weatherService_interfaceHasCorrectRetrofitAnnotations() {
        val clazz = WeatherService::class.java
        assertTrue("WeatherService must be an interface", clazz.isInterface)

        val getLocForecastMethod = clazz.methods.firstOrNull { it.name == "getLocationForecast" }
        assertNotNull("WeatherService must declare getLocationForecast", getLocForecastMethod)

        val getAnnotation = getLocForecastMethod?.getAnnotation(GET::class.java)
        assertNotNull("getLocationForecast must have @GET annotation", getAnnotation)
        assertEquals(
            "Endpoint must match MET Norway locationforecast compact API path",
            "weatherapi/locationforecast/2.0/compact",
            getAnnotation?.value
        )

        // Verify Query parameter annotations exist
        val parameterAnnotations = getLocForecastMethod?.parameterAnnotations ?: emptyArray()
        val queryNames = parameterAnnotations.flatMap { it.toList() }
            .filterIsInstance<Query>()
            .map { it.value }

        assertTrue("Query parameter 'lat' must be present", queryNames.contains("lat"))
        assertTrue("Query parameter 'lon' must be present", queryNames.contains("lon"))

        // Verify User-Agent Header annotation exists
        val headerNames = parameterAnnotations.flatMap { it.toList() }
            .filterIsInstance<Header>()
            .map { it.value }

        assertTrue("Header 'User-Agent' must be supported", headerNames.contains("User-Agent"))
    }

    @Test
    fun weatherService_createFactoryReturnsValidProxy() {
        val service = WeatherService.create()
        assertNotNull("WeatherService.create() must return an instance", service)

        val clientService = WeatherNetworkClient.weatherService
        assertNotNull("WeatherNetworkClient.weatherService must be initialized", clientService)
    }

    @Test
    fun okHttpClient_injectsDefaultUserAgentHeaderWhenMissing() {
        val client = WeatherService.createOkHttpClient("CustomApp/2.0 (test@example.com)")
        var interceptedUserAgent: String? = null

        // Build a mock request through an interceptor to verify header injection
        val testClient = client.newBuilder()
            .addInterceptor { chain ->
                interceptedUserAgent = chain.request().header("User-Agent")
                okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(okhttp3.ResponseBody.create(null, "{}"))
                    .build()
            }
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=60.0&lon=10.0")
            .build()

        testClient.newCall(request).execute()

        assertEquals("CustomApp/2.0 (test@example.com)", interceptedUserAgent)
    }
}
