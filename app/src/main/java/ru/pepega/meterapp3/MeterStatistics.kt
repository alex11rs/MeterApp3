package ru.pepega.meterapp3

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MeterStatistics {

    fun getDayConsumption(config: MeterConfig, data: MeterData): Float {
        return maxOf(0f, data.current - data.previous)
    }

    fun getNightConsumption(config: MeterConfig, data: MeterData): Float {
        return if (config.tariffType == MeterTariffType.DUAL) {
            maxOf(0f, data.currentNight - data.previousNight)
        } else 0f
    }

    fun getTotalConsumption(config: MeterConfig, data: MeterData): Float {
        return getDayConsumption(config, data) + getNightConsumption(config, data)
    }

    fun getTotalCost(config: MeterConfig, data: MeterData): Float {
        val dayCost = getDayConsumption(config, data) * data.tariff
        val nightCost = if (config.tariffType == MeterTariffType.DUAL) {
            getNightConsumption(config, data) * data.nightTariff
        } else 0f
        return dayCost + nightCost
    }

    fun getConsumptionTrend(config: MeterConfig, data: MeterData): Int {
        val currentConsumption = getTotalConsumption(config, data)
        val previousConsumption = getPreviousPeriodConsumption(config, data) ?: return 0

        return when {
            currentConsumption > previousConsumption -> 1
            currentConsumption < previousConsumption -> -1
            else -> 0
        }
    }

    private fun getPreviousPeriodConsumption(config: MeterConfig, data: MeterData): Float? {
        val lastHistoryEntry = data.history.lastOrNull() ?: return null
        val previousHistoryEntry = data.history.dropLast(1).lastOrNull() ?: return null

        val dayConsumption = maxOf(0f, lastHistoryEntry.value - previousHistoryEntry.value)
        val nightConsumption = if (config.tariffType == MeterTariffType.DUAL) {
            val lastNightValue = lastHistoryEntry.secondaryValue ?: 0f
            val previousNightValue = previousHistoryEntry.secondaryValue ?: 0f
            maxOf(0f, lastNightValue - previousNightValue)
        } else 0f

        return dayConsumption + nightConsumption
    }

    fun getMonthHistory(
        configs: List<MeterConfig>,
        tariffsEnabled: Boolean,
        meterDataById: Map<String, MeterData>
    ): List<MonthHistory> {
        val months = mutableMapOf<String, MutableList<MeterHistoryItem>>()
        val monthFormat = SimpleDateFormat("MM.yyyy", Locale.getDefault())

        configs.filter { it.enabled }.forEach { meter ->
            val data = meterDataById[meter.id] ?: MeterData()

            val allReadings = buildList {
                addAll(data.history)
                if (data.lastUpdate > 0) {
                    add(MeterReadingHistoryEntry(
                        value = data.current,
                        timestamp = data.lastUpdate,
                        secondaryValue = if (meter.tariffType == MeterTariffType.DUAL) data.currentNight else null
                    ))
                }
            }
                .sortedBy { it.timestamp }
                .groupBy { monthFormat.format(Date(it.timestamp)) }
                .map { (_, entries) -> entries.last() }
                .sortedBy { it.timestamp }

            allReadings.forEachIndexed { index, entry ->
                val prev = allReadings.getOrNull(index - 1)
                val monthKey = monthFormat.format(Date(entry.timestamp))

                val dayConsumption = if (prev != null) maxOf(0f, entry.value - prev.value) else 0f
                val nightConsumption = if (meter.tariffType == MeterTariffType.DUAL && prev != null) {
                    maxOf(0f, (entry.secondaryValue ?: 0f) - (prev.secondaryValue ?: 0f))
                } else 0f
                val consumption = dayConsumption + nightConsumption
                val cost = if (tariffsEnabled) {
                    dayConsumption * data.tariff + nightConsumption * data.nightTariff
                } else 0f

                months.getOrPut(monthKey) { mutableListOf() }.add(
                    MeterHistoryItem(
                        icon = meter.icon,
                        name = meter.name,
                        reading = entry.value,
                        nightReading = entry.secondaryValue ?: 0f,
                        unit = meter.unit,
                        consumption = consumption,
                        dayConsumption = dayConsumption,
                        nightConsumption = nightConsumption,
                        tariffType = meter.tariffType,
                        cost = cost,
                        date = entry.timestamp
                    )
                )
            }
        }

        return months.entries
            .map { MonthHistory(month = it.key, meters = it.value) }
            .sortedByDescending { monthFormat.parse(it.month)?.time ?: Long.MIN_VALUE }
    }

    fun calculateTotalSummary(
        configs: List<MeterConfig>,
        tariffsEnabled: Boolean,
        meterDataById: Map<String, MeterData>
    ): TotalSummary {
        var totalCost = 0f
        var totalConsumption = 0f
        val meterSummaries = mutableListOf<MeterSummary>()

        configs.filter { it.enabled }.forEach { meter ->
            val data = meterDataById[meter.id] ?: MeterData()
            val dayConsumption = getDayConsumption(meter, data)
            val nightConsumption = getNightConsumption(meter, data)
            val consumption = dayConsumption + nightConsumption
            val cost = getTotalCost(meter, data)
            totalConsumption += consumption
            if (tariffsEnabled) totalCost += cost
            meterSummaries.add(
                MeterSummary(
                    id = meter.id,
                    icon = meter.icon,
                    name = meter.name,
                    consumption = consumption,
                    dayConsumption = dayConsumption,
                    nightConsumption = nightConsumption,
                    currentReading = data.current,
                    currentNightReading = data.currentNight,
                    tariffType = meter.tariffType,
                    unit = meter.unit,
                    cost = cost,
                    lastUpdate = data.lastUpdate
                )
            )
        }

        return TotalSummary(
            totalCost = totalCost,
            totalConsumption = totalConsumption,
            meters = meterSummaries
        )
    }
}
