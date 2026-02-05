package com.example.telemetry.data

import com.example.telemetry.data.local.TelemetryDao
import com.example.telemetry.data.local.TelemetryEntity
import com.example.telemetry.data.remote.TelemetryApi
import com.example.telemetry.data.remote.model.SensorData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TelemetryRepository(
    private val api: TelemetryApi,
    private val dao: TelemetryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val latest: Flow<TelemetryEntity?> = dao.observeLatest()
    val history: Flow<List<TelemetryEntity>> = dao.observeHistory()

    suspend fun refreshLatest(limit: Int = 1) = withContext(ioDispatcher) {
        val response = api.getLatestReadings(limit)
        if (response.success && !response.data.isNullOrEmpty()) {
            dao.insertAll(response.data.map { TelemetryEntity.fromSensorData(it) })
        }
    }

    suspend fun refreshHistory(rfidTag: String?, from: String?, to: String?) = withContext(ioDispatcher) {
        val response = if (!rfidTag.isNullOrBlank()) {
            api.getDataByRfid(rfidTag)
        } else if (!from.isNullOrBlank() && !to.isNullOrBlank()) {
            api.getDataByDateRange(from, to)
        } else {
            api.getAllSensorData()
        }
        
        if (response.success && !response.data.isNullOrEmpty()) {
            dao.clear()
            dao.insertAll(response.data.map { TelemetryEntity.fromSensorData(it) })
        }
    }

    suspend fun getLatestReadings(limit: Int) = withContext(ioDispatcher) {
        api.getLatestReadings(limit)
    }

    suspend fun getDataWithLightDetected() = withContext(ioDispatcher) {
        api.getDataWithLightDetected()
    }

    suspend fun createSensorData(sensorData: SensorData) = withContext(ioDispatcher) {
        api.createSensorData(sensorData)
    }

    suspend fun updateSensorData(id: Long, sensorData: SensorData) = withContext(ioDispatcher) {
        api.updateSensorData(id, sensorData)
    }

    suspend fun deleteSensorData(id: Long) = withContext(ioDispatcher) {
        api.deleteSensorData(id)
    }

    suspend fun healthCheck() = withContext(ioDispatcher) {
        api.healthCheck()
    }
}
