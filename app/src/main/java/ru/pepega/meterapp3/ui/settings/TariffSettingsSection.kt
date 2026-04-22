package ru.pepega.meterapp3.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.ui.settings.components.SettingsSectionHeader

@Composable
fun ApartmentsShortcutCard(
    modifier: Modifier = Modifier,
    sectionContainerColor: Color,
    sectionContentColor: Color,
    onOpenApartments: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenApartments() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.multi_apartments_title), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.multi_apartments_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = sectionContentColor.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = sectionContentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TariffSettingsSection(
    sectionContainerColor: Color,
    sectionContentColor: Color,
    currencies: List<String>,
    currency: String,
    tariffsEnabled: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onTariffsEnabledChange: (Boolean) -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SettingsSectionHeader(
                title = stringResource(R.string.cost_calculation),
                description = if (tariffsEnabled) stringResource(R.string.show_cost) else stringResource(R.string.consumption_only),
                expanded = expanded,
                sectionContentColor = sectionContentColor,
                onToggleExpanded = onToggleExpanded
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.cost_calculation),
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = tariffsEnabled,
                        onCheckedChange = onTariffsEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (tariffsEnabled) stringResource(R.string.show_cost) else stringResource(R.string.consumption_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = sectionContentColor.copy(alpha = 0.75f)
                )

                if (tariffsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.currency_label), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { curr ->
                            FilterChip(
                                selected = currency == curr,
                                onClick = { onCurrencySelected(curr) },
                                label = { Text(curr) }
                            )
                        }
                    }
                }
            }
        }
    }
}
