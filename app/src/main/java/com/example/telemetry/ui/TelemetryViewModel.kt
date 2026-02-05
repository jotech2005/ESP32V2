package com.example.telemetry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telemetry.data.TelemetryRepository
import com.example.telemetry.data.local.TelemetryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

data class TelemetryUi(
    val id: Long,
    val rfidTag: String?,
    val temperatura: Double?,
    val humedad: Double?,
    val luzDetectada: Boolean?,
    val fechaCreacion: String?,
    // Campos de compatibilidad
    val datetime: String? = fechaCreacion,
    val uid: String? = rfidTag,
    val luz: Int? = if (luzDetectada == true) 1 else if (luzDetectada == false) 0 else null,
    val temp: Double? = temperatura,
    val hum: Double? = humedad
)

data class DashboardUiState(
    val latest: TelemetryUi? = null,
    val history: List<TelemetryUi> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class TelemetryViewModel(private val repository: TelemetryRepository) : ViewModel() {
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    private val latestFlow = repository.latest.map { it?.toUi() }
    private val historyFlow = repository.history.map { list -> list.map { it.toUi() } }

    val dashboardState: StateFlow<DashboardUiState> = combine(
        latestFlow,
        historyFlow,
        loading,
        error
    ) { latest, history, loadingValue, errorValue ->
        DashboardUiState(latest, history, loadingValue, errorValue)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    val historyState: StateFlow<List<TelemetryUi>> = historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            error.value = null
            runCatching { repository.refreshLatest() }
                .onFailure { error.value = it.message }
            runCatching { repository.refreshHistory(rfidTag = null, from = null, to = null) }
                .onFailure { error.value = it.message }
            loading.value = false
        }
    }

    fun refreshHistory(rfidTag: String?, from: String?, to: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            error.value = null
            runCatching { repository.refreshHistory(rfidTag, from, to) }
                .onFailure { error.value = it.message }
            loading.value = false
        }
    }
}

private fun TelemetryEntity.toUi() = TelemetryUi(
    id = id,
    rfidTag = rfidTag,
    temperatura = temperatura,
    humedad = humedad,
    luzDetectada = luzDetectada,
    fechaCreacion = fechaCreacion
)

class TelemetryViewModelFactory(
    private val repository: TelemetryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TelemetryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TelemetryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
