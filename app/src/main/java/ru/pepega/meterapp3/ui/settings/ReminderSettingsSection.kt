package ru.pepega.meterapp3.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import ru.pepega.meterapp3.ui.settings.components.ReminderSelectorCard

@Composable
fun ReminderSettingsSection(
    modifier: Modifier = Modifier,
    sectionContainerColor: Color,
    sectionContentColor: Color,
    reminderEnabled: Boolean,
    reminderDayFrom: String,
    reminderDayTo: String,
    reminderTime1: String,
    reminderTime2: String,
    reminderPeriodSummary: String,
    reminderTimesSummary: String,
    hasPermissionIssues: Boolean,
    isCurrentMonthSubmitted: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onShowIssuesClick: () -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onSelectDayFrom: () -> Unit,
    onSelectDayTo: () -> Unit,
    onSelectTime1: () -> Unit,
    onSelectTime2: () -> Unit,
    onClearTime2: (() -> Unit)?,
    onResetSubmission: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.reminders_title), fontWeight = FontWeight.Bold)
                            if (hasPermissionIssues) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(24.dp).clickable { onShowIssuesClick() }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.permission_settings),
                                        tint = sectionContentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (reminderEnabled) stringResource(R.string.reminder_period_time) else stringResource(R.string.reminders_disabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = sectionContentColor.copy(alpha = 0.75f),
                            modifier = Modifier.padding(start = 26.dp)
                        )
                    }
                }
                Text(
                    text = if (expanded) stringResource(R.string.expanded_indicator) else stringResource(R.string.collapsed_indicator),
                    fontSize = 18.sp,
                    color = sectionContentColor
                )
            }

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
                        text = stringResource(R.string.reminder_enable_label),
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = onReminderEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (reminderEnabled) stringResource(R.string.reminder_period_time) else stringResource(R.string.reminders_disabled),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (reminderEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = sectionContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reminder_period_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = sectionContentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.reminder_period_from_inline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = sectionContentColor.copy(alpha = 0.82f)
                        )
                        ReminderSelectorCard(
                            modifier = Modifier.weight(1f),
                            label = "",
                            value = reminderDayFrom.ifBlank { stringResource(R.string.not_selected) },
                            onClick = onSelectDayFrom
                        )
                        Text(
                            text = stringResource(R.string.reminder_period_to_inline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = sectionContentColor.copy(alpha = 0.82f)
                        )
                        ReminderSelectorCard(
                            modifier = Modifier.weight(1f),
                            label = "",
                            value = reminderDayTo.ifBlank { stringResource(R.string.not_selected) },
                            onClick = onSelectDayTo
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reminderPeriodSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = sectionContentColor.copy(alpha = 0.72f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = sectionContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reminder_time_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = sectionContentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.first_time_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = sectionContentColor.copy(alpha = 0.82f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ReminderSelectorCard(
                                label = "",
                                value = reminderTime1.ifBlank { stringResource(R.string.not_selected) },
                                onClick = onSelectTime1
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.second_time_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = sectionContentColor.copy(alpha = 0.82f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ReminderSelectorCard(
                                label = "",
                                value = reminderTime2.ifBlank { stringResource(R.string.not_selected) },
                                onClick = onSelectTime2,
                                onClear = onClearTime2
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reminderTimesSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = sectionContentColor.copy(alpha = 0.7f)
                    )

                    if (isCurrentMonthSubmitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.reminder_submitted_state),
                            style = MaterialTheme.typography.bodySmall,
                            color = sectionContentColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onResetSubmission,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(stringResource(R.string.reminder_submission_reset_action))
                        }
                    }
                }
            }
        }
    }
}
