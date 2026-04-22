package ru.pepega.meterapp3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.pepega.meterapp3.reminders.ReminderManager
import ru.pepega.meterapp3.sharing.shareReadings
import ru.pepega.meterapp3.scanner.applyMeterDecimalDigits
import ru.pepega.meterapp3.scanner.normalizeCloudCandidate
import ru.pepega.meterapp3.theme.MeterAppTheme
import ru.pepega.meterapp3.theme.getThemePreset
import ru.pepega.meterapp3.ui.main.ExitConfirmationDialog
import ru.pepega.meterapp3.ui.main.MainScreenDialogs
import ru.pepega.meterapp3.ui.main.MainScreenContent
import ru.pepega.meterapp3.ui.main.MainScreenSnackbarHost
import ru.pepega.meterapp3.ui.main.MainScreenViewModel
import ru.pepega.meterapp3.ui.main.MainScreenViewModelFactory
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SortType {
    BY_NAME,
    BY_LAST_UPDATE,
    BY_CONSUMPTION
}

enum class Screen {
    MAIN, HISTORY, SUMMARY, ADD_READING, INFO, SCANNER, APARTMENTS
}

private fun expectedScanLength(
    meterData: MeterData?,
    readingField: ReadingField
): Int? {
    val rawValue = when (readingField) {
        ReadingField.DAY -> meterData?.current?.takeIf { it > 0f } ?: meterData?.previous?.takeIf { it > 0f }
        ReadingField.NIGHT -> meterData?.currentNight?.takeIf { it > 0f } ?: meterData?.previousNight?.takeIf { it > 0f }
    } ?: return null

    return BigDecimal.valueOf(rawValue.toDouble())
        .stripTrailingZeros()
        .toPlainString()
        .count { it.isDigit() }
        .coerceAtLeast(1)
}

private fun previousScanValue(
    meterData: MeterData?,
    readingField: ReadingField
): Long? {
    val rawValue = when (readingField) {
        ReadingField.DAY -> meterData?.current?.takeIf { it > 0f } ?: meterData?.previous?.takeIf { it > 0f }
        ReadingField.NIGHT -> meterData?.currentNight?.takeIf { it > 0f } ?: meterData?.previousNight?.takeIf { it > 0f }
    } ?: return null

    return rawValue.toLong().takeIf { it > 0L }
}

private sealed class MetersScreenState(val screen: Screen) {
    data object Main : MetersScreenState(Screen.MAIN)
    data object History : MetersScreenState(Screen.HISTORY)
    data object Summary : MetersScreenState(Screen.SUMMARY)
    data class AddReading(
        val meter: MeterConfig,
        val initialText: String? = null,
        val initialNightText: String? = null
    ) :
        MetersScreenState(Screen.ADD_READING)
    data class Info(val meter: MeterConfig) : MetersScreenState(Screen.INFO)
    data class Scanner(val meter: MeterConfig, val readingField: ReadingField = ReadingField.DAY) : MetersScreenState(Screen.SCANNER)
    data object Apartments : MetersScreenState(Screen.APARTMENTS)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences
    private lateinit var reminderManager: ReminderManager
    private var refreshCounter by mutableStateOf(0)
    private val prefsChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        refreshCounter++
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderManager.scheduleReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appPreferences = AppPreferences.from(this)
        reminderManager = ReminderManager(this, appPreferences)
        appPreferences.registerChangeListener(prefsChangeListener)

        setContent {
            val themePreset = appPreferences.themePreset()

            MeterAppTheme(themePreset = themePreset) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.TopCenter
                    ) {
                        Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()) {
                            MetersListScreen(
                                reminderManager = reminderManager,
                                refreshCounter = refreshCounter,
                                requestPermission = {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                onRefresh = { refreshCounter++ },
                                onExit = { finish() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::appPreferences.isInitialized) {
            appPreferences.unregisterChangeListener(prefsChangeListener)
        }
        super.onDestroy()
    }

    fun resetSubmissionFlag() {
        appPreferences.resetCurrentMonthSubmitted()
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MetersListScreen(
    reminderManager: ReminderManager,
    refreshCounter: Int,
    requestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val appPreferences = rememberAppPreferences()
    val prefs = appPreferences.sharedPreferences
    val repository = rememberMeterRepository()
    val mainScreenViewModel: MainScreenViewModel = rememberViewModel(repository) {
        MainScreenViewModelFactory(repository)
    }
    val mainScreenState by mainScreenViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var screenState by remember { mutableStateOf<MetersScreenState>(MetersScreenState.Main) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedMeterForEdit by remember { mutableStateOf<MeterConfig?>(null) }
    var showAddMeterDialog by remember { mutableStateOf(false) }
    var updateTrigger by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var recentlyUpdatedMeterId by remember { mutableStateOf<String?>(null) }
    var recentHighlightNonce by remember { mutableStateOf(0) }
    LaunchedEffect(refreshCounter) {
        mainScreenViewModel.syncActiveApartment()
    }
    val apartments = mainScreenState.apartments
    val activeApartmentId = mainScreenState.activeApartmentId
    val configs = mainScreenState.configs
    val meterDataById = mainScreenState.meterDataById
    val currency = appPreferences.currency()
    val tariffsEnabled = appPreferences.tariffsEnabled()
    val activeApartment = remember(apartments, activeApartmentId) {
        apartments.firstOrNull { it.id == activeApartmentId } ?: apartments.firstOrNull()
    }
    val summary = remember(configs, tariffsEnabled, meterDataById) {
        MeterStatistics.calculateTotalSummary(
            configs = configs,
            tariffsEnabled = tariffsEnabled,
            meterDataById = meterDataById
        )
    }
    val hasBottomStatusCard = configs.any { it.enabled }

    fun refreshApartmentData() {
        mainScreenViewModel.syncActiveApartment()
        updateTrigger++
    }

    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                kotlinx.coroutines.delay(2_000)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = screenState.screen == Screen.MAIN) {
        showExitDialog = true
    }

    BackHandler(enabled = screenState.screen != Screen.MAIN) {
        screenState = MetersScreenState.Main
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onExit = {
                showExitDialog = false
                onExit()
            }
        )
    }
    val screenOrder = listOf(
        Screen.MAIN,
        Screen.HISTORY,
        Screen.SUMMARY,
        Screen.ADD_READING,
        Screen.INFO,
        Screen.SCANNER,
        Screen.APARTMENTS
    )

    val renderMainScreen: @Composable () -> Unit = {
        activeApartment?.let { currentApartment ->
            MainScreenContent(
                apartments = apartments,
                activeApartment = currentApartment,
                configs = configs,
                meterDataById = meterDataById,
                currency = currency,
                tariffsEnabled = tariffsEnabled,
                summary = summary,
                context = context,
                onApartmentSelect = mainScreenViewModel::selectApartment,
                onShowHistory = { screenState = MetersScreenState.History },
                onShowSummary = { screenState = MetersScreenState.Summary },
                onShowSettings = { showSettings = true },
                onRefresh = { refreshApartmentData() },
                onAddMeterClick = { showAddMeterDialog = true },
                onAddReadingClick = { meter -> screenState = MetersScreenState.AddReading(meter) },
                onInfoClick = { meter -> screenState = MetersScreenState.Info(meter) },
                onEditClick = { meter -> selectedMeterForEdit = meter },
                recentlyUpdatedMeterId = recentlyUpdatedMeterId,
                recentHighlightNonce = recentHighlightNonce,
                updateTrigger = updateTrigger,
                onUpdateTrigger = { updateTrigger++ },
                refreshTrigger = refreshCounter
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screenState.screen,
            transitionSpec = {
                val transitionDuration = 300
                val currentIndex = screenOrder.indexOf(initialState)
                val targetIndex = screenOrder.indexOf(targetState)
                if (targetIndex > currentIndex) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(transitionDuration)
                    ) + fadeIn(animationSpec = tween(transitionDuration)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(transitionDuration)
                        ) + fadeOut(animationSpec = tween(transitionDuration))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(transitionDuration)
                    ) + fadeIn(animationSpec = tween(transitionDuration)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(transitionDuration)
                        ) + fadeOut(animationSpec = tween(transitionDuration))
                }.using(SizeTransform(clip = false))
            },
            label = "screen_transitions"
        ) { screen ->
            when (screen) {
                Screen.HISTORY -> HistoryScreen(
                    configs = configs,
                    tariffsEnabled = tariffsEnabled,
                    onBack = { screenState = MetersScreenState.Main },
                    onShareClick = {
                        coroutineScope.launch {
                            shareReadings(
                                context = context,
                                repository = repository,
                                configs = configs
                            )
                        }
                    }
                )

                Screen.SUMMARY -> SummaryScreen(
                    configs = configs,
                    currency = currency,
                    tariffsEnabled = tariffsEnabled,
                    onBack = { screenState = MetersScreenState.Main }
                )

                Screen.APARTMENTS -> ApartmentManagementScreen(
                    onBack = {
                        refreshApartmentData()
                        screenState = MetersScreenState.Main
                    },
                    onChanged = {
                        refreshApartmentData()
                    }
                )

                Screen.ADD_READING -> (screenState as? MetersScreenState.AddReading)?.let { addReading ->
                    AddReadingScreen(
                        meterConfig = addReading.meter,
                        initialText = addReading.initialText,
                        initialNightText = addReading.initialNightText,
                        onBack = { screenState = MetersScreenState.Main },
                        onScanClick = { readingField ->
                            screenState = MetersScreenState.Scanner(addReading.meter, readingField)
                        },
                        onSaved = { successMessage ->
                            recentlyUpdatedMeterId = addReading.meter.id
                            recentHighlightNonce++
                            screenState = MetersScreenState.Main
                        },
                        onInfoClick = { meter ->
                            screenState = MetersScreenState.Info(meter)
                        },
                        onEditClick = { meter ->
                            selectedMeterForEdit = meter
                        },
                        onDeleted = { successMessage ->
                            screenState = MetersScreenState.Main
                            onRefresh()
                            showSnackbar(successMessage)
                        }
                    )
                } ?: renderMainScreen()

                Screen.INFO -> (screenState as? MetersScreenState.Info)?.let { infoState ->
                    MeterDetailsScreen(
                        configs = configs,
                        meterConfig = infoState.meter,
                        onBack = { screenState = MetersScreenState.Main }
                    )
                } ?: renderMainScreen()

                Screen.SCANNER -> (screenState as? MetersScreenState.Scanner)?.let { scannerState ->
                    MeterScannerScreen(
                        expectedLength = expectedScanLength(
                            meterData = meterDataById[scannerState.meter.id],
                            readingField = scannerState.readingField
                        ),
                        previousReadingValue = previousScanValue(
                            meterData = meterDataById[scannerState.meter.id],
                            readingField = scannerState.readingField
                        ),
                        onBack = {
                            screenState = MetersScreenState.AddReading(meter = scannerState.meter)
                        },
                        onTextRecognized = { recognizedText ->
                            val normalizedText = applyMeterDecimalDigits(
                                value = normalizeCloudCandidate(recognizedText),
                                decimalDigits = scannerState.meter.decimalDigits
                            )
                            screenState = MetersScreenState.AddReading(
                                meter = scannerState.meter,
                                initialText = if (scannerState.readingField == ReadingField.DAY) normalizedText else null,
                                initialNightText = if (scannerState.readingField == ReadingField.NIGHT) normalizedText else null
                            )
                        }
                    )
                } ?: renderMainScreen()

                else -> renderMainScreen()
            }
        }

        MainScreenSnackbarHost(
            snackbarHostState = snackbarHostState,
            screen = screenState.screen,
            hasBottomStatusCard = hasBottomStatusCard,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }

    MainScreenDialogs(
        reminderManager = reminderManager,
        configs = configs,
        showSettings = showSettings,
        showAddMeterDialog = showAddMeterDialog,
        selectedMeterForEdit = selectedMeterForEdit,
        requestPermission = requestPermission,
        onShowMessage = ::showSnackbar,
        onRefresh = { refreshApartmentData() },
        onDismissSettings = { showSettings = false },
        onOpenApartments = {
            showSettings = false
            screenState = MetersScreenState.Apartments
        },
        onDismissAddMeter = { showAddMeterDialog = false },
        onMeterAdded = {
            showAddMeterDialog = false
            refreshApartmentData()
        },
        onDismissMeterEdit = { selectedMeterForEdit = null },
        onSaveMeterConfig = { updatedConfig ->
            coroutineScope.launch {
                repository.updateMeterConfig(updatedConfig)
            }
        },
        onResetMeterData = { meter ->
            coroutineScope.launch {
                repository.resetMeterData(meter.id)
            }
        },
        onMeterUpdated = {
            refreshApartmentData()
            selectedMeterForEdit = null
            showSnackbar(context.getString(R.string.meter_updated))
        },
        onMeterDataReset = {
            refreshApartmentData()
            updateTrigger++
            selectedMeterForEdit = null
            showSnackbar(context.getString(R.string.meter_reset))
        }
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
