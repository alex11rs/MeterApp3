package ru.pepega.meterapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.ui.details.MeterDetailsHeader
import ru.pepega.meterapp3.ui.details.MeterMainInfoCard
import ru.pepega.meterapp3.ui.details.MeterReadingsHistoryCard
import ru.pepega.meterapp3.ui.details.MeterStatisticsCard
import ru.pepega.meterapp3.ui.details.MeterDetailsViewModel
import ru.pepega.meterapp3.ui.details.MeterDetailsViewModelFactory

@Composable
fun MeterDetailsScreen(
    configs: List<MeterConfig>,
    meterConfig: MeterConfig,
    onBack: () -> Unit
) {
    val repository = rememberMeterRepository()
    val initialPage = configs.indexOfFirst { it.id == meterConfig.id }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { configs.size.coerceAtLeast(1) }
    val currentConfig = configs.getOrElse(pagerState.currentPage) { meterConfig }

    val meterDetailsViewModel: MeterDetailsViewModel = rememberViewModel(repository, currentConfig.id) {
        MeterDetailsViewModelFactory(repository, currentConfig)
    }
    val uiState = meterDetailsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        MeterDetailsHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        MeterMainInfoCard(
            configs = configs,
            pagerState = pagerState,
            meterConfig = currentConfig,
            meterData = uiState.value.meterData,
            verificationInfo = uiState.value.verificationInfo,
            totalCost = uiState.value.totalCost
        )

        MeterStatisticsCard(
            meterConfig = currentConfig,
            allReadings = uiState.value.allReadings,
            totalConsumptionChartPoints = uiState.value.totalConsumptionChartPoints,
            dayConsumptionChartPoints = uiState.value.dayConsumptionChartPoints,
            nightConsumptionChartPoints = uiState.value.nightConsumptionChartPoints,
            totalReadings = uiState.value.totalReadings,
            averageConsumption = uiState.value.averageConsumption,
            minConsumption = uiState.value.minConsumption,
            maxConsumption = uiState.value.maxConsumption
        )

        MeterReadingsHistoryCard(
            meterConfig = currentConfig,
            allReadings = uiState.value.allReadings
        )

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
