package ru.pepega.meterapp3.ui.main

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.TotalSummary
import ru.pepega.meterapp3.rememberMeterRepository

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    apartments: List<Apartment>,
    activeApartment: Apartment,
    configs: List<MeterConfig>,
    meterDataById: Map<String, MeterData>,
    currency: String,
    tariffsEnabled: Boolean,
    summary: TotalSummary,
    context: Context,
    onApartmentSelect: (String) -> Unit,
    onShowHistory: () -> Unit,
    onShowSummary: () -> Unit,
    onShowSettings: () -> Unit,
    onRefresh: () -> Unit,
    onAddMeterClick: () -> Unit,
    onAddReadingClick: (MeterConfig) -> Unit,
    onInfoClick: (MeterConfig) -> Unit,
    onEditClick: (MeterConfig) -> Unit,
    recentlyUpdatedMeterId: String?,
    recentHighlightNonce: Int,
    updateTrigger: Int,
    onUpdateTrigger: () -> Unit,
    refreshTrigger: Int = 0
) {
    val repository = rememberMeterRepository()
    val scope = rememberCoroutineScope()
    val controlsSpacing = 8.dp
    val state = rememberMainScreenContentState()
    val derivedState = rememberMainScreenDerivedState(
        configs = configs,
        meterDataById = meterDataById,
        summary = summary,
        sortType = state.sortType,
        showOnlyPendingThisMonth = state.showOnlyPendingThisMonth,
        refreshTrigger = refreshTrigger
    )

    MainContentDialogs(
        meterPendingDeletion = state.meterPendingDeletion,
        showMainGuideDialog = state.showMainGuideDialog,
        onDismissDelete = { state.meterPendingDeletion = null },
        onDeleteConfirmed = { meter ->
            scope.launch {
                repository.deleteMeterConfigById(meter.id)
                state.meterPendingDeletion = null
                onRefresh()
            }
        },
        onDismissGuide = state::dismissMainGuide
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        val expandedStatusCardSpacing = 116.dp
        val compactStatusCardSpacing = 68.dp

        LaunchedEffect(derivedState.visibleMeters.size) {
            state.collapseStatusCard()
        }
        LaunchedEffect(derivedState.reminderEnabled) {
            if (!derivedState.reminderEnabled) {
                state.collapseStatusCard()
            }
        }

        val statusCardBottomSpacing by animateDpAsState(
            targetValue = if (state.statusCardExpanded) expandedStatusCardSpacing else compactStatusCardSpacing,
            label = "statusCardBottomSpacing"
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                MainDashboardHeader(
                    apartments = apartments,
                    activeApartment = activeApartment,
                    configs = configs,
                    showMainGuideButton = state.showMainGuideButton,
                    showApartmentMenu = state.showApartmentMenu,
                    onShowApartmentMenuChange = { state.showApartmentMenu = it },
                    onAddMeterClick = {
                        state.collapseStatusCard()
                        onAddMeterClick()
                    },
                    onApartmentSelect = {
                        state.collapseStatusCard()
                        onApartmentSelect(it)
                    },
                    onShowGuide = { state.showMainGuideDialog = true },
                    onShowHistory = {
                        state.collapseStatusCard()
                        onShowHistory()
                    },
                    onShowSettings = {
                        state.collapseStatusCard()
                        onShowSettings()
                    }
                )

                MainDashboardSummaryCard(
                    summary = summary,
                    currency = currency,
                    tariffsEnabled = tariffsEnabled,
                    currentMonthPrepositional = derivedState.currentMonthPrepositional,
                    hasSummaryCalculation = derivedState.hasSummaryCalculation,
                    configs = configs,
                    meterDataById = meterDataById,
                    onShowSummary = {
                        state.collapseStatusCard()
                        onShowSummary()
                    }
                )

                Spacer(modifier = Modifier.height(controlsSpacing))

                MainMetersSection(
                    modifier = Modifier.weight(1f),
                    visibleMeters = derivedState.visibleMeters,
                    meterDataById = meterDataById,
                    currency = currency,
                    tariffsEnabled = tariffsEnabled,
                    recentlyUpdatedMeterId = recentlyUpdatedMeterId,
                    recentHighlightNonce = recentHighlightNonce,
                    updateTrigger = updateTrigger,
                    showOnlyPendingThisMonth = state.showOnlyPendingThisMonth,
                    sortType = state.sortType,
                    showSortMenu = state.showSortMenu,
                    showBottomStatusCard = derivedState.showBottomStatusCard,
                    onAnyAction = state::collapseStatusCard,
                    onShowOnlyPendingChange = state::persistPendingFilter,
                    onShowSortMenuChange = { state.showSortMenu = it },
                    onSortTypeChange = state::persistSortType,
                    onUpdateTrigger = onUpdateTrigger,
                    onRefresh = onRefresh,
                    onAddReadingClick = {
                        state.collapseStatusCard()
                        onAddReadingClick(it)
                    },
                    onInfoClick = {
                        state.collapseStatusCard()
                        onInfoClick(it)
                    },
                    onEditClick = {
                        state.collapseStatusCard()
                        onEditClick(it)
                    },
                    onDeleteClick = {
                        state.collapseStatusCard()
                        state.meterPendingDeletion = it
                    }
                )
            }
        }

        if (derivedState.showBottomStatusCard) {
            BottomStatusCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp),
                state = derivedState.bottomStatusCardState,
                expanded = state.statusCardExpanded,
                onExpandedChange = { state.statusCardExpanded = it }
            )
        }
    }
}
