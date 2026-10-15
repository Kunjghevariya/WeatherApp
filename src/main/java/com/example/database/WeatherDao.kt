package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for weather forecasts.
 * Provides methods for inserting, deleting, and querying hourly forecasts
 * by location and time range, as well as managing forecast metadata.
 */
@Dao
interface WeatherDao {

    // ==========================================
    // INSERTION METHODS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(hourlyForecast: HourlyForecast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecasts(hourlyForecasts: List<HourlyForecast>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hourlyForecast: HourlyForecast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hourlyForecasts: List<HourlyForecast>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourly(hourlyForecast: HourlyForecast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourly(hourlyForecasts: List<HourlyForecast>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyEntries(entries: List<HourlyForecast>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ForecastMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastMetadata(metadata: ForecastMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: ForecastMetadata)

    /**
     * Atomically saves forecast metadata and replaces hourly forecast entries.
     */
    @Transaction
    suspend fun saveForecast(
        metadata: ForecastMetadata,
        entries: List<HourlyForecast>
    ) {
        insertMetadata(metadata)
        deleteHourlyForecastsByLocation(metadata.locationKey)
        insertHourlyForecasts(entries)
    }

    // ==========================================
    // QUERY METHODS BY LOCATION AND TIME RANGE
    // ==========================================

    /**
     * Query hourly forecasts for a given location within a specified time range (inclusive),
     * ordered chronologically. Returns a reactive [Flow].
     */
    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getHourlyForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    /**
     * Query hourly forecasts for a given location within a specified time range (inclusive),
     * ordered chronologically. Suspend one-shot query.
     */
    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsByTimeRangeOnce(locationKey: String, startTime: Long, endTime: Long): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getHourlyForecasts(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsOnce(locationKey: String, startTime: Long, endTime: Long): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getHourlyByTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun queryHourlyForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun queryHourlyByLocationAndTimeRange(locationKey: String, startTime: Long, endTime: Long): Flow<List<HourlyForecast>>

    // ==========================================
    // QUERY METHODS BY LOCATION
    // ==========================================

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    fun getHourlyForecastsByLocation(locationKey: String): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsByLocationOnce(locationKey: String): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    fun getHourlyForecasts(locationKey: String): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsOnce(locationKey: String): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    fun getHourlyEntries(locationKey: String): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey ORDER BY timestamp ASC")
    suspend fun getHourlyEntriesOnce(locationKey: String): List<HourlyForecast>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    fun getHourlyForecastsFromTime(locationKey: String, fromTimestamp: Long): Flow<List<HourlyForecast>>

    @Query("SELECT * FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    suspend fun getHourlyForecastsFromTimeOnce(locationKey: String, fromTimestamp: Long): List<HourlyForecast>

    // ==========================================
    // METADATA & RELATION QUERIES
    // ==========================================

    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    fun getForecastMetadata(locationKey: String): Flow<ForecastMetadata?>

    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    suspend fun getForecastMetadataOnce(locationKey: String): ForecastMetadata?

    @Query("SELECT * FROM forecast_metadata ORDER BY last_updated DESC")
    fun getAllForecastMetadata(): Flow<List<ForecastMetadata>>

    @Transaction
    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    fun getForecastWithHourly(locationKey: String): Flow<ForecastWithHourly?>

    @Transaction
    @Query("SELECT * FROM forecast_metadata WHERE location_key = :locationKey LIMIT 1")
    suspend fun getForecastWithHourlyOnce(locationKey: String): ForecastWithHourly?

    // ==========================================
    // DELETION METHODS
    // ==========================================

    @Delete
    suspend fun deleteHourlyForecast(hourlyForecast: HourlyForecast): Int

    @Delete
    suspend fun delete(hourlyForecast: HourlyForecast): Int

    @Delete
    suspend fun delete(hourlyForecasts: List<HourlyForecast>): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteHourlyForecastsByLocation(locationKey: String): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteByLocation(locationKey: String): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteHourlyByLocation(locationKey: String): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteHourlyForecasts(locationKey: String): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey")
    suspend fun deleteHourlyEntriesForLocation(locationKey: String): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun deleteHourlyForecastsByTimeRange(locationKey: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun deleteByLocationAndTimeRange(locationKey: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun deleteByTimeRange(locationKey: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM hourly_forecasts WHERE location_key = :locationKey AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun deleteHourlyByTimeRange(locationKey: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM hourly_forecasts WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun deleteByTimeRange(startTime: Long, endTime: Long): Int

    @Query("DELETE FROM hourly_forecasts")
    suspend fun deleteAllHourlyForecasts(): Int

    @Query("DELETE FROM hourly_forecasts")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM forecast_metadata WHERE location_key = :locationKey")
    suspend fun deleteForecast(locationKey: String): Int

    @Query("DELETE FROM forecast_metadata WHERE location_key = :locationKey")
    suspend fun deleteMetadataByLocation(locationKey: String): Int

    @Query("DELETE FROM forecast_metadata WHERE last_updated < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int
}
