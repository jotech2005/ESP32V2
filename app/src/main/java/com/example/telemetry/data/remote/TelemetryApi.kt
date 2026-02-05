package com.example.telemetry.data.remote

import com.example.telemetry.data.remote.model.SensorDataResponse
import com.example.telemetry.data.remote.model.SensorDataListResponse
import com.example.telemetry.data.remote.model.SensorData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface TelemetryApi {
    // GET: Obtener todos los datos
    @GET("api/sensor-data")
    suspend fun getAllSensorData(): SensorDataListResponse

    // GET: Obtener por ID
    @GET("api/sensor-data/{id}")
    suspend fun getSensorDataById(@Path("id") id: Long): SensorDataResponse

    // GET: Últimas N lecturas
    @GET("api/sensor-data/latest/{limit}")
    suspend fun getLatestReadings(@Path("limit") limit: Int): SensorDataListResponse

    // GET: Buscar por RFID
    @GET("api/sensor-data/rfid/{rfidTag}")
    suspend fun getDataByRfid(@Path("rfidTag") rfidTag: String): SensorDataListResponse

    // GET: Buscar por rango de fechas
    @GET("api/sensor-data/date-range")
    suspend fun getDataByDateRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): SensorDataListResponse

    // GET: Datos con luz detectada
    @GET("api/sensor-data/light-detected")
    suspend fun getDataWithLightDetected(): SensorDataListResponse

    // POST: Crear nuevo registro
    @POST("api/sensor-data")
    suspend fun createSensorData(@Body sensorData: SensorData): SensorDataResponse

    // PUT: Actualizar registro
    @PUT("api/sensor-data/{id}")
    suspend fun updateSensorData(@Path("id") id: Long, @Body sensorData: SensorData): SensorDataResponse

    // DELETE: Eliminar registro
    @DELETE("api/sensor-data/{id}")
    suspend fun deleteSensorData(@Path("id") id: Long): SensorDataResponse

    // GET: Estadísticas - total de registros
    @GET("api/sensor-data/stats/total-records")
    suspend fun getTotalRecords(): Map<String, Any>

    // GET: Health check
    @GET("api/sensor-data/health")
    suspend fun healthCheck(): Map<String, Any>

    companion object {
        fun create(baseUrl: String, client: OkHttpClient): TelemetryApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(TelemetryApi::class.java)
    }
}
