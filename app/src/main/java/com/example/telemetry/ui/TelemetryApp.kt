package com.example.telemetry.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.telemetry.ui.theme.telemetryGradient
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private enum class TelemetryTab(val label: String) { DASHBOARD("Dashboard"), HISTORY("Histórico") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryApp(viewModel: TelemetryViewModel) {
    val dashboard by viewModel.dashboardState.collectAsState()
    val history by viewModel.historyState.collectAsState()
    var tab by rememberSaveable { mutableStateOf(TelemetryTab.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telemetría", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    TextButton(onClick = { viewModel.refreshDashboard() }) {
                        Text("Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                TelemetryTab.values().forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        label = { Text(item.label) },
                        icon = { }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (tab) {
            TelemetryTab.DASHBOARD -> DashboardScreen(
                state = dashboard,
                modifier = Modifier.padding(innerPadding)
            )
            TelemetryTab.HISTORY -> HistoryScreen(
                data = history,
                onSearch = { uid, from, to -> viewModel.refreshHistory(uid, from, to) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DashboardScreen(state: DashboardUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(telemetryGradient())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(title = "Fecha", value = formatDate(state.latest?.datetime))
            MetricCard(title = "UID", value = state.latest?.uid ?: "—")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(title = "Temperatura", value = state.latest?.temp?.let { "${String.format("%.1f", it)} °C" } ?: "—")
            MetricCard(title = "Humedad", value = state.latest?.hum?.let { "${String.format("%.1f", it)} %" } ?: "—")
        }
        MetricCard(title = "Luz", value = when (state.latest?.luz) { 1 -> "Encendida"; 0 -> "Apagada"; else -> "—" })
        Text("Tendencia (cache si no hay red)", style = MaterialTheme.typography.titleMedium)
        SimpleLineChart(
            seriesA = state.history.mapNotNull { it.temp },
            seriesB = state.history.mapNotNull { it.hum },
            labels = state.history.map { formatDate(it.datetime) },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(top = 4.dp)
        )
        if (state.loading) {
            Text("Cargando…", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        } else if (state.history.isEmpty()) {
            Text("Sin datos en cache todavía", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun HistoryScreen(
    data: List<TelemetryUi>,
    onSearch: (uid: String?, from: String?, to: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var uid by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(telemetryGradient())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Filtrar", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            FilterField(label = "UID", value = uid, onValueChange = { uid = it })
            FilterField(label = "Desde (ISO)", value = from, onValueChange = { from = it })
            FilterField(label = "Hasta (ISO)", value = to, onValueChange = { to = it })
        }
        TextButton(onClick = { onSearch(uid.ifBlank { null }, from.ifBlank { null }, to.ifBlank { null }) }) {
            Text("Buscar")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))) {
                    Column(Modifier.padding(12.dp)) {
                        Text(formatDate(item.datetime), style = MaterialTheme.typography.labelMedium)
                        Text(item.uid ?: "—", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Luz: ${item.luz ?: "—"}")
                            Text("T: ${item.temp ?: "—"}")
                            Text("H: ${item.hum ?: "—"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RowScope.FilterField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun SimpleLineChart(
    seriesA: List<Double>,
    seriesB: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (seriesA.isEmpty() && seriesB.isEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))) {
            Text("Sin datos", modifier = Modifier.padding(12.dp))
        }
        return
    }
    val maxVal = (seriesA + seriesB).maxOrNull() ?: 0.0
    val minVal = (seriesA + seriesB).minOrNull() ?: 0.0
    val span = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))) {
        Canvas(modifier = modifier.padding(12.dp)) {
            val height = size.height
            val width = size.width
            fun pointAt(index: Int, value: Double, count: Int): Offset {
                val x = if (count <= 1) width / 2 else width * (index.toFloat() / (count - 1))
                val yRatio = ((value - minVal) / span).toFloat()
                val y = height - yRatio * height
                return Offset(x, y)
            }

            fun drawSeries(values: List<Double>, color: Color) {
                if (values.isEmpty()) return
                val path = Path()
                values.forEachIndexed { i, v ->
                    val p = pointAt(i, v, values.size)
                    if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                drawPath(path, color = color, style = Stroke(width = 4f, cap = StrokeCap.Round))
            }

            drawSeries(seriesA, Color(0xFF22D3EE))
            drawSeries(seriesB, Color(0xFFA855F7))
        }
    }
}

private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse(raw)
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (ex: DateTimeParseException) {
        raw
    }
}
