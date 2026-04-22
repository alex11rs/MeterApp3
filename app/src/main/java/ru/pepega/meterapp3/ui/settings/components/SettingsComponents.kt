package ru.pepega.meterapp3.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R

@Composable
fun SettingsSectionHeader(
    title: String,
    description: String,
    expanded: Boolean,
    sectionContentColor: Color,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = sectionContentColor.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 26.dp)
            )
        }
        Text(
            text = if (expanded) stringResource(R.string.expanded_indicator) else stringResource(R.string.collapsed_indicator),
            fontSize = 18.sp,
            color = sectionContentColor
        )
    }
}

@Composable
fun ReminderSelectorCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onClear != null) {
                        Text(
                            text = stringResource(R.string.clear_short),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onClear)
                                .padding(end = 8.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DayNumberPickerDialog(
    title: String,
    selectedDay: Int?,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..31).forEach { day ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(day) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedDay == day) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ) {
                        Text(
                            text = day.toString(),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.clear))
            }
        }
    )
}
