package ru.pepega.meterapp3.ui.main

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterStatistics
import ru.pepega.meterapp3.MeterThemeIcon
import ru.pepega.meterapp3.SadMeterSummaryIconSize
import ru.pepega.meterapp3.TotalSummary
import ru.pepega.meterapp3.rememberAppPreferences
import ru.pepega.meterapp3.theme.ThemePreset
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun MainDashboardHeader(
    apartments: List<Apartment>,
    activeApartment: Apartment,
    configs: List<MeterConfig>,
    showMainGuideButton: Boolean,
    showApartmentMenu: Boolean,
    onShowApartmentMenuChange: (Boolean) -> Unit,
    onAddMeterClick: () -> Unit,
    onApartmentSelect: (String) -> Unit,
    onShowGuide: () -> Unit,
    onShowHistory: () -> Unit,
    onShowSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 2.dp
    ) {
        val headerActionContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        val headerActionContentColor = MaterialTheme.colorScheme.onSurface

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    Row(
                        modifier = Modifier.clickable(enabled = apartments.size > 1) {
                            onShowApartmentMenuChange(true)
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (apartments.size > 1) activeApartment.name else "Счётчики",
                            style = if (apartments.size > 1) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .then(
                                    if (apartments.size > 1) {
                                        Modifier.widthIn(max = 112.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (apartments.size > 1) {
                                        Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                        if (apartments.size > 1) {
                            Text("▼", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    DropdownMenu(
                        expanded = showApartmentMenu,
                        onDismissRequest = { onShowApartmentMenuChange(false) },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        tonalElevation = 4.dp,
                        shadowElevation = 2.dp
                    ) {
                        apartments.forEach { apartment ->
                            ApartmentMenuItem(
                                title = apartment.name,
                                selected = apartment.id == activeApartment.id,
                                onClick = {
                                    onShowApartmentMenuChange(false)
                                    onApartmentSelect(apartment.id)
                                }
                            )
                        }
                    }
                }

                val canAddMeter = configs.size < 10
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clipToBounds()
                        .clickable(enabled = canAddMeter) { onAddMeterClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = if (canAddMeter) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        }
                    ) {}
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (canAddMeter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showMainGuideButton) {
                    HeaderActionButton(
                        onClick = onShowGuide,
                        buttonDescription = "Открыть руководство",
                        containerColor = headerActionContainerColor,
                        contentColor = headerActionContentColor
                    ) {
                        Text("?", fontSize = 18.sp)
                    }
                }

                HeaderActionButton(
                    onClick = onShowHistory,
                    buttonDescription = "История",
                    containerColor = headerActionContainerColor,
                    contentColor = headerActionContentColor
                ) {
                    Text("📜", fontSize = 16.sp)
                }

                HeaderActionButton(
                    onClick = onShowSettings,
                    buttonDescription = "Настройки",
                    containerColor = headerActionContainerColor,
                    contentColor = headerActionContentColor
                ) {
                    Text("⚙️", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun MainDashboardSummaryCard(
    summary: TotalSummary,
    currency: String,
    tariffsEnabled: Boolean,
    currentMonthPrepositional: String,
    hasSummaryCalculation: Boolean,
    configs: List<MeterConfig>,
    meterDataById: Map<String, MeterData>,
    onShowSummary: () -> Unit
) {
    val isSadMeterTheme = rememberAppPreferences().themePreset() == ThemePreset.FOREST
    val summaryScrollState = rememberScrollState()
    val showHiddenMetersBadge by remember(summary.meters.size) {
        derivedStateOf { summary.meters.size > 4 && summaryScrollState.value == 0 }
    }
    val showSummaryScrollHint by remember(summary.meters.size) {
        derivedStateOf {
            summary.meters.size > 4 &&
                summaryScrollState.maxValue > 0 &&
                summaryScrollState.value < summaryScrollState.maxValue
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        val summarySectionSpacing = if (tariffsEnabled) 8.dp else 6.dp
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(summarySectionSpacing)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 84.dp)
                ) {
                    Text(
                        text = if (tariffsEnabled) "Общий платёж" else "Общий расход",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "В $currentMonthPrepositional",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.62f)
                    )
                }

                if (hasSummaryCalculation && tariffsEnabled) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .widthIn(max = 116.dp)
                            .clickable { onShowSummary() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = "${String.format("%.0f", summary.totalCost)} $currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                } else if (!hasSummaryCalculation && tariffsEnabled) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = "Нет данных",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val shouldCenterSummaryRow = summary.meters.size <= 4
                val summaryRowModifier = if (shouldCenterSummaryRow) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.horizontalScroll(summaryScrollState)
                }
                val summaryRowArrangement = if (shouldCenterSummaryRow) {
                    Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                } else {
                    Arrangement.spacedBy(8.dp)
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = summaryRowModifier,
                        horizontalArrangement = summaryRowArrangement,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sortedSummaryMeters = remember(summary.meters) {
                            summary.meters.sortedByDescending { it.consumption }
                        }
                        sortedSummaryMeters.forEachIndexed { index, meter ->
                            val trendData = meterDataById[meter.id] ?: MeterData()
                            val meterConfig = configs.firstOrNull { it.id == meter.id }
                            val trendDirection = meterConfig?.let {
                                MeterStatistics.getConsumptionTrend(
                                    config = it,
                                    data = trendData
                                )
                            } ?: 0
                            val trendArrow = when (trendDirection) {
                                1 -> "↑"
                                -1 -> "↓"
                                else -> ""
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
                            ) {
                            Column(
                                    modifier = Modifier
                                        .width(66.dp)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        MeterThemeIcon(
                                            icon = meter.icon,
                                            meterConfig = meterConfig,
                                            fallbackTitle = meter.name,
                                            fallbackUnit = meter.unit,
                                            modifier = Modifier.size(if (isSadMeterTheme) 28.dp else 22.dp),
                                            fontSize = 18.sp
                                        )
                                        if (trendArrow.isNotEmpty()) {
                                            Text(
                                                text = trendArrow,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (trendDirection > 0) {
                                                    MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                                } else {
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatSummaryConsumption(meter.consumption),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                            }
                            }

                            if (index == 3 && showHiddenMetersBadge) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.36f)
                                ) {
                                    Text(
                                        text = "+${summary.meters.size - 4}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (showSummaryScrollHint) {
                    Box(
                        modifier = Modifier.width(22.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatSummaryConsumption(value: Float): String {
    val symbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
    val format = DecimalFormat("0.#", symbols).apply {
        isGroupingUsed = false
    }
    return format.format(value.toDouble())
}

@Composable
private fun ApartmentMenuItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            null
        },
        onClick = onClick
    )
}

@Composable
private fun HeaderActionButton(
    onClick: () -> Unit,
    buttonDescription: String,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .semantics { this.contentDescription = buttonDescription },
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}
