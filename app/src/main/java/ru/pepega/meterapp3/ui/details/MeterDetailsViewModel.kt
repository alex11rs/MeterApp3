package ru.pepega.meterapp3.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterRepository
import ru.pepega.meterapp3.MeterStatistics
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.MeterReadingHistoryEntry
import ru.pepega.meterapp3.VerificationInfo
import ru.pepega.meterapp3.getVerificationInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ConsumptionChartPoint(
    val label: String,
    val value: Float
)

data class MeterDetailsUiState(
    val meterData: MeterData = MeterData(),
    val allReadings: List<Pair<Long, Float>> = emptyList(),
    val totalConsumptionChartPoints: List<ConsumptionChartPoint> = emptyList(),
    val dayConsumptionChartPoints: List<ConsumptionChartPoint> = emptyList(),
    val nightConsumptionChartPoints: List<ConsumptionChartPoint> = emptyList(),
    val totalReadings: Int = 0,
    val averageConsumption: Float = 0f,
    val minConsumption: Float = 0f,
    val maxConsumption: Float = 0f,
    val totalCost: Float = 0f,
    val verificationInfo: VerificationInfo = getVerificationInfo(0L, 0)
)

class MeterDetailsViewModel(
    private val repository: MeterRepository,
    private val meterConfig: MeterConfig
) : ViewModel() {
    val uiState: StateFlow<MeterDetailsUiState> = repository.observeMeterData(meterConfig.id)
        .map { meterData -> buildUiState(meterConfig, meterData) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildUiState(meterConfig, MeterData())
        )

    private fun buildUiState(
        meterConfig: MeterConfig,
        meterData: MeterData
    ): MeterDetailsUiState {
        val allReadings = buildAllReadings(meterConfig, meterData)
        val consumptionValues = buildConsumptionValues(allReadings)
        val chartPoints = buildConsumptionChartPoints(meterConfig, meterData)
        return MeterDetailsUiState(
            meterData = meterData,
            allReadings = allReadings,
            totalConsumptionChartPoints = chartPoints.total,
            dayConsumptionChartPoints = chartPoints.day,
            nightConsumptionChartPoints = chartPoints.night,
            totalReadings = allReadings.size,
            averageConsumption = if (consumptionValues.isNotEmpty()) {
                consumptionValues.sum() / consumptionValues.size
            } else 0f,
            minConsumption = consumptionValues.minOrNull() ?: 0f,
            maxConsumption = consumptionValues.maxOrNull() ?: 0f,
            totalCost = MeterStatistics.getTotalCost(meterConfig, meterData),
            verificationInfo = getVerificationInfo(meterConfig.verificationDate, meterConfig.validityYears)
        )
    }

    private fun buildAllReadings(
        meterConfig: MeterConfig,
        meterData: MeterData
    ): List<Pair<Long, Float>> {
        val readings = mutableListOf<Pair<Long, Float>>()
        if (meterData.lastUpdate > 0) {
            readings.add(
                meterData.lastUpdate to (
                    meterData.current + if (meterConfig.tariffType == ru.pepega.meterapp3.MeterTariffType.DUAL) {
                        meterData.currentNight
                    } else 0f
                )
            )
        }
        meterData.history
            .asReversed()
            .filter { it.timestamp > 0 }
            .forEach { entry ->
                readings.add(entry.timestamp to (entry.value + (entry.secondaryValue ?: 0f)))
            }
        return readings.sortedByDescending { it.first }
    }

    private fun buildConsumptionValues(allReadings: List<Pair<Long, Float>>): List<Float> {
        if (allReadings.size <= 1) return emptyList()
        return buildList {
            for (index in 0 until allReadings.lastIndex) {
                add(allReadings[index].second - allReadings[index + 1].second)
            }
        }
    }

    private data class ChartPointSets(
        val total: List<ConsumptionChartPoint>,
        val day: List<ConsumptionChartPoint>,
        val night: List<ConsumptionChartPoint>
    )

    private fun buildConsumptionChartPoints(
        meterConfig: MeterConfig,
        meterData: MeterData
    ): ChartPointSets {
        val entries = mutableListOf<MeterReadingHistoryEntry>()
        if (meterData.lastUpdate > 0) {
            entries.add(
                MeterReadingHistoryEntry(
                    value = meterData.current,
                    timestamp = meterData.lastUpdate,
                    secondaryValue = if (meterConfig.tariffType == MeterTariffType.DUAL) {
                        meterData.currentNight
                    } else {
                        null
                    }
                )
            )
        }
        entries.addAll(meterData.history.asReversed().filter { it.timestamp > 0 })
        if (entries.size <= 1) return ChartPointSets(emptyList(), emptyList(), emptyList())

        val dateFormat = SimpleDateFormat("MM.yy", Locale.getDefault())
        val total = buildList {
            for (index in 0 until entries.lastIndex) {
                val current = entries[index]
                val next = entries[index + 1]
                add(
                    ConsumptionChartPoint(
                        label = dateFormat.format(Date(current.timestamp)),
                        value = ((current.value - next.value) + ((current.secondaryValue ?: 0f) - (next.secondaryValue ?: 0f))).coerceAtLeast(0f)
                    )
                )
            }
        }.asReversed().takeLast(6)
        val day = buildList {
            for (index in 0 until entries.lastIndex) {
                val current = entries[index]
                val next = entries[index + 1]
                add(
                    ConsumptionChartPoint(
                        label = dateFormat.format(Date(current.timestamp)),
                        value = (current.value - next.value).coerceAtLeast(0f)
                    )
                )
            }
        }.asReversed().takeLast(6)
        val night = if (meterConfig.tariffType == MeterTariffType.DUAL) {
            buildList {
                for (index in 0 until entries.lastIndex) {
                    val current = entries[index]
                    val next = entries[index + 1]
                    add(
                        ConsumptionChartPoint(
                            label = dateFormat.format(Date(current.timestamp)),
                            value = ((current.secondaryValue ?: 0f) - (next.secondaryValue ?: 0f)).coerceAtLeast(0f)
                        )
                    )
                }
            }.asReversed().takeLast(6)
        } else {
            emptyList()
        }
        return ChartPointSets(
            total = total,
            day = day,
            night = night
        )
    }
}

class MeterDetailsViewModelFactory(
    private val repository: MeterRepository,
    private val meterConfig: MeterConfig
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeterDetailsViewModel(repository, meterConfig) as T
    }
}
