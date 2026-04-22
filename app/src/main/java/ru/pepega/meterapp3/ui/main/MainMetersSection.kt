package ru.pepega.meterapp3.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.CollapsibleMeterCard
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.SortType

@Composable
fun MainMetersSection(
    modifier: Modifier = Modifier,
    visibleMeters: List<MeterConfig>,
    meterDataById: Map<String, ru.pepega.meterapp3.MeterData>,
    currency: String,
    tariffsEnabled: Boolean,
    recentlyUpdatedMeterId: String?,
    recentHighlightNonce: Int,
    updateTrigger: Int,
    showOnlyPendingThisMonth: Boolean,
    sortType: SortType,
    showSortMenu: Boolean,
    showBottomStatusCard: Boolean,
    onAnyAction: () -> Unit,
    onShowOnlyPendingChange: (Boolean) -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    onUpdateTrigger: () -> Unit,
    onRefresh: () -> Unit,
    onAddReadingClick: (MeterConfig) -> Unit,
    onInfoClick: (MeterConfig) -> Unit,
    onEditClick: (MeterConfig) -> Unit,
    onDeleteClick: (MeterConfig) -> Unit
) {
    val controlsSpacing = 8.dp
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            onAnyAction()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        RowFilters(
            visibleMetersCount = visibleMeters.size,
            showOnlyPendingThisMonth = showOnlyPendingThisMonth,
            sortType = sortType,
            showSortMenu = showSortMenu,
            onAnyAction = onAnyAction,
            onShowOnlyPendingChange = onShowOnlyPendingChange,
            onShowSortMenuChange = onShowSortMenuChange,
            onSortTypeChange = onSortTypeChange,
            onUpdateTrigger = onUpdateTrigger
        )
    }

    if (showOnlyPendingThisMonth && visibleMeters.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Все счётчики за этот месяц заполнены",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = controlsSpacing)
            ) {
                itemsIndexed(
                    items = visibleMeters,
                    key = { _, meter -> "${meter.id}_$updateTrigger" }
                ) { index, meter ->
                    CollapsibleMeterCard(
                        modifier = Modifier,
                        meterConfig = meter,
                        meterData = meterDataById[meter.id] ?: ru.pepega.meterapp3.MeterData(),
                        unit = meter.unit,
                        currency = currency,
                        tariffsEnabled = tariffsEnabled,
                        onDataChanged = {
                            onAnyAction()
                            onRefresh()
                        },
                        onAddClick = {
                            onAnyAction()
                            onAddReadingClick(meter)
                        },
                        onInfoClick = {
                            onAnyAction()
                            onInfoClick(meter)
                        },
                        onEditClick = {
                            onAnyAction()
                            onEditClick(meter)
                        },
                        onDeleteClick = {
                            onAnyAction()
                            onDeleteClick(meter)
                        },
                        shouldHighlightRecentUpdate = recentlyUpdatedMeterId == meter.id,
                        recentHighlightNonce = recentHighlightNonce,
                        forceUpdate = updateTrigger
                    )

                    if (index < visibleMeters.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(horizontal = 28.dp)
                                .alpha(0.28f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(if (showBottomStatusCard) 68.dp else 24.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(controlsSpacing)
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

@Composable
private fun RowFilters(
    visibleMetersCount: Int,
    showOnlyPendingThisMonth: Boolean,
    sortType: SortType,
    showSortMenu: Boolean,
    onAnyAction: () -> Unit,
    onShowOnlyPendingChange: (Boolean) -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    onUpdateTrigger: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (showOnlyPendingThisMonth) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
            }
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .clickable {
                        onAnyAction()
                        val newValue = !showOnlyPendingThisMonth
                        onShowOnlyPendingChange(newValue)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showOnlyPendingThisMonth) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (showOnlyPendingThisMonth) {
                        "Не заполнены: $visibleMetersCount"
                    } else {
                        "Не заполнены"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (showOnlyPendingThisMonth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                    },
                    fontWeight = if (showOnlyPendingThisMonth) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }

        Box {
            Surface(
                modifier = Modifier.padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .clickable {
                            onAnyAction()
                            onShowSortMenuChange(true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Сортировка",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                    )
                    Text(
                        text = when (sortType) {
                            SortType.BY_NAME -> "Имя"
                            SortType.BY_LAST_UPDATE -> "Дата"
                            SortType.BY_CONSUMPTION -> "Расход"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
            ) {
                SortMenuItem(
                    title = "По имени",
                    selected = sortType == SortType.BY_NAME,
                    onClick = {
                        onAnyAction()
                        onSortTypeChange(SortType.BY_NAME)
                        onUpdateTrigger()
                        onShowSortMenuChange(false)
                    }
                )
                SortMenuItem(
                    title = "По дате",
                    selected = sortType == SortType.BY_LAST_UPDATE,
                    onClick = {
                        onAnyAction()
                        onSortTypeChange(SortType.BY_LAST_UPDATE)
                        onUpdateTrigger()
                        onShowSortMenuChange(false)
                    }
                )
                SortMenuItem(
                    title = "По расходу",
                    selected = sortType == SortType.BY_CONSUMPTION,
                    onClick = {
                        onAnyAction()
                        onSortTypeChange(SortType.BY_CONSUMPTION)
                        onUpdateTrigger()
                        onShowSortMenuChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(title) },
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
