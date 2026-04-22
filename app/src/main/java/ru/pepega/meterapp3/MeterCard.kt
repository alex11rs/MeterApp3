package ru.pepega.meterapp3

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.ui.common.formatPlainNumber
import ru.pepega.meterapp3.theme.ThemePreset
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollapsibleMeterCard(
    modifier: Modifier = Modifier,
    meterConfig: MeterConfig,
    meterData: MeterData,
    unit: String = "m3",
    currency: String = "\u20BD",
    tariffsEnabled: Boolean = true,
    onDataChanged: () -> Unit,
    onAddClick: () -> Unit,
    onInfoClick: (MeterConfig) -> Unit,
    onEditClick: (MeterConfig) -> Unit,
    onDeleteClick: (MeterConfig) -> Unit,
    shouldHighlightRecentUpdate: Boolean = false,
    recentHighlightNonce: Int = 0,
    forceUpdate: Int
) {
    val isSadMeterTheme = rememberAppPreferences().themePreset() == ThemePreset.FOREST
    var showMenu by remember { mutableStateOf(false) }

    val cardColor = getColorByName(meterConfig.color)
    val cardShape = RoundedCornerShape(16.dp)
    val cardInteractionSource = remember { MutableInteractionSource() }
    val accentColor = lerp(cardColor, MaterialTheme.colorScheme.primary, 0.22f)
    var showRecentHighlight by remember { mutableStateOf(false) }
    val recentHighlightProgress by animateFloatAsState(
        targetValue = if (showRecentHighlight) 1f else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "recentHighlightProgress"
    )
    val animatedContainerColor = lerp(
        cardColor.copy(alpha = 0.25f),
        accentColor.copy(alpha = 0.34f),
        recentHighlightProgress
    )
    val borderBrush = remember(cardColor) {
        Brush.linearGradient(
            colors = listOf(
                lerp(cardColor, Color.White, 0.12f).copy(alpha = 0.68f),
                cardColor.copy(alpha = 0.64f),
                lerp(cardColor, Color.Black, 0.03f).copy(alpha = 0.66f)
            )
        )
    }

    val verificationStatus = remember(meterConfig.verificationDate, meterConfig.validityYears) {
        getVerificationStatus(meterConfig.verificationDate, meterConfig.validityYears)
    }
    val hasCurrentMonthReading = remember(meterData.lastUpdate) {
        if (meterData.lastUpdate <= 0L) {
            false
        } else {
            val now = Calendar.getInstance()
            val lastUpdateCalendar = Calendar.getInstance().apply {
                timeInMillis = meterData.lastUpdate
            }
            now.get(Calendar.MONTH) == lastUpdateCalendar.get(Calendar.MONTH) &&
            now.get(Calendar.YEAR) == lastUpdateCalendar.get(Calendar.YEAR)
        }
    }
    val checkBadgeScale by animateFloatAsState(
        targetValue = if (showRecentHighlight) 1.22f else 1f,
        animationSpec = tween(durationMillis = 420),
        label = "checkBadgeScale"
    )
    val checkBadgeAlpha by animateFloatAsState(
        targetValue = if (hasCurrentMonthReading) {
            if (showRecentHighlight) 1f else 0.92f
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 320),
        label = "checkBadgeAlpha"
    )
    LaunchedEffect(shouldHighlightRecentUpdate, recentHighlightNonce) {
        if (shouldHighlightRecentUpdate) {
            showRecentHighlight = true
            kotlinx.coroutines.delay(1600)
            showRecentHighlight = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(72.dp)
            .combinedClickable(
                interactionSource = cardInteractionSource,
                indication = LocalIndication.current,
                onClick = onAddClick,
                onLongClick = { showMenu = true }
            )
            .border(width = 1.dp, brush = borderBrush, shape = cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f + (0.04f * recentHighlightProgress)),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                                accentColor.copy(alpha = 0.02f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f + (0.03f * recentHighlightProgress)),
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            radius = 460f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .aspectRatio(1f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.24f + (0.12f * recentHighlightProgress)),
                                    accentColor.copy(alpha = 0.08f + (0.06f * recentHighlightProgress))
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MeterThemeIcon(
                        icon = meterConfig.icon,
                        meterConfig = meterConfig,
                        fallbackTitle = meterConfig.name,
                        fallbackUnit = meterConfig.unit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isSadMeterTheme) SadMeterCardIconPadding else 4.dp),
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meterConfig.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (meterConfig.tariffType == MeterTariffType.DUAL) {
                            stringResource(
                                R.string.day_night_compact_value,
                                formatPlainNumber(meterData.current),
                                formatPlainNumber(meterData.currentNight),
                                unit
                            )
                        } else {
                            "${formatPlainNumber(meterData.current)} $unit"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (verificationStatus !is VerificationStatus.OK && verificationStatus !is VerificationStatus.NOT_SET) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = verificationStatus.getColor().copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = verificationStatus.getIcon(),
                            fontSize = 18.sp,
                            color = verificationStatus.getColor()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .scale(checkBadgeScale)
                        .background(
                            color = lerp(
                                MaterialTheme.colorScheme.primary,
                                accentColor,
                                recentHighlightProgress * 0.35f
                            ).copy(alpha = checkBadgeAlpha * 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = lerp(
                            MaterialTheme.colorScheme.primary,
                            accentColor,
                            recentHighlightProgress * 0.35f
                        ).copy(alpha = checkBadgeAlpha)
                    )
                }

                Box {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = lerp(cardColor.copy(alpha = 0.18f), MaterialTheme.colorScheme.surface, 0.78f),
                        tonalElevation = 4.dp,
                        shadowElevation = 2.dp
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.meter_details_menu)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onInfoClick(meterConfig)
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.meter_settings_menu)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEditClick(meterConfig)
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.meter_delete_menu)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick(meterConfig)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        }
    }
}

