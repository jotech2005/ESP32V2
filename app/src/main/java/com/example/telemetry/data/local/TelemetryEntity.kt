package com.example.telemetry.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.telemetry.data.remote.model.SensorData

@Entity(tableName = "telemetry")
data class TelemetryEntity(
    @PrimaryKey val id: Long,
    val rfidTag: String?,
    val temperatura: Double?,
    val humedad: Double?,
    val luzDetectada: Boolean?,
    val fechaCreacion: String?
) {
    companion object {
        fun fromSensorData(data: SensorData) = TelemetryEntity(
            id = data.id ?: 0L,
            rfidTag = data.rfidTag,
            temperatura = data.temperatura,
            humedad = data.humedad,
            luzDetectada = data.luzDetectada,
            fechaCreacion = data.fechaCreacion
        )

        // Alias para compatibilidad
        fun fromDto(dto: SensorData) = fromSensorData(dto)
    }
}
