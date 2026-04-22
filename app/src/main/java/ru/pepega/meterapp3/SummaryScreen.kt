package ru.pepega.meterapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.ui.summary.SummaryHeader
import ru.pepega.meterapp3.ui.summary.SummaryMetersTableCard
import ru.pepega.meterapp3.ui.summary.SummaryTotalCard
import ru.pepega.meterapp3.ui.summary.SummaryViewModel
import ru.pepega.meterapp3.ui.summary.SummaryViewModelFactory

@Composable
fun SummaryScreen(
    configs: List<MeterConfig>,
    currency: String,
    tariffsEnabled: Boolean,
    onBack: () -> Unit
) {
    val repository = rememberMeterRepository()
    val summaryViewModel: SummaryViewModel = rememberViewModel(repository, configs, tariffsEnabled) {
        SummaryViewModelFactory(repository, configs, tariffsEnabled)
    }
    val uiState = summaryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SummaryHeader(onBack = onBack)

        SummaryTotalCard(
            summary = uiState.value.summary,
            currency = currency,
            tariffsEnabled = tariffsEnabled,
            hasSummaryCalculation = uiState.value.hasSummaryCalculation
        )

        Spacer(modifier = Modifier.height(12.dp))

        SummaryMetersTableCard(
            summary = uiState.value.summary,
            tariffsEnabled = tariffsEnabled
        )
    }
}
