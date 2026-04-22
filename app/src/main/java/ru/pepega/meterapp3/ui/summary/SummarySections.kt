package ru.pepega.meterapp3.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.TotalSummary

@Composable
fun SummaryHeader(
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Text("←", fontSize = 20.sp)
            }
            Text(
                text = stringResource(R.string.summary_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(34.dp))
        }
    }
}

@Composable
fun SummaryTotalCard(
    summary: TotalSummary,
    currency: String,
    tariffsEnabled: Boolean,
    hasSummaryCalculation: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (tariffsEnabled) {
                    stringResource(R.string.summary_total_payment)
                } else {
                    stringResource(R.string.summary_total_consumption)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (tariffsEnabled) {
                    if (hasSummaryCalculation) {
                        "${String.format("%.0f", summary.totalCost)} $currency"
                    } else {
                        stringResource(R.string.summary_no_calculation_short)
                    }
                } else {
                    String.format("%.2f", summary.totalConsumption)
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (tariffsEnabled && !hasSummaryCalculation) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = if (tariffsEnabled && !hasSummaryCalculation) {
                    stringResource(R.string.summary_no_calculation)
                } else {
                    stringResource(
                        R.string.month_status_progress,
                        summary.meters.count { it.lastUpdate > 0 },
                        summary.meters.size
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SummaryMetersTableCard(
    summary: TotalSummary,
    tariffsEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.meter_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.date_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.consumption_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(76.dp),
                    textAlign = TextAlign.End
                )
                if (tariffsEnabled) {
                    Text(
                        text = stringResource(R.string.amount_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(68.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.size(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.size(6.dp))

            summary.meters.forEach { meter ->
                val dateText = if (meter.lastUpdate > 0) {
                    java.text.SimpleDateFormat(
                        "dd.MM.yy",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(meter.lastUpdate))
                } else {
                    stringResource(R.string.dash)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meter.icon} ${meter.name}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(68.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f", meter.consumption),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(76.dp),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Medium
                    )
                    if (tariffsEnabled) {
                        Text(
                            text = String.format("%.0f", meter.cost),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(68.dp),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}
