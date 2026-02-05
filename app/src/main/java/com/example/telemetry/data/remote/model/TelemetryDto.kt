package com.example.telemetry.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Modelo de datos del sensor (coincide con SensorData del backend)
@JsonClass(generateAdapter = true)
data class SensorData(
    val id: Long? = null,
    val rfidTag: String? = null,
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val luzDetectada: Boolean? = null,
    val fechaCreacion: String? = null
)

// Respuesta para un solo registro
@JsonClass(generateAdapter = true)
data class SensorDataResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SensorData? = null,
    val id: Long? = null,
    val fechaCreacion: String? = null
)

// Respuesta para lista de registros
@JsonClass(generateAdapter = true)
data class SensorDataListResponse(
    val success: Boolean,
    val total: Int? = null,
    val limit: Int? = null,
    val message: String? = null,
    val data: List<SensorData>? = null,
    @Json(name = "rfid_tag") val rfidTag: String? = null,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null
)

// Alias para compatibilidad con código existente
typealias TelemetryDto = SensorData
