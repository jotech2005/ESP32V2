package com.example.telemetry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.telemetry.ui.TelemetryApp
import com.example.telemetry.ui.TelemetryViewModel
import com.example.telemetry.ui.TelemetryViewModelFactory
import com.example.telemetry.ui.theme.TelemetryTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TelemetryViewModel by viewModels {
        TelemetryViewModelFactory((application as TelemetryApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TelemetryTheme {
                TelemetryApp(viewModel)
            }
        }
    }
}
