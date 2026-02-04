package com.example.telemetry.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF22D3EE),
    secondary = Color(0xFFA855F7),
    background = Color(0xFF0B1021),
    surface = Color(0xFF111827),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun TelemetryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}

fun telemetryGradient(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF0B1224), Color(0xFF0F172A)),
    start = Offset(0f, 0f),
    end = Offset(800f, 800f)
)
