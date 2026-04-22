package ru.pepega.meterapp3.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.MeterThemeIcon
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.SadMeterCardIconContainerSize
import ru.pepega.meterapp3.VerificationInfo
import ru.pepega.meterapp3.VerificationStatus
import ru.pepega.meterapp3.formatDate
import ru.pepega.meterapp3.formatYears
import ru.pepega.meterapp3.rememberAppPreferences
import ru.pepega.meterapp3.theme.ThemePreset

@Composable
fun MeterDetailsHeader(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.back),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.meter_details_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(34.dp))
        }
    }
}

@Composable
fun MeterMainInfoCard(
    configs: List<MeterConfig>,
    pagerState: PagerState,
    meterConfig: MeterConfig,
    meterData: MeterData,
    verificationInfo: VerificationInfo,
    totalCost: Float
) {
    val verificationStatus = verificationInfo.status
    val isPepeMeterTheme = rememberAppPreferences().themePreset() == ThemePreset.FOREST

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pageConfig = configs.getOrElse(page) { meterConfig }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MeterThemeIcon(
                        icon = pageConfig.icon,
                        meterConfig = pageConfig,
                        fallbackTitle = pageConfig.name,
                        fallbackUnit = pageConfig.unit,
                        modifier = Modifier
                            .size(if (isPepeMeterTheme) SadMeterCardIconContainerSize else 48.dp),
                        fontSize = 34.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = pageConfig.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (pageConfig.model.isNotEmpty()) {
                            Text(
                                text = pageConfig.model,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.model_not_specified),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            if (configs.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    configs.forEachIndexed { index, _ ->
                        val selected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (selected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (meterConfig.tariffType == MeterTariffType.DUAL) {
                DetailRow(
                    label = stringResource(R.string.current_day_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", meterData.current),
                        meterConfig.unit
                    )
                )
                DetailRow(
                    label = stringResource(R.string.current_night_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", meterData.currentNight),
                        meterConfig.unit
                    )
                )
                DetailRow(
                    label = stringResource(R.string.day_tariff_label),
                    value = String.format("%.2f", meterData.tariff)
                )
                DetailRow(
                    label = stringResource(R.string.night_tariff_label),
                    value = String.format("%.2f", meterData.nightTariff)
                )
                DetailRow(
                    label = stringResource(R.string.amount_label),
                    value = String.format("%.2f", totalCost),
                    valueColor = MaterialTheme.colorScheme.primary,
                    emphasized = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            if (meterConfig.verificationDate > 0) {
                DetailRow(
                    label = stringResource(R.string.date_label),
                    value = formatDate(meterConfig.verificationDate)
                )
                DetailRow(
                    label = stringResource(R.string.validity_label),
                    value = stringResource(R.string.years_value, formatYears(meterConfig.validityYears))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (verificationStatus is VerificationStatus.EXPIRED) {
                            stringResource(R.string.expired_label)
                        } else {
                            stringResource(R.string.expires_label)
                        }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (verificationStatus !is VerificationStatus.OK &&
                            verificationStatus !is VerificationStatus.NOT_SET
                        ) {
                            Text(
                                text = verificationStatus.getIcon(),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            formatDate(verificationInfo.expiryDate),
                            fontWeight = FontWeight.Bold,
                            color = verificationStatus.getColor()
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.verification_not_specified),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MeterStatisticsCard(
    meterConfig: MeterConfig,
    allReadings: List<Pair<Long, Float>>,
    totalConsumptionChartPoints: List<ConsumptionChartPoint>,
    dayConsumptionChartPoints: List<ConsumptionChartPoint>,
    nightConsumptionChartPoints: List<ConsumptionChartPoint>,
    totalReadings: Int,
    averageConsumption: Float,
    minConsumption: Float,
    maxConsumption: Float
) {
    var chartMode by remember(meterConfig.id) { mutableStateOf(ChartMode.TOTAL) }
    var expanded by remember(meterConfig.id) { mutableStateOf(true) }
    val activeChartPoints = when (chartMode) {
        ChartMode.TOTAL -> totalConsumptionChartPoints
        ChartMode.DAY -> dayConsumptionChartPoints
        ChartMode.NIGHT -> nightConsumptionChartPoints
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.statistics_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            if (totalConsumptionChartPoints.isNotEmpty()) {
                if (meterConfig.tariffType == MeterTariffType.DUAL) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        ChartMode.entries.forEach { mode ->
                            FilterChip(
                                selected = chartMode == mode,
                                onClick = { chartMode = mode },
                                label = {
                                    Text(
                                        text = stringResource(
                                            when (mode) {
                                                ChartMode.TOTAL -> R.string.chart_mode_total
                                                ChartMode.DAY -> R.string.chart_mode_day
                                                ChartMode.NIGHT -> R.string.chart_mode_night
                                            }
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                MeterConsumptionChart(
                    points = activeChartPoints,
                    unit = meterConfig.unit
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DetailRow(
                label = stringResource(R.string.total_readings_label),
                value = totalReadings.toString()
            )

            if (totalReadings > 0) {
                val totalConsumption = allReadings.first().second - (allReadings.lastOrNull()?.second ?: 0f)
                DetailRow(
                    label = stringResource(R.string.total_consumption_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", totalConsumption),
                        meterConfig.unit
                    ),
                    valueColor = MaterialTheme.colorScheme.primary,
                    emphasized = true
                )
            }

            if (allReadings.size >= 2) {
                val lastMonthConsumption = allReadings[0].second - allReadings[1].second
                DetailRow(
                    label = stringResource(R.string.last_month_consumption_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", lastMonthConsumption),
                        meterConfig.unit
                    ),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }

            if (totalReadings > 1) {
                DetailRow(
                    label = stringResource(R.string.average_consumption_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", averageConsumption),
                        meterConfig.unit
                    )
                )
                DetailRow(
                    label = stringResource(R.string.min_consumption_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", minConsumption),
                        meterConfig.unit
                    )
                )
                DetailRow(
                    label = stringResource(R.string.max_consumption_label),
                    value = stringResource(
                        R.string.reading_value,
                        String.format("%.2f", maxConsumption),
                        meterConfig.unit
                    )
                )
            }
            }
        }
    }
}

private enum class ChartMode {
    TOTAL,
    DAY,
    NIGHT
}

@Composable
private fun MeterConsumptionChart(
    points: List<ConsumptionChartPoint>,
    unit: String
) {
    val maxValue = points.maxOfOrNull(ConsumptionChartPoint::value)?.coerceAtLeast(1f) ?: 1f
    val minValue = points.minOfOrNull(ConsumptionChartPoint::value)?.coerceAtLeast(0f) ?: 0f
    val lineColor = MaterialTheme.colorScheme.primary
    val lineFillColor = lineColor.copy(alpha = 0.12f)
    val pointColor = MaterialTheme.colorScheme.surface
    val guideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.consumption_chart_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", minValue)} - ${String.format("%.1f", maxValue)} $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val chartTop = 10.dp.toPx()
            val chartBottom = size.height - 10.dp.toPx()
            val chartHeight = chartBottom - chartTop
            val count = points.size.coerceAtLeast(1)
            val stepX = if (count == 1) 0f else size.width / (count - 1)
            val pointRadius = 4.dp.toPx()
            val linePath = Path()
            val fillPath = Path()

            repeat(3) { index ->
                val y = chartTop + (chartHeight / 2f) * index
                drawLine(
                    color = guideColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val chartPoints = points.mapIndexed { index, point ->
                val progress = (point.value / maxValue).coerceIn(0f, 1f)
                Offset(
                    x = if (count == 1) size.width / 2f else index * stepX,
                    y = chartBottom - (progress * chartHeight)
                )
            }

            chartPoints.forEachIndexed { index, offset ->
                if (index == 0) {
                    linePath.moveTo(offset.x, offset.y)
                    fillPath.moveTo(offset.x, chartBottom)
                    fillPath.lineTo(offset.x, offset.y)
                } else {
                    linePath.lineTo(offset.x, offset.y)
                    fillPath.lineTo(offset.x, offset.y)
                }
            }
            if (chartPoints.isNotEmpty()) {
                val last = chartPoints.last()
                fillPath.lineTo(last.x, chartBottom)
                fillPath.close()
            }

            drawPath(
                path = fillPath,
                color = lineFillColor
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            chartPoints.forEach { offset ->
                drawCircle(
                    color = pointColor,
                    radius = pointRadius + 2.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = lineColor,
                    radius = pointRadius,
                    center = offset
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val lastIndex = points.lastIndex
            points.forEachIndexed { index, point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", point.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (points.size <= 4 || index == 0 || index == lastIndex || index % 2 == 0) {
                            point.label
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MeterReadingsHistoryCard(
    meterConfig: MeterConfig,
    allReadings: List<Pair<Long, Float>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.readings_history_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (allReadings.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_readings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                val yearFormat = remember { java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()) }
                val dateFormat = remember { java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()) }
                val availableYears by remember(allReadings) {
                    derivedStateOf {
                        allReadings.map { yearFormat.format(java.util.Date(it.first)).toInt() }
                            .distinct().sortedDescending()
                    }
                }
                var selectedYear by remember(availableYears) {
                    mutableIntStateOf(availableYears.firstOrNull() ?: 0)
                }
                val filteredReadings by remember(allReadings, selectedYear) {
                    derivedStateOf { allReadings.filter { yearFormat.format(java.util.Date(it.first)).toInt() == selectedYear } }
                }

                if (availableYears.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val idx = availableYears.indexOf(selectedYear)
                                if (idx < availableYears.lastIndex) selectedYear = availableYears[idx + 1]
                            },
                            enabled = availableYears.indexOf(selectedYear) < availableYears.lastIndex,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = {
                                val idx = availableYears.indexOf(selectedYear)
                                if (idx > 0) selectedYear = availableYears[idx - 1]
                            },
                            enabled = availableYears.indexOf(selectedYear) > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                filteredReadings.forEachIndexed { index, reading ->
                    val dateStr = dateFormat.format(java.util.Date(reading.first))
                    val consumption = if (index < filteredReadings.size - 1) {
                        reading.second - filteredReadings[index + 1].second
                    } else {
                        0f
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dateStr, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(
                                R.string.reading_value,
                                String.format("%.2f", reading.second),
                                meterConfig.unit
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (index < filteredReadings.size - 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                stringResource(
                                    R.string.consumption_suffix,
                                    String.format("%.2f", consumption),
                                    meterConfig.unit
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = value,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
