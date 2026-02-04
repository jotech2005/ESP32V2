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
    val datetime: String,
    val uid: String,
    val luz: Int?,
    val temp: Double?,
    val hum: Double?
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
            runCatching { repository.refreshHistory(uid = null, from = null, to = null) }
                .onFailure { error.value = it.message }
            loading.value = false
        }
    }

    fun refreshHistory(uid: String?, from: String?, to: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            error.value = null
            runCatching { repository.refreshHistory(uid, from, to) }
                .onFailure { error.value = it.message }
            loading.value = false
        }
    }
}

private fun TelemetryEntity.toUi() = TelemetryUi(
    datetime = datetime,
    uid = uid,
    luz = luz,
    temp = temp,
    hum = hum
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
