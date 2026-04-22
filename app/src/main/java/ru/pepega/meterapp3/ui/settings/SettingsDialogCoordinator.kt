package ru.pepega.meterapp3.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.backup.BackupManager
import ru.pepega.meterapp3.rememberAppPreferences
import ru.pepega.meterapp3.reminders.ReminderManager
import ru.pepega.meterapp3.ui.common.showShortUserMessage

@Immutable
data class SettingsReminderIssueState(
    val hasAutoStartIssue: Boolean,
    val hasNotificationIssue: Boolean,
    val hasBatteryIssue: Boolean
) {
    val hasAnyIssue: Boolean
        get() = hasAutoStartIssue || hasNotificationIssue || hasBatteryIssue
}

@Immutable
data class SettingsReminderActions(
    val onReminderEnabledChanged: (Boolean) -> Unit,
    val onResetSubmission: () -> Unit,
    val openAutoStartSettings: () -> Unit,
    val openNotificationSettings: () -> Unit,
    val requestDisableBatteryOptimizations: () -> Unit,
    val openBatterySettings: () -> Unit
)

@Immutable
data class SettingsBackupCoordinator(
    val exportBackup: (Uri) -> Unit,
    val exportCsv: (Uri) -> Unit,
    val importBackup: (Uri, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit
)

@Composable
fun rememberSettingsReminderIssueState(
    reminderManager: ReminderManager
): SettingsReminderIssueState {
    return remember(reminderManager) {
        SettingsReminderIssueState(
            hasAutoStartIssue = reminderManager.isXiaomiDevice() && !reminderManager.isAutoStartEnabled(),
            hasNotificationIssue = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !reminderManager.hasNotificationPermission(),
            hasBatteryIssue = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !reminderManager.isBatteryOptimizationDisabled()
        )
    }
}

@Composable
fun rememberSettingsReminderActions(
    context: Context,
    appPreferences: AppPreferences = rememberAppPreferences(),
    reminderManager: ReminderManager,
    issueState: SettingsReminderIssueState,
    requestPermission: () -> Unit,
    state: SettingsDialogState,
    onRefresh: () -> Unit
): SettingsReminderActions {
    return remember(
        context,
        appPreferences,
        reminderManager,
        issueState,
        requestPermission,
        state,
        onRefresh
    ) {
        SettingsReminderActions(
            onReminderEnabledChanged = { enabled ->
                state.reminderEnabled = enabled
                state.reminderEnabledChanged = true
                if (enabled && !appPreferences.reminderPermissionsRequestedOnce()) {
                    requestPermission()
                    if (issueState.hasAnyIssue) {
                        state.showIssuesDialog = true
                    }
                    appPreferences.markReminderPermissionsRequested()
                }
            },
            onResetSubmission = {
                reminderManager.resetCurrentMonthSubmitted()
                showShortUserMessage(context, context.getString(R.string.reminder_submission_reset_done))
                onRefresh()
            },
            openAutoStartSettings = {
                reminderManager.openAutoStartSettings(context as ComponentActivity)
            },
            openNotificationSettings = {
                reminderManager.openAppNotificationSettings(context as ComponentActivity)
            },
            requestDisableBatteryOptimizations = {
                reminderManager.requestDisableBatteryOptimizations(context as ComponentActivity)
            },
            openBatterySettings = {
                reminderManager.openBatterySettings(context as ComponentActivity)
            }
        )
    }
}

@Composable
fun rememberSettingsBackupCoordinator(
    context: Context,
    appPreferences: AppPreferences = rememberAppPreferences()
): SettingsBackupCoordinator {
    val scope = rememberCoroutineScope()
    return remember(context, appPreferences, scope) {
        SettingsBackupCoordinator(
            exportBackup = { uri ->
                scope.launch {
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            BackupManager.exportBackup(context, uri, appPreferences.sharedPreferences)
                        }
                    }
                    result.onSuccess {
                        showShortUserMessage(context, context.getString(R.string.backup_export_success))
                    }.onFailure {
                        showShortUserMessage(context, context.getString(R.string.backup_export_error))
                    }
                }
            },
            exportCsv = { uri ->
                scope.launch {
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            BackupManager.exportCsv(context, uri, appPreferences.sharedPreferences)
                        }
                    }
                    result.onSuccess {
                        showShortUserMessage(context, context.getString(R.string.csv_export_success))
                    }.onFailure {
                        showShortUserMessage(context, context.getString(R.string.backup_export_error))
                    }
                }
            },
            importBackup = { uri, onSuccess, onFailure ->
                scope.launch {
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            BackupManager.importBackup(context, uri, appPreferences.sharedPreferences)
                        }
                    }
                    result.onSuccess {
                        showShortUserMessage(context, context.getString(R.string.backup_import_success))
                        onSuccess()
                    }.onFailure {
                        showShortUserMessage(context, context.getString(R.string.backup_import_error))
                        onFailure()
                    }
                }
            }
        )
    }
}
