package ru.pepega.meterapp3.ui.meter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.TariffTypeSelector
import ru.pepega.meterapp3.availableColors
import ru.pepega.meterapp3.availableIcons
import ru.pepega.meterapp3.availableUnits
import java.util.Calendar

@Composable
fun MeterTariffSection(
    tariffType: MeterTariffType,
    showTariffBlock: Boolean,
    onToggleExpanded: () -> Unit,
    onTariffTypeChange: (MeterTariffType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.tariff_type_label), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (tariffType == MeterTariffType.SINGLE) {
                            stringResource(R.string.tariff_type_single)
                        } else {
                            stringResource(R.string.tariff_type_dual)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = if (showTariffBlock) {
                        stringResource(R.string.expanded_indicator)
                    } else {
                        stringResource(R.string.collapsed_indicator)
                    },
                    fontSize = 20.sp
                )
            }

            if (showTariffBlock) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                TariffTypeSelector(
                    tariffType = tariffType,
                    onTariffTypeChange = onTariffTypeChange
                )
            }
        }
    }
}

@Composable
fun MeterVerificationSection(
    showVerificationBlock: Boolean,
    titleResId: Int,
    day: String,
    month: String,
    year: String,
    validityYears: String,
    onToggleExpanded: () -> Unit,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onValidityYearsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(stringResource(titleResId), fontWeight = FontWeight.Bold)
                }
                Text(
                    text = if (showVerificationBlock) {
                        stringResource(R.string.expanded_indicator)
                    } else {
                        stringResource(R.string.collapsed_indicator)
                    },
                    fontSize = 20.sp,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
                )
            }

            if (showVerificationBlock) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        DigitTextField(
                            value = day,
                            onValueChange = onDayChange,
                            maxLength = 2,
                            labelResId = R.string.day_short,
                            placeholder = "15"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DigitTextField(
                            value = month,
                            onValueChange = onMonthChange,
                            maxLength = 2,
                            labelResId = R.string.month_short,
                            placeholder = "03"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DigitTextField(
                            value = year,
                            onValueChange = onYearChange,
                            maxLength = 4,
                            labelResId = R.string.year_short,
                            placeholder = "2024"
                        )
                    }
                }

                DigitTextField(
                    value = validityYears,
                    onValueChange = onValidityYearsChange,
                    maxLength = 2,
                    labelResId = R.string.validity_years_label,
                    placeholder = "4",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MeterSelectionSection(
    icon: String,
    unit: String,
    color: String,
    onIconSelected: (String) -> Unit,
    onUnitSelected: (String) -> Unit,
    onColorSelected: (String) -> Unit
) {
    Text(stringResource(R.string.icon_label), fontWeight = FontWeight.Medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableIcons.forEach { value ->
            FilterChip(
                selected = icon == value,
                onClick = { onIconSelected(value) },
                label = { Text(value, fontSize = 20.sp) }
            )
        }
    }

    Text(stringResource(R.string.unit_label), fontWeight = FontWeight.Medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableUnits.forEach { value ->
            FilterChip(
                selected = unit == value,
                onClick = { onUnitSelected(value) },
                label = { Text(value) }
            )
        }
    }

    Text(stringResource(R.string.color_label), fontWeight = FontWeight.Medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableColors.forEach { (colorName, colorValue) ->
            FilterChip(
                selected = color == colorName,
                onClick = { onColorSelected(colorName) },
                label = { Box(Modifier.size(20.dp).background(colorValue, CircleShape)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorValue.copy(alpha = 0.3f)
                )
            )
        }
    }
}

fun parseVerificationDate(
    day: String,
    month: String,
    year: String
): Long {
    if (day.isEmpty() || month.isEmpty() || year.isEmpty()) return 0L

    return try {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year.toInt())
            set(Calendar.MONTH, month.toInt() - 1)
            set(Calendar.DAY_OF_MONTH, day.toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (_: Exception) {
        0L
    }
}

@Composable
private fun DigitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    labelResId: Int,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxLength && it.all(Char::isDigit)) {
                onValueChange(it)
            }
        },
        label = { Text(stringResource(labelResId)) },
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
