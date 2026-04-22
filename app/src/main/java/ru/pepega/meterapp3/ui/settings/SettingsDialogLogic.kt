package ru.pepega.meterapp3.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.rememberAppPreferences
import ru.pepega.meterapp3.reminders.ReminderManager
import ru.pepega.meterapp3.theme.ThemePreset
import kotlinx.coroutines.delay
import java.io.IOException

@Immutable
data class SettingsBackupActions(
    val exportBackup: () -> Unit,
    val exportCsv: () -> Unit,
    val importBackup: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Immutable
data class SettingsBringIntoViewRequesters(
    val theme: BringIntoViewRequester,
    val tariffs: BringIntoViewRequester,
    val scanner: BringIntoViewRequester,
    val reminder: BringIntoViewRequester,
    val backup: BringIntoViewRequester
)

@Stable
class SettingsDialogState(
    private val appPreferences: AppPreferences
) {
    private val reminderSettings = appPreferences.reminderSettings()
    private val scannerSettings = appPreferences.scannerSettings()

    var selectedTheme by mutableStateOf(appPreferences.themePreset())
    var themeChanged by mutableStateOf(false)
    var currency by mutableStateOf(appPreferences.currency())
    var currencyChanged by mutableStateOf(false)
    var tariffsEnabled by mutableStateOf(appPreferences.tariffsEnabled())
    var tariffsEnabledChanged by mutableStateOf(false)
    var reminderEnabled by mutableStateOf(reminderSettings.enabled)
    var reminderEnabledChanged by mutableStateOf(false)
    var reminderDayFrom by mutableStateOf(reminderSettings.dayFrom?.toString().orEmpty())
    var reminderDayTo by mutableStateOf(reminderSettings.dayTo?.toString().orEmpty())
    var reminderDaysChanged by mutableStateOf(false)
    var reminderTime1 by mutableStateOf(reminderSettings.time1.takeIf { it.isNotBlank() } ?: "")
    var reminderTime2 by mutableStateOf(reminderSettings.time2.takeIf { it.isNotBlank() } ?: "")
    var reminderTimesChanged by mutableStateOf(false)
    var scannerEnabled by mutableStateOf(scannerSettings.enabled)
    var scannerEnabledChanged by mutableStateOf(false)
    var scannerApiKey by mutableStateOf(scannerSettings.apiKey)
    var scannerApiKeyChanged by mutableStateOf(false)
    var themeSettingsExpanded by mutableStateOf(false)
    var tariffsSettingsExpanded by mutableStateOf(false)
    var scannerSettingsExpanded by mutableStateOf(false)
    var reminderSettingsExpanded by mutableStateOf(false)
    var backupSettingsExpanded by mutableStateOf(false)
    var showAbout by mutableStateOf(false)
    var showGuide by mutableStateOf(false)
    var showRestartDialog by mutableStateOf(false)
    var showIssuesDialog by mutableStateOf(false)
    var pendingImportUri by mutableStateOf<Uri?>(null)
    var showImportConfirm by mutableStateOf(false)
    var showDayFromPicker by mutableStateOf(false)
    var showDayToPicker by mutableStateOf(false)

    fun onThemeSelected(themePreset: ThemePreset) {
        if (selectedTheme != themePreset) {
            selectedTheme = themePreset
            themeChanged = true
            showRestartDialog = true
        }
    }

    fun persistSelectedTheme() {
        appPreferences.setThemePreset(selectedTheme)
        themeChanged = false
    }

    fun onImportSelected(uri: Uri) {
        pendingImportUri = uri
        showImportConfirm = true
    }

    fun clearPendingImport() {
        showImportConfirm = false
        pendingImportUri = null
    }

    fun clearDayFrom() {
        reminderDayFrom = ""
        reminderDaysChanged = true
        showDayFromPicker = false
    }

    fun clearDayTo() {
        reminderDayTo = ""
        reminderDaysChanged = true
        showDayToPicker = false
    }

    fun selectDayFrom(day: Int) {
        reminderDayFrom = day.toString()
        if (reminderDayTo.toIntOrNull()?.let { it < day } == true) {
            reminderDayTo = day.toString()
        }
        reminderDaysChanged = true
        showDayFromPicker = false
    }

    fun selectDayTo(day: Int) {
        reminderDayTo = day.toString()
        if (reminderDayFrom.toIntOrNull()?.let { it > day } == true) {
            reminderDayFrom = day.toString()
        }
        reminderDaysChanged = true
        showDayToPicker = false
    }

    fun toSaveState(): SettingsSaveState {
        return SettingsSaveState(
            selectedTheme = selectedTheme,
            themeChanged = themeChanged,
            currency = currency,
            currencyChanged = currencyChanged,
            tariffsEnabled = tariffsEnabled,
            tariffsEnabledChanged = tariffsEnabledChanged,
            reminderEnabled = reminderEnabled,
            reminderEnabledChanged = reminderEnabledChanged,
            reminderDayFrom = reminderDayFrom,
            reminderDayTo = reminderDayTo,
            reminderDaysChanged = reminderDaysChanged,
            reminderTime1 = reminderTime1,
            reminderTime2 = reminderTime2,
            reminderTimesChanged = reminderTimesChanged,
            scannerEnabled = scannerEnabled,
            scannerEnabledChanged = scannerEnabledChanged,
            scannerApiKey = scannerApiKey,
            scannerApiKeyChanged = scannerApiKeyChanged
        )
    }
}

@Composable
fun rememberSettingsDialogState(): SettingsDialogState {
    val appPreferences = rememberAppPreferences()
    return remember(appPreferences) { SettingsDialogState(appPreferences) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSettingsBringIntoViewRequesters(): SettingsBringIntoViewRequesters {
    return remember {
        SettingsBringIntoViewRequesters(
            theme = BringIntoViewRequester(),
            tariffs = BringIntoViewRequester(),
            scanner = BringIntoViewRequester(),
            reminder = BringIntoViewRequester(),
            backup = BringIntoViewRequester()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BindSettingsExpandBringIntoViewEffects(
    state: SettingsDialogState,
    requesters: SettingsBringIntoViewRequesters
) {
    BringSectionIntoViewWhenExpanded(
        expanded = state.themeSettingsExpanded,
        requester = requesters.theme
    )
    BringSectionIntoViewWhenExpanded(
        expanded = state.tariffsSettingsExpanded,
        requester = requesters.tariffs
    )
    BringSectionIntoViewWhenExpanded(
        expanded = state.scannerSettingsExpanded,
        requester = requesters.scanner
    )
    BringSectionIntoViewWhenExpanded(
        expanded = state.reminderSettingsExpanded,
        requester = requesters.reminder
    )
    BringSectionIntoViewWhenExpanded(
        expanded = state.backupSettingsExpanded,
        requester = requesters.backup
    )
}

data class SettingsSaveState(
    val selectedTheme: ThemePreset,
    val themeChanged: Boolean,
    val currency: String,
    val currencyChanged: Boolean,
    val tariffsEnabled: Boolean,
    val tariffsEnabledChanged: Boolean,
    val reminderEnabled: Boolean,
    val reminderEnabledChanged: Boolean,
    val reminderDayFrom: String,
    val reminderDayTo: String,
    val reminderDaysChanged: Boolean,
    val reminderTime1: String,
    val reminderTime2: String,
    val reminderTimesChanged: Boolean,
    val scannerEnabled: Boolean,
    val scannerEnabledChanged: Boolean,
    val scannerApiKey: String,
    val scannerApiKeyChanged: Boolean
) {
    val hasChanges: Boolean
        get() = themeChanged ||
            currencyChanged ||
            tariffsEnabledChanged ||
            reminderEnabledChanged ||
            reminderDaysChanged ||
            reminderTimesChanged ||
            scannerEnabledChanged ||
            scannerApiKeyChanged

    val shouldRefreshReminders: Boolean
        get() = reminderEnabledChanged || reminderDaysChanged || reminderTimesChanged
}

@Composable
fun rememberSettingsBackupActions(
    context: Context,
    backupCoordinator: SettingsBackupCoordinator,
    onImportSelected: (Uri) -> Unit
): SettingsBackupActions {
    val exportLauncher = rememberBackupExportLauncher(
        context = context,
        backupCoordinator = backupCoordinator
    )
    val exportCsvLauncher = rememberCsvExportLauncher(
        context = context,
        backupCoordinator = backupCoordinator
    )
    val importLauncher = rememberBackupImportLauncher(onImportSelected)

    return SettingsBackupActions(
        exportBackup = {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                .format(java.util.Date())
            exportLauncher.launch("meterapp_backup_$timestamp.json")
        },
        exportCsv = {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                .format(java.util.Date())
            exportCsvLauncher.launch("meterapp_history_$timestamp.csv")
        },
        importBackup = {
            importLauncher.launch(arrayOf("application/json", "text/plain"))
        }
    )
}

fun persistSettingsChanges(
    appPreferences: AppPreferences,
    state: SettingsSaveState,
    reminderManager: ReminderManager
): Boolean {
    val normalizedScannerApiKey = state.scannerApiKey.trim()
    val scannerEnabled = state.scannerEnabled && normalizedScannerApiKey.isNotBlank()

    if (state.themeChanged) appPreferences.setThemePreset(state.selectedTheme)
    if (state.currencyChanged) appPreferences.setCurrency(state.currency)
    if (state.tariffsEnabledChanged) appPreferences.setTariffsEnabled(state.tariffsEnabled)
    if (state.scannerEnabledChanged || (state.scannerEnabled && normalizedScannerApiKey.isBlank())) {
        appPreferences.setScannerEnabled(scannerEnabled)
    }
    if (state.scannerApiKeyChanged) appPreferences.setScannerApiKey(normalizedScannerApiKey)
    if (state.reminderEnabledChanged) appPreferences.setReminderEnabled(state.reminderEnabled)
    if (state.reminderDaysChanged) {
        appPreferences.setReminderDayRange(
            dayFrom = state.reminderDayFrom.toIntOrNull(),
            dayTo = state.reminderDayTo.toIntOrNull()
        )
    }
    if (state.reminderTimesChanged) {
        appPreferences.setReminderTimes(
            time1 = state.reminderTime1,
            time2 = state.reminderTime2
        )
    }

    if (state.shouldRefreshReminders) {
        reminderManager.refreshReminderSchedule()
    }

    return state.hasChanges
}

fun importScannerApiKeyFromUri(
    context: Context,
    uri: Uri
): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readText().trim()
        }?.ifBlank { null }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }
}

@Composable
private fun rememberCsvExportLauncher(
    context: Context,
    backupCoordinator: SettingsBackupCoordinator
): ManagedActivityResultLauncher<String, Uri?> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            backupCoordinator.exportCsv(uri)
        }
    }
}

@Composable
private fun rememberBackupExportLauncher(
    context: Context,
    backupCoordinator: SettingsBackupCoordinator
): ManagedActivityResultLauncher<String, Uri?> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            backupCoordinator.exportBackup(uri)
        }
    }
}

@Composable
private fun rememberBackupImportLauncher(
    onImportSelected: (Uri) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportSelected(uri)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BringSectionIntoViewWhenExpanded(
    expanded: Boolean,
    requester: BringIntoViewRequester
) {
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(120)
            requester.bringIntoView()
        }
    }
}
