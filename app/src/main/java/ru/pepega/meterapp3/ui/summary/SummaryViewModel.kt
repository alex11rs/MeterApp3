package ru.pepega.meterapp3.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterRepository
import ru.pepega.meterapp3.MeterStatistics
import ru.pepega.meterapp3.TotalSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SummaryUiState(
    val summary: TotalSummary = TotalSummary(
        totalCost = 0f,
        totalConsumption = 0f,
        meters = emptyList()
    ),
    val hasSummaryCalculation: Boolean = false
)

class SummaryViewModel(
    repository: MeterRepository,
    configs: List<MeterConfig>,
    tariffsEnabled: Boolean
) : ViewModel() {
    val uiState: StateFlow<SummaryUiState> = repository.observeAllMeterData()
        .map { meterDataById ->
            val summary = MeterStatistics.calculateTotalSummary(
                configs = configs,
                tariffsEnabled = tariffsEnabled,
                meterDataById = meterDataById
            )
            SummaryUiState(
                summary = summary,
                hasSummaryCalculation = summary.meters.any { it.lastUpdate > 0L }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState()
        )
}

class SummaryViewModelFactory(
    private val repository: MeterRepository,
    private val configs: List<MeterConfig>,
    private val tariffsEnabled: Boolean
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryViewModel(repository, configs, tariffsEnabled) as T
    }
}
