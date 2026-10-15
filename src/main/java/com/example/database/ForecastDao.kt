package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for [ForecastMetadata] and [HourlyForecast] records.
 */
@Dao
interface ForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ForecastMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecasts(entries: List<HourlyForecast>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(entry: HourlyForecast)

    /**
     * Atomically clears existing hourly entries for the given location
     * and persists the new metadata and hourly forecast entries.
     */
    @Transaction
    suspend fun saveForecast(
        metadata: ForecastMetadata,
        entries: List<HourlyForecast>
    ) {
        insertMetadata(metadata)
        deleteHourlyEntriesForLocation(metadata.locationKey)
        insertHourlyForecasts(entries)
    }

    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    fun getForecastMetadata(locationKey: String): Flow<ForecastMetadata?>

    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    suspend fun getForecastMetadataOnce(locationKey: String): ForecastMetadata?

    @Query("SELECT * FROM forecast_metadata ORDER BY last_updated DESC")
    fun getAllForecastMetadata(): Flow<List<ForecastMetadata>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    fun getHourlyForecasts(locationKey: String): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsOnce(locationKey: String): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    fun getHourlyForecastsFromTime(locationKey: String, fromTimestamp: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getHourlyForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsByTimeRangeOnce(locationKey: String, startTime: Long, endTime: Long): List<HourlyForecast>

    @Transaction
    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    fun getForecastWithHourly(locationKey: String): Flow<ForecastWithHourly?>

    @Transaction
    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    suspend fun getForecastWithHourlyOnce(locationKey: String): ForecastWithHourly?

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteHourlyEntriesForLocation(locationKey: String)

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun deleteHourlyForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM forecast_metadata WHERE location_key = :locationKey")
    suspend fun deleteForecast(locationKey: String)

    @Query("DELETE FROM forecast_metadata WHERE last_updated < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    suspend fun insertHourlyEntries(entries: List<HourlyForecast>) = insertHourlyForecasts(entries)
    fun getHourlyEntries(locationKey: String): Flow<List<HourlyForecast>> = getHourlyForecasts(locationKey)
    suspend fun getHourlyEntriesOnce(locationKey: String): List<HourlyForecast> = getHourlyForecastsOnce(locationKey)
}
