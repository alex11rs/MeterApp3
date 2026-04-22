package ru.pepega.meterapp3.ui.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.SortType

@Stable
class MainScreenContentState(
    private val appPreferences: AppPreferences
) {
    private val settings = appPreferences.mainScreenSettings()

    var sortType by mutableStateOf(
        settings.sortType
    )
    var showSortMenu by mutableStateOf(false)
    var showApartmentMenu by mutableStateOf(false)
    var showOnlyPendingThisMonth by mutableStateOf(
        settings.showOnlyPendingThisMonth
    )
    var meterPendingDeletion by mutableStateOf<MeterConfig?>(null)
    var showMainGuideButton by mutableStateOf(!settings.guideDismissed)
    var showMainGuideDialog by mutableStateOf(false)
    var statusCardExpanded by mutableStateOf(false)

    fun collapseStatusCard() {
        statusCardExpanded = false
    }

    fun dismissMainGuide() {
        showMainGuideDialog = false
        if (showMainGuideButton) {
            appPreferences.dismissMainGuide()
            showMainGuideButton = false
        }
    }

    fun persistSortType(sortType: SortType) {
        this.sortType = sortType
        appPreferences.setSortType(sortType)
    }

    fun persistPendingFilter(enabled: Boolean) {
        showOnlyPendingThisMonth = enabled
        appPreferences.setShowOnlyPendingThisMonth(enabled)
    }
}

@Composable
fun rememberMainScreenContentState(): MainScreenContentState {
    val appPreferences = ru.pepega.meterapp3.rememberAppPreferences()
    return remember(appPreferences) { MainScreenContentState(appPreferences) }
}
