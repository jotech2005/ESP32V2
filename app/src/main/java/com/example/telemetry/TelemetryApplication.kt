package com.example.telemetry

import android.app.Application
import androidx.room.Room
import com.example.telemetry.data.TelemetryRepository
import com.example.telemetry.data.local.TelemetryDatabase
import com.example.telemetry.data.remote.TelemetryApi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class TelemetryApplication : Application() {
    lateinit var repository: TelemetryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val cacheSize = 5L * 1024L * 1024L
        val cache = Cache(cacheDir, cacheSize)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val api = TelemetryApi.create(BuildConfig.API_BASE, client)
        val db = Room.databaseBuilder(this, TelemetryDatabase::class.java, "telemetry.db")
            .fallbackToDestructiveMigration()
            .build()

        repository = TelemetryRepository(api, db.telemetryDao())
    }
}
