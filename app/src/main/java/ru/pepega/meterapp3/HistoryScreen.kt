package ru.pepega.meterapp3

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.MeterThemeIcon
import ru.pepega.meterapp3.ui.common.copyTextToClipboard
import ru.pepega.meterapp3.ui.common.showShortUserMessage
import ru.pepega.meterapp3.ui.history.HistoryViewModel
import ru.pepega.meterapp3.ui.history.HistoryViewModelFactory

@Composable
fun HistoryScreen(
    configs: List<MeterConfig>,
    tariffsEnabled: Boolean,
    onBack: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = rememberMeterRepository()
    val historyViewModel: HistoryViewModel = rememberViewModel(repository, configs, tariffsEnabled) {
        HistoryViewModelFactory(repository, configs, tariffsEnabled)
    }
    val history = historyViewModel.uiState.collectAsState().value.history
    val availableYears by remember(history) {
        derivedStateOf {
            history.map { it.month.substringAfterLast(".") }.distinct().sortedDescending()
        }
    }
    var selectedYear by remember(availableYears) {
        mutableIntStateOf(availableYears.firstOrNull()?.toIntOrNull() ?: 0)
    }
    val filteredHistory by remember(history, selectedYear) {
        derivedStateOf {
            history.filter { it.month.endsWith(".$selectedYear") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (availableYears.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val idx = availableYears.indexOf(selectedYear.toString())
                            if (idx < availableYears.lastIndex) selectedYear = availableYears[idx + 1].toInt()
                        },
                        enabled = availableYears.indexOf(selectedYear.toString()) < availableYears.lastIndex,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = {
                            val idx = availableYears.indexOf(selectedYear.toString())
                            if (idx > 0) selectedYear = availableYears[idx - 1].toInt()
                        },
                        enabled = availableYears.indexOf(selectedYear.toString()) > 0,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    availableYears.forEach { year ->
                        val isSelected = year == selectedYear.toString()
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .background(
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
            filteredHistory.forEachIndexed { index, monthData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.history_month_prefix, monthData.month),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        monthData.meters.forEach { meter ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                val copiedValue = if (meter.tariffType == MeterTariffType.DUAL) {
                                                    "${String.format("%.2f", meter.reading)} / ${String.format("%.2f", meter.nightReading)}"
                                                } else {
                                                    String.format("%.2f", meter.reading)
                                                }
                                                copyTextToClipboard(
                                                    context = context,
                                                    label = context.getString(R.string.copy_value_label),
                                                    text = copiedValue
                                                )
                                                showShortUserMessage(
                                                    context,
                                                    context.getString(R.string.copied_value, copiedValue)
                                                )
                                            }
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            MeterThemeIcon(
                                                icon = meter.icon,
                                                meterConfig = null,
                                                fallbackTitle = meter.name,
                                                fallbackUnit = meter.unit,
                                                modifier = Modifier.size(24.dp),
                                                fontSize = 20.sp
                                            )
                                            Text(
                                                text = meter.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = if (meter.tariffType == MeterTariffType.DUAL) {
                                                if (meter.consumption > 0f)
                                                    "+${String.format("%.2f", meter.consumption)} ${meter.unit}"
                                                else
                                                    meter.unit
                                            } else {
                                                buildString {
                                                    append(stringResource(
                                                        R.string.reading_value,
                                                        String.format("%.2f", meter.reading),
                                                        meter.unit
                                                    ))
                                                    if (meter.consumption > 0f) {
                                                        append(" (+${String.format("%.2f", meter.consumption)})")
                                                    }
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (meter.tariffType == MeterTariffType.DUAL) {
                                        Text(
                                            text = "День: ${String.format("%.2f", meter.reading)}  Ночь: ${String.format("%.2f", meter.nightReading)} ${meter.unit}",
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
                if (index < filteredHistory.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0f)
                            )
                        )
                    )
            )
            }
        }
    }
}
