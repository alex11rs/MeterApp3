package ru.pepega.meterapp3

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.ui.reading.AddReadingHeader
import ru.pepega.meterapp3.ui.reading.AddReadingInputSection
import ru.pepega.meterapp3.ui.reading.AddReadingSubmitResult
import ru.pepega.meterapp3.ui.reading.AddReadingSummaryCard
import ru.pepega.meterapp3.ui.reading.AddReadingViewModel
import ru.pepega.meterapp3.ui.reading.AddReadingViewModelFactory
import ru.pepega.meterapp3.ui.reading.MeterVerificationInputLockedCard
import ru.pepega.meterapp3.ui.reading.MeterVerificationStatusCard
import ru.pepega.meterapp3.ui.common.formatPlainNumber
import kotlinx.coroutines.launch

@Composable
fun AddReadingScreen(
    meterConfig: MeterConfig,
    initialText: String? = null,
    initialNightText: String? = null,
    onBack: () -> Unit,
    onScanClick: (ReadingField) -> Unit,
    onSaved: (String) -> Unit,
    onInfoClick: (MeterConfig) -> Unit,
    onEditClick: (MeterConfig) -> Unit,
    onDeleted: (String) -> Unit
) {
    val context = LocalContext.current
    val appPreferences = rememberAppPreferences()
    val repository = rememberMeterRepository()
    val scannerEnabled = remember(appPreferences) { appPreferences.scannerSettings().enabled }
    val viewModel: AddReadingViewModel = rememberViewModel(
        repository,
        meterConfig,
        initialText,
        initialNightText
    ) {
        AddReadingViewModelFactory(
            appContext = context.applicationContext,
            repository = repository,
            meterConfig = meterConfig,
            initialText = initialText,
            initialNightText = initialNightText
        )
    }
    val state by viewModel.screenState.collectAsState()
    val verificationInfo = state.verificationInfo
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialText, initialNightText) {
        viewModel.applyInitialReadings(initialText, initialNightText)
    }

    suspend fun handleSubmitResult(result: AddReadingSubmitResult) {
        when (result) {
            is AddReadingSubmitResult.Saved -> {
                focusManager.clearFocus()
                vibrate(context)
                kotlinx.coroutines.delay(500)
                onSaved(context.getString(result.messageResId))
            }

            AddReadingSubmitResult.ShowAnomalyDialog -> Unit
            is AddReadingSubmitResult.ValidationError -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete_meter_title)) },
            text = { Text(stringResource(R.string.delete_meter_message, meterConfig.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            repository.deleteMeterConfigById(meterConfig.id)
                            onDeleted(context.getString(R.string.meter_deleted))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    state.anomalyDialogState?.let { anomalyDialog ->
        AlertDialog(
            onDismissRequest = viewModel::dismissAnomalyDialog,
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800)) },
            title = { Text(anomalyDialog.title) },
            text = { Text(anomalyDialog.joke) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            handleSubmitResult(viewModel.confirmAnomalousSave())
                        }
                    },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(stringResource(R.string.anomaly_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAnomalyDialog) {
                    Text(stringResource(R.string.anomaly_fix_action))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AddReadingHeader(
            context = context,
            meterConfig = meterConfig,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        AddReadingSummaryCard(
            context = context,
            meterConfig = meterConfig,
            meterData = state.meterData,
            hasCurrentMonthReading = state.hasCurrentMonthReading,
            onEnterEditMode = {
                viewModel.enterEditMode()
                focusRequester.requestFocus()
            }
        )

        if (meterConfig.verificationDate > 0 && verificationInfo != null) {
            Spacer(modifier = Modifier.height(16.dp))
            MeterVerificationStatusCard(
                verificationDate = meterConfig.verificationDate,
                validityYears = meterConfig.validityYears,
                verificationInfo = verificationInfo
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (verificationInfo?.status is VerificationStatus.EXPIRED) {
            MeterVerificationInputLockedCard()
        } else {
            AddReadingInputSection(
                meterConfig = meterConfig,
                isEditMode = state.isEditMode,
                inputText = state.inputText,
                nightInputText = state.nightInputText,
                focusRequester = focusRequester,
                enabled = !state.hasCurrentMonthReading || state.isEditMode,
                onInputTextChange = viewModel::updateInputText,
                onNightInputTextChange = viewModel::updateNightInputText,
                onClearInput = viewModel::clearInput,
                onClearNightInput = viewModel::clearNightInput,
                scannerEnabled = scannerEnabled,
                onScanClick = onScanClick
            )
        }

        if (verificationInfo?.status !is VerificationStatus.EXPIRED) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        handleSubmitResult(viewModel.submit())
                    }
                },
                enabled = !state.isSaving && (!state.hasCurrentMonthReading || state.isEditMode),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusProperties { canFocus = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isEditMode) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (state.isEditMode) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (state.isEditMode) R.string.edit_reading_action else R.string.save_reading_action
                    )
                )
            }
        }

        state.messageResId?.let { messageResId ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodySmall,
                color = if (state.isMessageError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun vibrate(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    } catch (_: Exception) {
    }
}
