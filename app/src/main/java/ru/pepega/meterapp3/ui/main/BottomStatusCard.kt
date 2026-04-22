package ru.pepega.meterapp3.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import java.util.Calendar

private enum class ReminderBannerState {
    ACTIVE,
    OUT_OF_PERIOD,
    SUBMITTED,
    INCOMPLETE
}

data class BottomStatusCardUiState(
    val monthTitle: String,
    val compactReminderToggleText: String,
    val expandedReminderToggleText: String,
    val reminderTitle: String,
    val reminderActivityLabel: String,
    val reminderDetailText: String,
    val compactProgressText: String,
    val progressText: String,
    val remainingText: String,
    val reminderEnabled: Boolean,
    val reminderStatusColor: Color,
    val containerColor: Color
)

@Composable
fun rememberBottomStatusCardUiState(
    currentMonthLabel: String,
    updatedThisMonthCount: Int,
    totalMetersCount: Int,
    remainingThisMonthCount: Int,
    reminderEnabled: Boolean,
    reminderDayFrom: Int,
    reminderDayTo: Int,
    reminderTime1: String,
    reminderTime2: String,
    isCurrentMonthSubmitted: Boolean
): BottomStatusCardUiState {
    val reminderBannerState = if (!reminderEnabled) {
        null
    } else {
        val hasReminderTime = listOf(reminderTime1, reminderTime2).any { value ->
            val parts = value.split(":")
            if (parts.size != 2) {
                false
            } else {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                hour in 0..23 && minute in 0..59
            }
        }
        when {
            reminderDayFrom !in 1..31 || reminderDayTo !in reminderDayFrom..31 || !hasReminderTime -> ReminderBannerState.INCOMPLETE
            isCurrentMonthSubmitted -> ReminderBannerState.SUBMITTED
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH) !in reminderDayFrom..reminderDayTo -> ReminderBannerState.OUT_OF_PERIOD
            else -> ReminderBannerState.ACTIVE
        }
    }

    val reminderStatusColor = when (reminderBannerState) {
        ReminderBannerState.ACTIVE -> MaterialTheme.colorScheme.primary
        ReminderBannerState.OUT_OF_PERIOD,
        ReminderBannerState.SUBMITTED,
        ReminderBannerState.INCOMPLETE -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    return BottomStatusCardUiState(
        monthTitle = stringResource(R.string.month_status_title, currentMonthLabel),
        compactReminderToggleText = if (reminderEnabled) {
            stringResource(R.string.reminder_status_compact_active)
        } else {
            stringResource(R.string.reminder_status_compact_inactive)
        },
        expandedReminderToggleText = if (reminderEnabled) {
            stringResource(R.string.reminder_enabled_full)
        } else {
            stringResource(R.string.reminder_status_compact_inactive)
        },
        reminderTitle = stringResource(R.string.reminders_title).removePrefix("\uD83D\uDD14 "),
        reminderActivityLabel = if (reminderBannerState == ReminderBannerState.ACTIVE) {
            stringResource(R.string.reminder_status_label_active)
        } else {
            stringResource(R.string.reminder_status_label_inactive)
        },
        reminderDetailText = when (reminderBannerState) {
            ReminderBannerState.ACTIVE -> stringResource(R.string.reminder_status_active)
            ReminderBannerState.OUT_OF_PERIOD -> stringResource(R.string.reminder_status_out_of_period)
            ReminderBannerState.SUBMITTED -> stringResource(R.string.reminder_status_submitted)
            ReminderBannerState.INCOMPLETE -> stringResource(R.string.reminder_status_incomplete)
            null -> stringResource(R.string.reminders_disabled)
        },
        compactProgressText = if (remainingThisMonthCount > 0) {
            stringResource(
                R.string.month_status_compact_progress,
                updatedThisMonthCount,
                totalMetersCount,
                remainingThisMonthCount
            )
        } else {
            stringResource(
                R.string.month_status_compact_complete,
                updatedThisMonthCount,
                totalMetersCount
            )
        },
        progressText = stringResource(
            R.string.month_status_progress,
            updatedThisMonthCount,
            totalMetersCount
        ),
        remainingText = if (remainingThisMonthCount > 0) {
            stringResource(R.string.month_status_remaining, remainingThisMonthCount)
        } else {
            stringResource(R.string.month_status_complete)
        },
        reminderEnabled = reminderEnabled,
        reminderStatusColor = reminderStatusColor,
        containerColor = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.tertiaryContainer,
            0.2f
        )
    )
}

@Composable
fun BottomStatusCard(
    modifier: Modifier = Modifier,
    state: BottomStatusCardUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) 16.dp else 14.dp,
        label = "statusCardHorizontalPadding"
    )
    val verticalPadding by animateDpAsState(
        targetValue = if (expanded) 14.dp else 9.dp,
        label = "statusCardVerticalPadding"
    )
    val cardShape = RoundedCornerShape(22.dp)
    val borderColor = lerp(state.containerColor, MaterialTheme.colorScheme.onSurface, 0.18f).copy(alpha = 0.12f)
    val glowBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            state.containerColor.copy(alpha = 0.98f)
        )
    )

    Surface(
        modifier = modifier,
        shape = cardShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = glowBrush, shape = cardShape)
                .border(width = 1.dp, color = borderColor, shape = cardShape)
                .clickable(enabled = state.reminderEnabled) {
                    onExpandedChange(!expanded)
                }
        ) {
            if (!expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "\uD83D\uDD14", fontSize = 14.sp)
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(
                                    color = state.reminderStatusColor,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = state.compactReminderToggleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    if (state.reminderEnabled) {
                        ExpandCollapseIndicator(
                            expanded = false,
                            active = true,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Text(
                        text = state.compactProgressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                        .padding(bottom = if (state.reminderEnabled) 22.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "\uD83D\uDD14", fontSize = 16.sp)
                            Text(
                                text = state.reminderTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.expandedReminderToggleText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.reminderEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = state.reminderActivityLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = state.reminderStatusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.reminderDetailText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.progressText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.remainingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    )
                }

                if (state.reminderEnabled) {
                    ExpandCollapseIndicator(
                        expanded = true,
                        active = true,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandCollapseIndicator(
    expanded: Boolean,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(
                color = if (active) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (expanded) "▼" else "▲",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.95f)
            }
        )
    }
}
