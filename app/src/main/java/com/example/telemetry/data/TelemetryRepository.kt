package com.example.telemetry.data

import com.example.telemetry.data.local.TelemetryDao
import com.example.telemetry.data.local.TelemetryEntity
import com.example.telemetry.data.remote.TelemetryApi
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

    suspend fun refreshLatest() = withContext(ioDispatcher) {
        val dto = api.latest()
        dao.insertAll(listOf(TelemetryEntity.fromDto(dto)))
    }

    suspend fun refreshHistory(uid: String?, from: String?, to: String?) = withContext(ioDispatcher) {
        val list = api.list(uid = uid, from = from, to = to, limit = 100, sort = "desc")
        dao.clear()
        dao.insertAll(list.map { TelemetryEntity.fromDto(it) })
    }
}
