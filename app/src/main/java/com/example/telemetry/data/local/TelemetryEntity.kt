package com.example.telemetry.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.telemetry.data.remote.model.TelemetryDto

@Entity(tableName = "telemetry")
data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val datetime: String,
    val uid: String,
    val luz: Int?,
    val temp: Double?,
    val hum: Double?
) {
    companion object {
        fun fromDto(dto: TelemetryDto) = TelemetryEntity(
            datetime = dto.datetime,
            uid = dto.uid,
            luz = dto.luz,
            temp = dto.temp,
            hum = dto.hum
        )
    }
}
