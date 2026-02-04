package com.example.telemetry.data.remote

import com.example.telemetry.data.remote.model.TelemetryDto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TelemetryApi {
    @GET("telemetry/latest")
    suspend fun latest(): TelemetryDto

    @GET("telemetry")
    suspend fun list(
        @Query("uid") uid: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("sort") sort: String = "desc"
    ): List<TelemetryDto>

    companion object {
        fun create(baseUrl: String, client: OkHttpClient): TelemetryApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(TelemetryApi::class.java)
    }
}
