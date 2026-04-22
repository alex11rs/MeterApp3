package ru.pepega.meterapp3.ui.reading

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.MeterThemeIcon
import ru.pepega.meterapp3.ui.common.copyTextToClipboard
import ru.pepega.meterapp3.ui.common.formatPlainNumber
import ru.pepega.meterapp3.ui.common.showShortUserMessage
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.SadMeterCardIconContainerSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddReadingHeader(
    context: Context,
    meterConfig: MeterConfig,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.back),
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MeterThemeIcon(
                    icon = meterConfig.icon,
                    meterConfig = meterConfig,
                    fallbackTitle = meterConfig.name,
                    fallbackUnit = meterConfig.unit,
                    modifier = Modifier.size(SadMeterCardIconContainerSize),
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = meterConfig.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddReadingSummaryCard(
    context: Context,
    meterConfig: MeterConfig,
    meterData: MeterData,
    hasCurrentMonthReading: Boolean,
    onEnterEditMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (meterConfig.model.isNotEmpty()) {
                Text(
                    text = meterConfig.model,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (hasCurrentMonthReading) {
                CurrentMonthReadingSection(
                    context = context,
                    meterConfig = meterConfig,
                    meterData = meterData,
                    onEnterEditMode = onEnterEditMode
                )
            } else {
                PreviousReadingSection(
                    meterConfig = meterConfig,
                    meterData = meterData
                )
            }

            if (meterData.lastUpdate > 0) {
                if (meterConfig.tariffType == MeterTariffType.DUAL) {
                    SummaryRow(
                        label = stringResource(R.string.previous_day_label),
                        value = "${formatPlainNumber(meterData.previous)} ${meterConfig.unit}"
                    )
                    SummaryRow(
                        label = stringResource(R.string.previous_night_label),
                        value = "${formatPlainNumber(meterData.previousNight)} ${meterConfig.unit}"
                    )
                } else {
                    SummaryRow(
                        label = stringResource(R.string.previous_label),
                        value = "${formatPlainNumber(meterData.previous)} ${meterConfig.unit}"
                    )
                }

                val lastUpdateText = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(Date(meterData.lastUpdate))
                SummaryRow(
                    label = stringResource(R.string.updated_label),
                    value = lastUpdateText
                )
            }
        }
    }
}

@Composable
private fun CurrentMonthReadingSection(
    context: Context,
    meterConfig: MeterConfig,
    meterData: MeterData,
    onEnterEditMode: () -> Unit
) {
    val copiedText = if (meterConfig.tariffType == MeterTariffType.DUAL) {
        "${String.format("%.2f", meterData.current)} / ${String.format("%.2f", meterData.currentNight)}"
    } else {
        String.format("%.2f", meterData.current)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.current_month_reading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (meterConfig.tariffType == MeterTariffType.DUAL) {
                    Text(
                        text = stringResource(
                            R.string.day_night_compact_value,
                            String.format("%.2f", meterData.current),
                            String.format("%.2f", meterData.currentNight),
                            meterConfig.unit
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${formatPlainNumber(meterData.current)} ${meterConfig.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            label = context.getString(R.string.copy_value_label),
                            text = copiedText
                        )
                        showShortUserMessage(context, context.getString(R.string.copied_value, copiedText))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                        contentDescription = stringResource(R.string.copy_hint),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = onEnterEditMode,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_hint),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviousReadingSection(
    meterConfig: MeterConfig,
    meterData: MeterData
) {
    when {
        meterData.lastUpdate <= 0L -> {
            SummaryRow(
                label = stringResource(R.string.last_reading_label),
                value = stringResource(R.string.no_data),
                valueColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                valueWeight = FontWeight.Medium
            )
        }

        meterConfig.tariffType == MeterTariffType.DUAL -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SummaryRow(
                    label = stringResource(R.string.last_reading_day_label),
                    value = "${formatPlainNumber(meterData.current)} ${meterConfig.unit}",
                    valueWeight = FontWeight.Bold
                )
                SummaryRow(
                    label = stringResource(R.string.last_reading_night_label),
                    value = "${formatPlainNumber(meterData.currentNight)} ${meterConfig.unit}",
                    valueWeight = FontWeight.Bold
                )
            }
        }

        else -> {
            SummaryRow(
                label = stringResource(R.string.last_reading_label),
                value = "${formatPlainNumber(meterData.current)} ${meterConfig.unit}",
                valueWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = valueWeight,
            color = valueColor
        )
    }
}

@Composable
fun AddReadingInputSection(
    meterConfig: MeterConfig,
    isEditMode: Boolean,
    inputText: String,
    nightInputText: String,
    focusRequester: FocusRequester,
    enabled: Boolean,
    onInputTextChange: (String) -> Unit,
    onNightInputTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onClearNightInput: () -> Unit,
    scannerEnabled: Boolean,
    onScanClick: (ru.pepega.meterapp3.ReadingField) -> Unit
) {
    if (meterConfig.tariffType == MeterTariffType.DUAL) {
        DualTariffInputSection(
            meterConfig = meterConfig,
            inputText = inputText,
            nightInputText = nightInputText,
            focusRequester = focusRequester,
            enabled = enabled,
            onInputTextChange = onInputTextChange,
            onNightInputTextChange = onNightInputTextChange,
            onClearInput = onClearInput,
            onClearNightInput = onClearNightInput,
            scannerEnabled = scannerEnabled,
            onScanClick = onScanClick
        )
    } else {
        SingleTariffInputSection(
            meterConfig = meterConfig,
            isEditMode = isEditMode,
            inputText = inputText,
            focusRequester = focusRequester,
            enabled = enabled,
            onInputTextChange = onInputTextChange,
            onClearInput = onClearInput,
            scannerEnabled = scannerEnabled,
            onScanClick = onScanClick
        )
    }
}

@Composable
private fun DualTariffInputSection(
    meterConfig: MeterConfig,
    inputText: String,
    nightInputText: String,
    focusRequester: FocusRequester,
    enabled: Boolean,
    onInputTextChange: (String) -> Unit,
    onNightInputTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onClearNightInput: () -> Unit,
    scannerEnabled: Boolean,
    onScanClick: (ru.pepega.meterapp3.ReadingField) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ReadingInputRow(
            value = inputText,
            label = stringResource(R.string.day_reading_label),
            placeholder = stringResource(R.string.reading_placeholder, meterConfig.unit),
            focusRequester = focusRequester,
            enabled = enabled,
            onValueChange = onInputTextChange,
            onClear = onClearInput,
            scannerEnabled = scannerEnabled,
            onScanClick = { onScanClick(ru.pepega.meterapp3.ReadingField.DAY) },
            scanDescription = stringResource(R.string.scan_day_action)
        )

        ReadingInputRow(
            value = nightInputText,
            label = stringResource(R.string.night_reading_label),
            placeholder = stringResource(R.string.reading_placeholder, meterConfig.unit),
            enabled = enabled,
            onValueChange = onNightInputTextChange,
            onClear = onClearNightInput,
            scannerEnabled = scannerEnabled,
            onScanClick = { onScanClick(ru.pepega.meterapp3.ReadingField.NIGHT) },
            scanDescription = stringResource(R.string.scan_night_action)
        )
    }
}

@Composable
private fun SingleTariffInputSection(
    meterConfig: MeterConfig,
    isEditMode: Boolean,
    inputText: String,
    focusRequester: FocusRequester,
    enabled: Boolean,
    onInputTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    scannerEnabled: Boolean,
    onScanClick: (ru.pepega.meterapp3.ReadingField) -> Unit
) {
    ReadingInputRow(
        value = inputText,
        label = stringResource(
            when {
                isEditMode -> R.string.corrected_reading_label
                !enabled -> R.string.reading_saved_label
                else -> R.string.new_reading_label
            }
        ),
        placeholder = stringResource(R.string.reading_placeholder, meterConfig.unit),
        focusRequester = focusRequester,
        enabled = enabled,
        onValueChange = onInputTextChange,
        onClear = onClearInput,
        scannerEnabled = scannerEnabled,
        onScanClick = { onScanClick(ru.pepega.meterapp3.ReadingField.DAY) },
        scanDescription = stringResource(R.string.scan_action)
    )
}

@Composable
private fun ReadingInputRow(
    value: String,
    label: String,
    placeholder: String,
    focusRequester: FocusRequester? = null,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    scannerEnabled: Boolean,
    onScanClick: () -> Unit,
    scanDescription: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() || char == '.' }) {
                        onValueChange(it)
                    }
                },
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = stringResource(R.string.clear),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (scannerEnabled) {
            FilledTonalIconButton(
                onClick = onScanClick,
                enabled = enabled,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = scanDescription,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
