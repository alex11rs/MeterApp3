package ru.pepega.meterapp3

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.pepega.meterapp3.reminders.isValidReminderTime
import ru.pepega.meterapp3.theme.ThemePreset
import ru.pepega.meterapp3.theme.getThemePreset
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val PREF_FILE_NAME = "meter_data"
private const val PREF_THEME_PRESET = "setting_theme_preset"
private const val PREF_CURRENCY = "setting_currency"
private const val PREF_TARIFFS_ENABLED = "setting_tariffs_enabled"
private const val PREF_SCANNER_ENABLED = "scanner_enabled"
private const val PREF_SCANNER_GOOGLE_API_KEY = "scanner_google_api_key"
private const val PREF_REMINDER_ENABLED = "reminder_enabled"
private const val PREF_REMINDER_DAY_FROM = "reminder_day_from"
private const val PREF_REMINDER_DAY_TO = "reminder_day_to"
private const val PREF_REMINDER_TIME_1 = "reminder_time_1"
private const val PREF_REMINDER_TIME_2 = "reminder_time_2"
private const val PREF_REMINDER_PERMISSIONS_REQUESTED_ONCE = "reminder_permissions_requested_once"
private const val PREF_MAIN_GUIDE_DISMISSED = "main_guide_dismissed"
private const val PREF_SORT_TYPE = "sort_type"
private const val PREF_SHOW_PENDING_THIS_MONTH_ONLY = "show_pending_this_month_only"

data class ReminderSettings(
    val enabled: Boolean,
    val dayFrom: Int?,
    val dayTo: Int?,
    val time1: String,
    val time2: String
)

data class ScannerSettings(
    val enabled: Boolean,
    val apiKey: String
)

data class MainScreenSettings(
    val sortType: SortType,
    val showOnlyPendingThisMonth: Boolean,
    val guideDismissed: Boolean
)

class AppPreferences(
    val sharedPreferences: SharedPreferences
) {
    companion object {
        fun from(context: Context): AppPreferences {
            return AppPreferences(
                context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            )
        }
    }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun themePreset(): ThemePreset = sharedPreferences.getThemePreset()

    fun setThemePreset(themePreset: ThemePreset) {
        sharedPreferences.edit().putString(PREF_THEME_PRESET, themePreset.name).apply()
    }

    fun currency(): String {
        return sharedPreferences.getString(PREF_CURRENCY, "\u20BD") ?: "\u20BD"
    }

    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(PREF_CURRENCY, currency).apply()
    }

    fun tariffsEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_TARIFFS_ENABLED, true)
    }

    fun setTariffsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_TARIFFS_ENABLED, enabled).apply()
    }

    fun scannerSettings(): ScannerSettings {
        val enabled = sharedPreferences.getBoolean(PREF_SCANNER_ENABLED, true)
        val apiKey = sharedPreferences
            .getString(PREF_SCANNER_GOOGLE_API_KEY, "")
            ?.trim()
            .orEmpty()
        return ScannerSettings(enabled = enabled && apiKey.isNotBlank(), apiKey = apiKey)
    }

    fun setScannerEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_SCANNER_ENABLED, enabled).apply()
    }

    fun setScannerApiKey(apiKey: String) {
        sharedPreferences.edit().putString(PREF_SCANNER_GOOGLE_API_KEY, apiKey.trim()).apply()
    }

    fun reminderSettings(): ReminderSettings {
        return ReminderSettings(
            enabled = sharedPreferences.getBoolean(PREF_REMINDER_ENABLED, false),
            dayFrom = sharedPreferences.takeIntIfPresent(PREF_REMINDER_DAY_FROM),
            dayTo = sharedPreferences.takeIntIfPresent(PREF_REMINDER_DAY_TO),
            time1 = sharedPreferences.getString(PREF_REMINDER_TIME_1, "") ?: "",
            time2 = sharedPreferences.getString(PREF_REMINDER_TIME_2, "") ?: ""
        )
    }

    fun setReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_REMINDER_ENABLED, enabled).apply()
    }

    fun setReminderDayRange(dayFrom: Int?, dayTo: Int?) {
        sharedPreferences.edit().apply {
            persistReminderDay(this, PREF_REMINDER_DAY_FROM, dayFrom)
            persistReminderDay(this, PREF_REMINDER_DAY_TO, dayTo)
        }.apply()
    }

    fun setReminderTimes(time1: String, time2: String) {
        sharedPreferences.edit().apply {
            persistReminderTime(this, PREF_REMINDER_TIME_1, time1)
            persistReminderTime(this, PREF_REMINDER_TIME_2, time2)
        }.apply()
    }

    fun reminderPermissionsRequestedOnce(): Boolean {
        return sharedPreferences.getBoolean(PREF_REMINDER_PERMISSIONS_REQUESTED_ONCE, false)
    }

    fun markReminderPermissionsRequested() {
        sharedPreferences.edit().putBoolean(PREF_REMINDER_PERMISSIONS_REQUESTED_ONCE, true).apply()
    }

    fun mainScreenSettings(): MainScreenSettings {
        val rawSortType = sharedPreferences.getString(PREF_SORT_TYPE, SortType.BY_NAME.name)
        val sortType = runCatching {
            SortType.valueOf(rawSortType ?: SortType.BY_NAME.name)
        }.getOrDefault(SortType.BY_NAME)
        return MainScreenSettings(
            sortType = sortType,
            showOnlyPendingThisMonth = sharedPreferences.getBoolean(PREF_SHOW_PENDING_THIS_MONTH_ONLY, false),
            guideDismissed = sharedPreferences.getBoolean(PREF_MAIN_GUIDE_DISMISSED, false)
        )
    }

    fun setSortType(sortType: SortType) {
        sharedPreferences.edit().putString(PREF_SORT_TYPE, sortType.name).apply()
    }

    fun setShowOnlyPendingThisMonth(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_SHOW_PENDING_THIS_MONTH_ONLY, enabled).apply()
    }

    fun dismissMainGuide() {
        sharedPreferences.edit().putBoolean(PREF_MAIN_GUIDE_DISMISSED, true).apply()
    }

    fun markCurrentMonthSubmitted(calendar: Calendar = Calendar.getInstance()) {
        sharedPreferences.edit().putBoolean(submittedMonthKey(calendar), true).apply()
    }

    fun resetCurrentMonthSubmitted(calendar: Calendar = Calendar.getInstance()) {
        sharedPreferences.edit().remove(submittedMonthKey(calendar)).apply()
    }

    fun isCurrentMonthSubmitted(): Boolean = isMonthSubmitted(Calendar.getInstance())

    fun isMonthSubmitted(calendar: Calendar): Boolean {
        return sharedPreferences.getBoolean(submittedMonthKey(calendar), false)
    }

    fun getDistinctReminderTimes(): List<String> {
        val settings = reminderSettings()
        return buildList {
            if (isValidReminderTime(settings.time1)) add(settings.time1)
            if (isValidReminderTime(settings.time2) && settings.time2 != settings.time1) add(settings.time2)
        }
    }

    private fun submittedMonthKey(calendar: Calendar): String {
        val month = SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(calendar.time)
        return "submitted_$month"
    }
}

@Composable
fun rememberAppPreferences(): AppPreferences {
    val context = LocalContext.current
    return remember(context) { AppPreferences.from(context) }
}

private fun SharedPreferences.takeIntIfPresent(key: String): Int? {
    return if (contains(key)) getInt(key, 0) else null
}

private fun persistReminderDay(
    editor: SharedPreferences.Editor,
    key: String,
    value: Int?
) {
    if (value != null && value in 1..31) {
        editor.putInt(key, value)
    } else {
        editor.remove(key)
    }
}

private fun persistReminderTime(
    editor: SharedPreferences.Editor,
    key: String,
    value: String
) {
    val normalizedValue = value.trim()
    if (normalizedValue.isNotBlank()) {
        editor.putString(key, normalizedValue)
    } else {
        editor.remove(key)
    }
}
