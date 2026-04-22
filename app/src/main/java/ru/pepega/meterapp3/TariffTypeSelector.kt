package ru.pepega.meterapp3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TariffTypeSelector(
    tariffType: MeterTariffType,
    onTariffTypeChange: (MeterTariffType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.tariff_type_label), fontWeight = FontWeight.Medium)

        TariffTypeOptionCard(
            title = stringResource(R.string.tariff_type_single),
            description = stringResource(R.string.tariff_type_single_description),
            selected = tariffType == MeterTariffType.SINGLE,
            onClick = { onTariffTypeChange(MeterTariffType.SINGLE) }
        )

        TariffTypeOptionCard(
            title = stringResource(R.string.tariff_type_dual),
            description = stringResource(R.string.tariff_type_dual_description),
            selected = tariffType == MeterTariffType.DUAL,
            onClick = { onTariffTypeChange(MeterTariffType.DUAL) }
        )
    }
}

@Composable
private fun TariffTypeOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
