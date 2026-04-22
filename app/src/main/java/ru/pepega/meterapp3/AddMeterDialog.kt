package ru.pepega.meterapp3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.ui.common.AppDialogScaffold
import ru.pepega.meterapp3.ui.meter.MeterSelectionSection
import ru.pepega.meterapp3.ui.meter.MeterTariffSection
import ru.pepega.meterapp3.ui.meter.MeterVerificationSection
import ru.pepega.meterapp3.ui.meter.parseVerificationDate
import ru.pepega.meterapp3.suggestedDecimalDigitsForUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeterDialog(
    existingConfigs: List<MeterConfig>,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit,
    onMeterAdded: () -> Unit
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

    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("5.0") }
    var nightPrice by remember { mutableStateOf("2.5") }
    var icon by remember { mutableStateOf("💧") }
    var unit by remember { mutableStateOf("м³") }
    var color by remember { mutableStateOf("blue") }
    var decimalDigits by remember { mutableStateOf(suggestedDecimalDigitsForUnit(unit).toString()) }
    var tariffType by remember { mutableStateOf(MeterTariffType.SINGLE) }
    var showTariffBlock by remember { mutableStateOf(false) }
    var validityYears by remember { mutableStateOf("4") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var showVerificationBlock by remember { mutableStateOf(false) }

    AppDialogScaffold(
        title = stringResource(R.string.add_meter_title),
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
                    if (name.isEmpty()) {
                        onShowMessage(context.getString(R.string.enter_name))
                        return@Button
                    }
                    if (existingConfigs.size >= 10) {
                        onShowMessage(context.getString(R.string.max_meters))
                        return@Button
                    }
                    if (existingConfigs.any { it.icon == icon && it.color == color }) {
                        onShowMessage(context.getString(R.string.duplicate_meter_icon_color))
                        return@Button
                    }

                    val verificationDate = parseVerificationDate(day, month, year)

                    scope.launch {
                    val activeApartmentId = repository.getActiveApartmentId()
                    val newConfig = MeterConfig(
                        id = "meter_${System.currentTimeMillis()}",
                        name = name,
                        model = model,
                        icon = icon,
                        unit = unit,
                        decimalDigits = decimalDigits.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                        color = color,
                        enabled = true,
                        order = existingConfigs.size,
                        apartmentId = activeApartmentId,
                        tariffType = tariffType,
                        verificationDate = verificationDate,
                        validityYears = validityYears.toIntOrNull() ?: 4
                    )

                    repository.addMeterConfig(newConfig)
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

                    onShowMessage(context.getString(R.string.meter_added))
                    onMeterAdded()
                    onDismiss()
                    }
                },
                enabled = name.isNotEmpty()
            ) {
                Text(stringResource(R.string.add))
            }
        }
    ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.cold_water_placeholder)) }
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { if (it.length <= 30) model = it },
                    label = { Text(stringResource(R.string.model_label_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.model_placeholder)) }
                )

                MeterTariffSection(
                    tariffType = tariffType,
                    showTariffBlock = showTariffBlock,
                    onToggleExpanded = { showTariffBlock = !showTariffBlock },
                    onTariffTypeChange = { tariffType = it }
                )

                if (tariffsEnabled) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() || char == '.' }) {
                                price = it
                            }
                        },
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
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() || char == '.' }) {
                                    nightPrice = it
                                }
                            },
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
                    titleResId = R.string.verification_date_optional,
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
                    onUnitSelected = {
                        unit = it
                        decimalDigits = suggestedDecimalDigitsForUnit(it).toString()
                    },
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

    }
}
