package com.example.telemetry.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry ORDER BY datetime DESC LIMIT 1")
    fun observeLatest(): Flow<TelemetryEntity?>

    @Query("SELECT * FROM telemetry ORDER BY datetime DESC")
    fun observeHistory(): Flow<List<TelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TelemetryEntity>)

    @Query("DELETE FROM telemetry")
    suspend fun clear()
}
