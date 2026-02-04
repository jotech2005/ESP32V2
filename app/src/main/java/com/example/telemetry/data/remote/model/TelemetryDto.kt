package com.example.telemetry.data.remote.model

data class TelemetryDto(
    val datetime: String,
    val uid: String,
    val luz: Int?,
    val temp: Double?,
    val hum: Double?
)
