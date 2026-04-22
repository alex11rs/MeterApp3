package ru.pepega.meterapp3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Calendar
import ru.pepega.meterapp3.ui.common.AppDialogScaffold
import ru.pepega.meterapp3.ui.meter.MeterSelectionSection
import ru.pepega.meterapp3.ui.meter.MeterTariffSection
import ru.pepega.meterapp3.ui.meter.MeterVerificationSection
import ru.pepega.meterapp3.ui.meter.parseVerificationDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterEditDialog(
    config: MeterConfig?,
    existingConfigs: List<MeterConfig>,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit,
    onSave: (MeterConfig) -> Unit,
    onResetData: (() -> Unit)?
) {
    val context = LocalContext.current
    val repository = rememberMeterRepository()
    val tariffsEnabled = rememberAppPreferences().tariffsEnabled()
    val scope = rememberCoroutineScope()
    val contentScrollState = rememberScrollState()
    val showScrollHint by remember {
        derivedStateOf {
            contentScrollState.maxValue > 0 && contentScrollState.value < contentScrollState.maxValue
        }
    }

    var name by remember { mutableStateOf(config?.name ?: "") }
    var model by remember { mutableStateOf(config?.model ?: "") }
    var price by remember { mutableStateOf("5.0") }
    var nightPrice by remember { mutableStateOf("0.0") }
    var icon by remember { mutableStateOf(config?.icon ?: "💧") }
    var unit by remember { mutableStateOf(config?.unit ?: "м³") }
    var color by remember { mutableStateOf(config?.color ?: "blue") }
    var decimalDigits by remember { mutableStateOf((config?.decimalDigits ?: 0).toString()) }
    var tariffType by remember { mutableStateOf(config?.tariffType ?: MeterTariffType.SINGLE) }
    var showTariffBlock by remember { mutableStateOf(false) }
    var validityYears by remember { mutableStateOf(config?.validityYears?.toString() ?: "4") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var showVerificationBlock by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        if (config?.verificationDate != null && config.verificationDate > 0) {
            val calendar = Calendar.getInstance().apply { timeInMillis = config.verificationDate }
            day = calendar.get(Calendar.DAY_OF_MONTH).toString()
            month = (calendar.get(Calendar.MONTH) + 1).toString()
            year = calendar.get(Calendar.YEAR).toString()
        }
        if (config != null) {
            val data = repository.getMeterData(config.id)
            price = data.tariff.toString()
            nightPrice = data.nightTariff.toString()
        }
    }

    AppDialogScaffold(
        title = stringResource(if (config == null) R.string.add_meter_title else R.string.edit_meter_title),
        onDismissRequest = onDismiss,
        showScrollHint = showScrollHint,
        contentScrollState = contentScrollState,
        actions = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (name.isEmpty()) return@Button
                    if (config == null && existingConfigs.size >= 10) {
                        onShowMessage(context.getString(R.string.max_meters))
                        return@Button
                    }

                    val newVerificationDate = parseVerificationDate(day, month, year)

                    if (newVerificationDate > System.currentTimeMillis()) {
                        onShowMessage(context.getString(R.string.verification_future_error))
                        return@Button
                    }

                    scope.launch {
                    val activeApartmentId = config?.apartmentId ?: repository.getActiveApartmentId()
                    val newConfig = MeterConfig(
                        id = config?.id ?: "meter_${System.currentTimeMillis()}",
                        name = name,
                        model = model,
                        icon = icon,
                        unit = unit,
                        decimalDigits = decimalDigits.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                        color = color,
                        enabled = config?.enabled ?: true,
                        apartmentId = activeApartmentId,
                        tariffType = tariffType,
                        verificationDate = newVerificationDate,
                        validityYears = validityYears.toIntOrNull() ?: 4
                    )
                    onSave(newConfig)

                    price.toFloatOrNull()?.let { newPrice ->
                        val data = repository.getMeterData(newConfig.id)
                        repository.saveMeterData(
                            newConfig.id,
                            data.copy(
                                tariff = newPrice,
                                nightTariff = if (tariffType == MeterTariffType.DUAL) {
                                    nightPrice.toFloatOrNull() ?: 0f
                                } else 0f
                            )
                        )
                    }
                    }
                },
                enabled = name.isNotEmpty() && (config != null || existingConfigs.size < 10)
            ) {
                Text(stringResource(R.string.save_action))
            }
        }
    ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), singleLine = true, placeholder = { Text(stringResource(R.string.cold_water_placeholder)) })
                OutlinedTextField(value = model, onValueChange = { if (it.length <= 30) model = it }, label = { Text(stringResource(R.string.model_label_optional)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), singleLine = true, placeholder = { Text(stringResource(R.string.model_placeholder)) })
                MeterTariffSection(
                    tariffType = tariffType,
                    showTariffBlock = showTariffBlock,
                    onToggleExpanded = { showTariffBlock = !showTariffBlock },
                    onTariffTypeChange = { tariffType = it }
                )

                if (tariffsEnabled) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() || ch == '.' }) price = it },
                        label = { Text(stringResource(R.string.price_per_unit_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        placeholder = { Text("45.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (tariffType == MeterTariffType.DUAL) {
                        OutlinedTextField(
                            value = nightPrice,
                            onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() || ch == '.' }) nightPrice = it },
                            label = { Text(stringResource(R.string.night_price_per_unit_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            placeholder = { Text("2.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                MeterVerificationSection(
                    showVerificationBlock = showVerificationBlock,
                    titleResId = R.string.verification_date_label,
                    day = day,
                    month = month,
                    year = year,
                    validityYears = validityYears,
                    onToggleExpanded = { showVerificationBlock = !showVerificationBlock },
                    onDayChange = { day = it },
                    onMonthChange = { month = it },
                    onYearChange = { year = it },
                    onValidityYearsChange = { validityYears = it }
                )

                MeterSelectionSection(
                    icon = icon,
                    unit = unit,
                    color = color,
                    onIconSelected = { icon = it },
                    onUnitSelected = { unit = it },
                    onColorSelected = { color = it }
                )

                OutlinedTextField(
                    value = decimalDigits,
                    onValueChange = {
                        if (it.length <= 1 && (it.isEmpty() || it.toIntOrNull()?.let { value -> value in 0..3 } == true)) {
                            decimalDigits = it
                        }
                    },
                    label = { Text(stringResource(R.string.meter_decimal_digits_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    supportingText = { Text(stringResource(R.string.meter_decimal_digits_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (config != null && onResetData != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                        Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.reset_meter_data))
                    }
                }

    }

    if (showResetConfirm && config != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.reset_data_title)) },
            text = { Text(stringResource(R.string.reset_data_message, config.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData?.invoke()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(stringResource(R.string.reset_meter_data))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
