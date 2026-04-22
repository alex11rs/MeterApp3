package ru.pepega.meterapp3.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.AboutDialog
import ru.pepega.meterapp3.MainGuideDialog
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.ui.settings.components.DayNumberPickerDialog

@Composable
fun SettingsDialogOverlays(
    state: SettingsDialogState,
    context: Context,
    sectionContainerColor: Color,
    issueState: SettingsReminderIssueState,
    reminderActions: SettingsReminderActions,
    backupCoordinator: SettingsBackupCoordinator,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state.showRestartDialog) {
        SettingsRestartDialog(
            onDismiss = { state.showRestartDialog = false },
            onRestartNow = {
                state.persistSelectedTheme()
                val intent = (context as? ComponentActivity)?.intent
                (context as? ComponentActivity)?.finish()
                (context as? ComponentActivity)?.startActivity(intent)
                state.showRestartDialog = false
                onDismiss()
            },
            onLater = {
                state.persistSelectedTheme()
                state.showRestartDialog = false
            }
        )
    }

    SettingsAboutDialog(
        showAbout = state.showAbout,
        onShowGuide = { state.showGuide = true },
        onDismiss = { state.showAbout = false }
    )
    SettingsGuideDialog(
        showGuide = state.showGuide,
        onDismiss = { state.showGuide = false }
    )

    SettingsDayPickerDialogs(
        showDayFromPicker = state.showDayFromPicker,
        showDayToPicker = state.showDayToPicker,
        reminderDayFrom = state.reminderDayFrom,
        reminderDayTo = state.reminderDayTo,
        onDismissDayFrom = { state.showDayFromPicker = false },
        onDismissDayTo = { state.showDayToPicker = false },
        onClearDayFrom = state::clearDayFrom,
        onClearDayTo = state::clearDayTo,
        onSelectDayFrom = state::selectDayFrom,
        onSelectDayTo = state::selectDayTo
    )

    SettingsImportConfirmDialog(
        showImportConfirm = state.showImportConfirm,
        pendingImportUri = state.pendingImportUri,
        importBackup = backupCoordinator.importBackup,
        onDismiss = state::clearPendingImport,
        onImportSuccess = {
            state.clearPendingImport()
            onRefresh()
            onDismiss()
        }
    )

    SettingsIssuesDialog(
        showIssuesDialog = state.showIssuesDialog,
        issueState = issueState,
        sectionContainerColor = sectionContainerColor,
        reminderActions = reminderActions,
        onDismiss = { state.showIssuesDialog = false }
    )
}

@Composable
fun SettingsRestartDialog(
    onDismiss: () -> Unit,
    onRestartNow: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.restart_required_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.restart_required_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onRestartNow,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.restart_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(stringResource(R.string.later))
            }
        }
    )
}

@Composable
fun SettingsDayPickerDialogs(
    showDayFromPicker: Boolean,
    showDayToPicker: Boolean,
    reminderDayFrom: String,
    reminderDayTo: String,
    onDismissDayFrom: () -> Unit,
    onDismissDayTo: () -> Unit,
    onClearDayFrom: () -> Unit,
    onClearDayTo: () -> Unit,
    onSelectDayFrom: (Int) -> Unit,
    onSelectDayTo: (Int) -> Unit
) {
    if (showDayFromPicker) {
        DayNumberPickerDialog(
            title = stringResource(R.string.reminder_select_day_from),
            selectedDay = reminderDayFrom.toIntOrNull(),
            onDismiss = onDismissDayFrom,
            onClear = onClearDayFrom,
            onSelect = onSelectDayFrom
        )
    }

    if (showDayToPicker) {
        DayNumberPickerDialog(
            title = stringResource(R.string.reminder_select_day_to),
            selectedDay = reminderDayTo.toIntOrNull(),
            onDismiss = onDismissDayTo,
            onClear = onClearDayTo,
            onSelect = onSelectDayTo
        )
    }
}

@Composable
fun SettingsImportConfirmDialog(
    showImportConfirm: Boolean,
    pendingImportUri: Uri?,
    importBackup: (Uri, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onImportSuccess: () -> Unit
) {
    if (!showImportConfirm || pendingImportUri == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_import_title)) },
        text = { Text(stringResource(R.string.backup_import_confirm)) },
        confirmButton = {
            Button(
                onClick = {
                    importBackup(pendingImportUri, onImportSuccess, onDismiss)
                }
            ) {
                Text(stringResource(R.string.restore_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsIssuesDialog(
    showIssuesDialog: Boolean,
    issueState: SettingsReminderIssueState,
    sectionContainerColor: Color,
    reminderActions: SettingsReminderActions,
    onDismiss: () -> Unit
) {
    if (!showIssuesDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stable_work_settings_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.stable_work_settings_message))

                if (issueState.hasAutoStartIssue) {
                    IssueCard(
                        title = stringResource(R.string.autostart_title),
                        message = stringResource(R.string.autostart_message),
                        sectionContainerColor = sectionContainerColor,
                        primaryAction = {
                            reminderActions.openAutoStartSettings()
                            onDismiss()
                        },
                        primaryLabel = stringResource(R.string.open_autostart_settings)
                    )
                }

                if (issueState.hasNotificationIssue) {
                    IssueCard(
                        title = stringResource(R.string.notifications_title),
                        message = stringResource(R.string.notifications_message),
                        sectionContainerColor = sectionContainerColor,
                        primaryAction = {
                            reminderActions.openNotificationSettings()
                            onDismiss()
                        },
                        primaryLabel = stringResource(R.string.open_notification_settings)
                    )
                }

                if (issueState.hasBatteryIssue) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.battery_optimization_title), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.battery_optimization_enabled),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    reminderActions.requestDisableBatteryOptimizations()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.disable_via_request))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    reminderActions.openBatterySettings()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.open_battery_settings))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.understand))
            }
        }
    )
}

@Composable
private fun IssueCard(
    title: String,
    message: String,
    sectionContainerColor: Color,
    primaryAction: () -> Unit,
    primaryLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = primaryAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(primaryLabel)
            }
        }
    }
}

@Composable
fun SettingsAboutDialog(
    showAbout: Boolean,
    onShowGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showAbout) {
        AboutDialog(
            onShowGuide = onShowGuide,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun SettingsGuideDialog(
    showGuide: Boolean,
    onDismiss: () -> Unit
) {
    if (showGuide) {
        MainGuideDialog(onDismiss = onDismiss)
    }
}
