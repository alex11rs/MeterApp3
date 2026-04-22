package ru.pepega.meterapp3.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MonthHistory
import ru.pepega.meterapp3.MeterRepository
import ru.pepega.meterapp3.MeterStatistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val history: List<MonthHistory> = emptyList()
)

class HistoryViewModel(
    repository: MeterRepository,
    configs: List<MeterConfig>,
    tariffsEnabled: Boolean
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = repository.observeAllMeterDataWithFullHistory()
        .map { meterDataById ->
            HistoryUiState(
                history = MeterStatistics.getMonthHistory(
                    configs = configs,
                    tariffsEnabled = tariffsEnabled,
                    meterDataById = meterDataById
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )
}

class HistoryViewModelFactory(
    private val repository: MeterRepository,
    private val configs: List<MeterConfig>,
    private val tariffsEnabled: Boolean
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(repository, configs, tariffsEnabled) as T
    }
}
