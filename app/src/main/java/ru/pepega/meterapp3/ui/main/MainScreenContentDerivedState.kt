package ru.pepega.meterapp3.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.SortType
import ru.pepega.meterapp3.TotalSummary
import ru.pepega.meterapp3.rememberAppPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MainScreenDerivedState(
    val visibleMeters: List<MeterConfig>,
    val showBottomStatusCard: Boolean,
    val hasSummaryCalculation: Boolean,
    val currentMonthPrepositional: String,
    val reminderEnabled: Boolean,
    val bottomStatusCardState: BottomStatusCardUiState
)

@Composable
fun rememberMainScreenDerivedState(
    configs: List<MeterConfig>,
    meterDataById: Map<String, MeterData>,
    summary: TotalSummary,
    sortType: SortType,
    showOnlyPendingThisMonth: Boolean,
    refreshTrigger: Int = 0
): MainScreenDerivedState {
    val appPreferences = rememberAppPreferences()
    val monthCalendar = remember { Calendar.getInstance() }
    val currentMonth = monthCalendar.get(Calendar.MONTH)
    val currentYear = monthCalendar.get(Calendar.YEAR)

    val sortedMeters = remember(configs, meterDataById, sortType) {
        sortMeters(
            meters = configs.filter { it.enabled },
            meterDataById = meterDataById,
            sortType = sortType
        )
    }
    val showBottomStatusCard = sortedMeters.isNotEmpty()
    val visibleMeters = remember(sortedMeters, meterDataById, showOnlyPendingThisMonth) {
        if (showOnlyPendingThisMonth) {
            sortedMeters.filterNot { meter ->
                isUpdatedThisMonth(
                    meterDataById = meterDataById,
                    meter = meter,
                    currentMonth = currentMonth,
                    currentYear = currentYear
                )
            }
    } else {
            sortedMeters
        }
    }
    val updatedThisMonthCount = remember(sortedMeters, meterDataById) {
        sortedMeters.count { meter ->
            isUpdatedThisMonth(
                meterDataById = meterDataById,
                meter = meter,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
    }
    val hasSummaryCalculation = remember(summary.meters) {
        summary.meters.any { it.lastUpdate > 0L }
    }
    val remainingThisMonthCount = sortedMeters.size - updatedThisMonthCount
    val currentMonthLabel = remember {
        val ruLocale = Locale.forLanguageTag("ru")
        SimpleDateFormat("LLLL", ruLocale).format(Date()).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(ruLocale) else char.toString()
        }
    }
    val currentMonthPrepositional = remember(currentMonth) {
        russianMonthPrepositionalLabel(currentMonth)
    }
    val reminderSettings = appPreferences.reminderSettings()
    val reminderEnabled = reminderSettings.enabled
    val reminderDayFrom = reminderSettings.dayFrom ?: 0
    val reminderDayTo = reminderSettings.dayTo ?: 0
    val reminderTime1 = reminderSettings.time1
    val reminderTime2 = reminderSettings.time2
    val isCurrentMonthSubmitted = appPreferences.isCurrentMonthSubmitted()
    val bottomStatusCardState = rememberBottomStatusCardUiState(
        currentMonthLabel = currentMonthLabel,
        updatedThisMonthCount = updatedThisMonthCount,
        totalMetersCount = sortedMeters.size,
        remainingThisMonthCount = remainingThisMonthCount,
        reminderEnabled = reminderEnabled,
        reminderDayFrom = reminderDayFrom,
        reminderDayTo = reminderDayTo,
        reminderTime1 = reminderTime1,
        reminderTime2 = reminderTime2,
        isCurrentMonthSubmitted = isCurrentMonthSubmitted
    )

    return MainScreenDerivedState(
        visibleMeters = visibleMeters,
        showBottomStatusCard = showBottomStatusCard,
        hasSummaryCalculation = hasSummaryCalculation,
        currentMonthPrepositional = currentMonthPrepositional,
        reminderEnabled = reminderEnabled,
        bottomStatusCardState = bottomStatusCardState
    )
}

private fun sortMeters(
    meters: List<MeterConfig>,
    meterDataById: Map<String, MeterData>,
    sortType: SortType
): List<MeterConfig> {
    return when (sortType) {
        SortType.BY_NAME -> meters.sortedBy { it.name }
        SortType.BY_LAST_UPDATE -> meters.sortedByDescending {
            meterDataById[it.id]?.lastUpdate ?: 0L
        }
        SortType.BY_CONSUMPTION -> meters.sortedByDescending {
            val data = meterDataById[it.id] ?: MeterData()
            data.current - data.previous
        }
    }
}

private fun isUpdatedThisMonth(
    meterDataById: Map<String, MeterData>,
    meter: MeterConfig,
    currentMonth: Int,
    currentYear: Int
): Boolean {
    val lastUpdate = meterDataById[meter.id]?.lastUpdate ?: 0L
    if (lastUpdate <= 0L) return false

    val updateCalendar = Calendar.getInstance().apply { timeInMillis = lastUpdate }
    return updateCalendar.get(Calendar.MONTH) == currentMonth &&
        updateCalendar.get(Calendar.YEAR) == currentYear
}

private fun russianMonthPrepositionalLabel(month: Int): String {
    val monthName = when (month) {
        Calendar.JANUARY -> "январе"
        Calendar.FEBRUARY -> "феврале"
        Calendar.MARCH -> "марте"
        Calendar.APRIL -> "апреле"
        Calendar.MAY -> "мае"
        Calendar.JUNE -> "июне"
        Calendar.JULY -> "июле"
        Calendar.AUGUST -> "августе"
        Calendar.SEPTEMBER -> "сентябре"
        Calendar.OCTOBER -> "октябре"
        Calendar.NOVEMBER -> "ноябре"
        Calendar.DECEMBER -> "декабре"
        else -> "месяце"
    }
    return monthName.replaceFirstChar { char ->
        val ruLocale = Locale.forLanguageTag("ru")
        if (char.isLowerCase()) char.titlecase(ruLocale) else char.toString()
    }
}
