package ru.pepega.meterapp3.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.pepega.meterapp3.rememberAppPreferences
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.reminders.ReminderManager
import ru.pepega.meterapp3.theme.ThemePreset
import ru.pepega.meterapp3.ui.common.AppDialogHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(
    reminderManager: ReminderManager,
    onDismiss: () -> Unit,
    requestPermission: () -> Unit,
    context: Context,
    onShowMessage: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenApartments: () -> Unit
) {
    val appPreferences = rememberAppPreferences()
    val state = rememberSettingsDialogState()
    val sectionContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val sectionContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val themeOptions = remember { ThemePreset.entries.toList() }
    val currencies = listOf("\u20BD", "$", "\u20AC", "\u20B4", "\u20B8")
    val bringIntoViewRequesters = rememberSettingsBringIntoViewRequesters()
    val issueState = rememberSettingsReminderIssueState(reminderManager)
    val reminderActions = rememberSettingsReminderActions(
        context = context,
        appPreferences = appPreferences,
        reminderManager = reminderManager,
        issueState = issueState,
        requestPermission = requestPermission,
        state = state,
        onRefresh = onRefresh
    )
    val backupCoordinator = rememberSettingsBackupCoordinator(
        context = context,
        appPreferences = appPreferences
    )
    val scannerKeyImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val importedKey = importScannerApiKeyFromUri(context, uri)
        if (importedKey.isNullOrBlank()) {
            onShowMessage(context.getString(R.string.scanner_key_import_error))
            return@rememberLauncherForActivityResult
        }
        state.scannerApiKey = importedKey
        state.scannerApiKeyChanged = true
        state.scannerEnabled = true
        state.scannerEnabledChanged = true
        onShowMessage(context.getString(R.string.scanner_key_import_success))
    }
    val backupActions = rememberSettingsBackupActions(
        context = context,
        backupCoordinator = backupCoordinator,
        onImportSelected = state::onImportSelected
    )
    val isCurrentMonthSubmitted = reminderManager.isCurrentMonthSubmitted()
    val reminderPeriodSummary = when {
        state.reminderDayFrom.isBlank() || state.reminderDayTo.isBlank() ->
            stringResource(R.string.reminder_period_not_selected)
        else -> stringResource(
            R.string.reminder_period_selected,
            state.reminderDayFrom,
            state.reminderDayTo
        )
    }
    val reminderTimesSummary = when {
        state.reminderTime1.isBlank() && state.reminderTime2.isBlank() ->
            stringResource(R.string.reminder_time_not_selected)
        state.reminderTime1.isNotBlank() && state.reminderTime2.isNotBlank() ->
            stringResource(
                R.string.reminder_two_times_selected,
                state.reminderTime1,
                state.reminderTime2
            )
        state.reminderTime1.isNotBlank() ->
            stringResource(R.string.reminder_one_time_selected, state.reminderTime1)
        else -> stringResource(R.string.reminder_one_time_selected, state.reminderTime2)
    }

    BindSettingsExpandBringIntoViewEffects(
        state = state,
        requesters = bringIntoViewRequesters
    )

    SettingsDialogOverlays(
        state = state,
        context = context,
        sectionContainerColor = sectionContainerColor,
        issueState = issueState,
        reminderActions = reminderActions,
        backupCoordinator = backupCoordinator,
        onRefresh = onRefresh,
        onDismiss = onDismiss
    )

    val configuration = LocalConfiguration.current
    val dialogScrollState = rememberScrollState()
    val dialogMaxHeight = configuration.screenHeightDp.dp * 0.88f
    val showScrollHint by remember {
        derivedStateOf {
            dialogScrollState.maxValue > 0 && dialogScrollState.value < dialogScrollState.maxValue
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dialogMaxHeight)
            ) {
                AppDialogHeader(
                    title = stringResource(R.string.settings_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            top = 20.dp,
                            end = 20.dp,
                            bottom = if (showScrollHint) 4.dp else 14.dp
                        )
                )
                if (showScrollHint) {
                    Text(
                        text = stringResource(R.string.scroll_down_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp)
                    )
                }

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.Transparent)

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(dialogScrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    ThemeSettingsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequesters.theme),
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        themeOptions = themeOptions,
                        selectedTheme = state.selectedTheme,
                        expanded = state.themeSettingsExpanded,
                        onToggleExpanded = { state.themeSettingsExpanded = !state.themeSettingsExpanded },
                        onThemeSelected = state::onThemeSelected
                    )

                    ApartmentsShortcutCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequesters.tariffs),
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        onOpenApartments = onOpenApartments
                    )

                    TariffSettingsSection(
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        currencies = currencies,
                        currency = state.currency,
                        tariffsEnabled = state.tariffsEnabled,
                        expanded = state.tariffsSettingsExpanded,
                        onToggleExpanded = { state.tariffsSettingsExpanded = !state.tariffsSettingsExpanded },
                        onTariffsEnabledChange = {
                            state.tariffsEnabled = it
                            state.tariffsEnabledChanged = true
                        },
                        onCurrencySelected = {
                            state.currency = it
                            state.currencyChanged = true
                        }
                    )

                    ScannerSettingsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequesters.scanner),
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        scannerEnabled = state.scannerEnabled,
                        scannerApiKey = state.scannerApiKey,
                        hasScannerApiKey = state.scannerApiKey.isNotBlank(),
                        expanded = state.scannerSettingsExpanded,
                        onToggleExpanded = { state.scannerSettingsExpanded = !state.scannerSettingsExpanded },
                        onScannerEnabledChange = {
                            if (it && state.scannerApiKey.isBlank()) {
                                state.scannerEnabled = false
                                state.scannerEnabledChanged = true
                                onShowMessage(context.getString(R.string.scanner_enable_requires_key))
                                return@ScannerSettingsSection
                            }
                            state.scannerEnabled = it
                            state.scannerEnabledChanged = true
                        },
                        onScannerApiKeyChange = {
                            state.scannerApiKey = it
                            if (it.isBlank() && state.scannerEnabled) {
                                state.scannerEnabled = false
                                state.scannerEnabledChanged = true
                            } else if (it.isNotBlank() && !state.scannerEnabled) {
                                state.scannerEnabled = true
                                state.scannerEnabledChanged = true
                            }
                            state.scannerApiKeyChanged = true
                        },
                        onImportScannerKeyClick = {
                            scannerKeyImportLauncher.launch(arrayOf("text/plain", "application/octet-stream", "*/*"))
                        }
                    )

                    ReminderSettingsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequesters.reminder),
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        reminderEnabled = state.reminderEnabled,
                        reminderDayFrom = state.reminderDayFrom,
                        reminderDayTo = state.reminderDayTo,
                        reminderTime1 = state.reminderTime1,
                        reminderTime2 = state.reminderTime2,
                        reminderPeriodSummary = reminderPeriodSummary,
                        reminderTimesSummary = reminderTimesSummary,
                        hasPermissionIssues = issueState.hasAnyIssue,
                        isCurrentMonthSubmitted = isCurrentMonthSubmitted,
                        expanded = state.reminderSettingsExpanded,
                        onToggleExpanded = { state.reminderSettingsExpanded = !state.reminderSettingsExpanded },
                        onShowIssuesClick = { state.showIssuesDialog = true },
                        onReminderEnabledChange = reminderActions.onReminderEnabledChanged,
                        onSelectDayFrom = { state.showDayFromPicker = true },
                        onSelectDayTo = { state.showDayToPicker = true },
                        onSelectTime1 = {
                            openTimePicker(context, state.reminderTime1) {
                                state.reminderTime1 = it
                                state.reminderTimesChanged = true
                            }
                        },
                        onSelectTime2 = {
                            openTimePicker(context, state.reminderTime2) {
                                state.reminderTime2 = it
                                state.reminderTimesChanged = true
                            }
                        },
                        onClearTime2 = if (state.reminderTime2.isNotBlank()) {
                            {
                                state.reminderTime2 = ""
                                state.reminderTimesChanged = true
                            }
                        } else {
                            null
                        },
                        onResetSubmission = reminderActions.onResetSubmission
                    )

                    BackupSettingsSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequesters.backup),
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        expanded = state.backupSettingsExpanded,
                        onToggleExpanded = { state.backupSettingsExpanded = !state.backupSettingsExpanded },
                        onExportClick = backupActions.exportBackup,
                        onExportCsvClick = backupActions.exportCsv,
                        onImportClick = backupActions.importBackup
                    )

                    AboutSettingsCard(
                        sectionContainerColor = sectionContainerColor,
                        sectionContentColor = sectionContentColor,
                        onClick = { state.showAbout = true }
                    )
                }

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.Transparent)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (state.scannerEnabled && state.scannerApiKey.isBlank()) {
                                state.scannerEnabled = false
                                state.scannerEnabledChanged = true
                                onShowMessage(context.getString(R.string.scanner_enable_requires_key))
                            }
                            val hasChanges = persistSettingsChanges(
                                appPreferences = appPreferences,
                                state = state.toSaveState(),
                                reminderManager = reminderManager
                            )
                            if (hasChanges) {
                                onShowMessage(context.getString(R.string.settings_saved))
                            }
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

private fun openTimePicker(
    context: Context,
    initial: String,
    onPicked: (String) -> Unit
) {
    val parts = initial.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 10
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(String.format("%02d:%02d", hour, minute)) },
        initialHour,
        initialMinute,
        true
    ).show()
}
