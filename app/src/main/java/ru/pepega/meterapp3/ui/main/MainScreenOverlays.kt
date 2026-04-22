package ru.pepega.meterapp3.ui.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.AddMeterDialog
import ru.pepega.meterapp3.MainGuideDialog
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterEditDialog
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.Screen
import ru.pepega.meterapp3.reminders.ReminderManager
import ru.pepega.meterapp3.ui.settings.SettingsDialog

@Composable
fun MainContentDialogs(
    meterPendingDeletion: MeterConfig?,
    showMainGuideDialog: Boolean,
    onDismissDelete: () -> Unit,
    onDeleteConfirmed: (MeterConfig) -> Unit,
    onDismissGuide: () -> Unit
) {
    meterPendingDeletion?.let { meter ->
        DeleteMeterConfirmationDialog(
            meter = meter,
            onDismiss = onDismissDelete,
            onConfirm = {
                onDeleteConfirmed(meter)
            }
        )
    }

    if (showMainGuideDialog) {
        MainGuideDialog(onDismiss = onDismissGuide)
    }
}

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выход") },
        text = { Text("Вы действительно хотите выйти из приложения?") },
        confirmButton = {
            Button(onClick = onExit) {
                Text("Да")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Нет")
            }
        }
    )
}

@Composable
fun SubmissionConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_submission_confirm_title)) },
        text = { Text(stringResource(R.string.reminder_submission_confirm_message)) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.reminder_submission_confirm_action))
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
private fun DeleteMeterConfirmationDialog(
    meter: MeterConfig,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val deleteMessagePrefix = "Вы уверены, что хотите удалить счётчик "
    val deleteMessageSuffix = "? Все его показания и история будут потеряны."

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_meter_title)) },
        text = {
            Text(
                buildAnnotatedString {
                    append(deleteMessagePrefix)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append("\"${meter.name}\"")
                    }
                    append(deleteMessageSuffix)
                }
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun MainScreenSnackbarHost(
    snackbarHostState: SnackbarHostState,
    screen: Screen,
    hasBottomStatusCard: Boolean,
    modifier: Modifier = Modifier
) {
    val bottomPadding = if (screen == Screen.MAIN && hasBottomStatusCard) 124.dp else 16.dp
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { snackbarData ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .widthIn(max = 360.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        },
        modifier = modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding)
    )
}

@Composable
fun MainScreenDialogs(
    reminderManager: ReminderManager,
    configs: List<MeterConfig>,
    showSettings: Boolean,
    showAddMeterDialog: Boolean,
    selectedMeterForEdit: MeterConfig?,
    requestPermission: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRefresh: () -> Unit,
    onDismissSettings: () -> Unit,
    onOpenApartments: () -> Unit,
    onDismissAddMeter: () -> Unit,
    onMeterAdded: () -> Unit,
    onDismissMeterEdit: () -> Unit,
    onSaveMeterConfig: (MeterConfig) -> Unit,
    onResetMeterData: (MeterConfig) -> Unit,
    onMeterUpdated: () -> Unit,
    onMeterDataReset: () -> Unit
) {
    val context = LocalContext.current
    if (showSettings) {
        SettingsDialog(
            reminderManager = reminderManager,
            onDismiss = onDismissSettings,
            requestPermission = requestPermission,
            context = context,
            onShowMessage = onShowMessage,
            onRefresh = onRefresh,
            onOpenApartments = onOpenApartments
        )
    }

    if (showAddMeterDialog) {
        AddMeterDialog(
            existingConfigs = configs,
            onDismiss = onDismissAddMeter,
            onShowMessage = onShowMessage,
            onMeterAdded = onMeterAdded
        )
    }

    if (selectedMeterForEdit != null) {
        MeterEditDialog(
            config = selectedMeterForEdit,
            existingConfigs = configs,
            onDismiss = onDismissMeterEdit,
            onShowMessage = onShowMessage,
            onSave = { updatedConfig ->
                onSaveMeterConfig(updatedConfig)
                onMeterUpdated()
            },
            onResetData = {
                onResetMeterData(selectedMeterForEdit)
                onMeterDataReset()
            }
        )
    }
}
