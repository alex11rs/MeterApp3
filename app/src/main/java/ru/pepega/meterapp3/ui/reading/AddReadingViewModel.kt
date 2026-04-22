package ru.pepega.meterapp3.ui.reading

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterReadingHistoryEntry
import ru.pepega.meterapp3.MeterRepository
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.VerificationInfo
import ru.pepega.meterapp3.getVerificationInfo
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val ANOMALY_MULTIPLIER = 2.5f

data class PendingReading(
    val dayValue: Float,
    val nightValue: Float
)

data class ConsumptionAnomalyDialogState(
    val title: String,
    val joke: String
)

data class AddReadingUiState(
    val meterData: MeterData = MeterData(),
    val inputText: String = "",
    val nightInputText: String = "",
    val isSaving: Boolean = false,
    val messageResId: Int? = null,
    val isMessageError: Boolean = false,
    val isEditMode: Boolean = false,
    val hasCurrentMonthReading: Boolean = false,
    val verificationInfo: VerificationInfo? = null,
    val anomalyDialogState: ConsumptionAnomalyDialogState? = null
)

sealed interface AddReadingSubmitResult {
    data class Saved(val messageResId: Int) : AddReadingSubmitResult
    data object ShowAnomalyDialog : AddReadingSubmitResult
    data class ValidationError(val messageResId: Int) : AddReadingSubmitResult
}

class AddReadingViewModel(
    private val appContext: Context,
    private val repository: MeterRepository,
    private val meterConfig: MeterConfig,
    initialText: String?,
    initialNightText: String?
) : ViewModel() {
    private var lastAppliedInitialText: String? = null
    private var lastAppliedInitialNightText: String? = null
    private var pendingAnomalousReading: PendingReading? = null

    val uiState: StateFlow<AddReadingUiState> = repository.observeMeterData(meterConfig.id).map { meterData ->
        val verificationInfo = getVerificationInfo(meterConfig.verificationDate, meterConfig.validityYears)
        AddReadingUiState(
            meterData = meterData,
            inputText = initialText.orEmpty(),
            nightInputText = initialNightText.orEmpty(),
            hasCurrentMonthReading = hasReadingThisMonth(meterData),
            verificationInfo = verificationInfo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddReadingUiState(
            inputText = initialText.orEmpty(),
            nightInputText = initialNightText.orEmpty(),
            verificationInfo = getVerificationInfo(meterConfig.verificationDate, meterConfig.validityYears)
        )
    )

    private val _screenState = kotlinx.coroutines.flow.MutableStateFlow(
        uiState.value.copy(
            inputText = initialText.orEmpty(),
            nightInputText = initialNightText.orEmpty()
        )
    )

    val screenState: StateFlow<AddReadingUiState> = kotlinx.coroutines.flow.combine(
        uiState,
        _screenState
    ) { dataState, localState ->
        dataState.copy(
            inputText = localState.inputText,
            nightInputText = localState.nightInputText,
            isSaving = localState.isSaving,
            messageResId = localState.messageResId,
            isMessageError = localState.isMessageError,
            isEditMode = localState.isEditMode,
            anomalyDialogState = localState.anomalyDialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _screenState.value
    )

    fun applyInitialReadings(initialText: String?, initialNightText: String?) {
        if (initialText != null && initialText != lastAppliedInitialText) {
            _screenState.value = _screenState.value.copy(inputText = initialText)
            lastAppliedInitialText = initialText
        }
        if (initialNightText != null && initialNightText != lastAppliedInitialNightText) {
            _screenState.value = _screenState.value.copy(nightInputText = initialNightText)
            lastAppliedInitialNightText = initialNightText
        }
    }

    fun enterEditMode() {
        val meterData = screenState.value.meterData
        _screenState.value = _screenState.value.copy(
            isEditMode = true,
            inputText = meterData.current.toString(),
            nightInputText = if (meterConfig.tariffType == MeterTariffType.DUAL) {
                meterData.currentNight.toString()
            } else {
                _screenState.value.nightInputText
            }
        )
    }

    fun updateInputText(value: String) {
        _screenState.value = _screenState.value.copy(inputText = value)
    }

    fun updateNightInputText(value: String) {
        _screenState.value = _screenState.value.copy(nightInputText = value)
    }

    fun clearInput() {
        _screenState.value = _screenState.value.copy(inputText = "")
    }

    fun clearNightInput() {
        _screenState.value = _screenState.value.copy(nightInputText = "")
    }

    fun dismissAnomalyDialog() {
        pendingAnomalousReading = null
        _screenState.value = _screenState.value.copy(anomalyDialogState = null)
    }

    suspend fun submit(): AddReadingSubmitResult {
        val state = screenState.value
        if (state.isSaving) return AddReadingSubmitResult.ValidationError(R.string.enter_number)
        val meterData = state.meterData
        val newValue = state.inputText.toFloatOrNull()
        val newNightValue = if (meterConfig.tariffType == MeterTariffType.DUAL) {
            state.nightInputText.toFloatOrNull()
        } else {
            0f
        }

        if (newValue == null || (meterConfig.tariffType == MeterTariffType.DUAL && newNightValue == null)) {
            return setValidationError(ru.pepega.meterapp3.R.string.enter_number)
        }
        if (!state.isEditMode && newValue < meterData.current) {
            return setValidationError(R.string.reading_less_than_current)
        }
        if (
            meterConfig.tariffType == MeterTariffType.DUAL &&
            !state.isEditMode &&
            newNightValue != null &&
            newNightValue < meterData.currentNight
        ) {
            return setValidationError(R.string.night_reading_less_than_current)
        }
        if (!state.isEditMode && shouldShowConsumptionAnomaly(meterData, newValue, newNightValue ?: 0f)) {
            pendingAnomalousReading = PendingReading(
                dayValue = newValue,
                nightValue = newNightValue ?: 0f
            )
            _screenState.value = state.copy(
                anomalyDialogState = buildConsumptionAnomalyDialogState()
            )
            return AddReadingSubmitResult.ShowAnomalyDialog
        }

        _screenState.value = state.copy(isSaving = true)
        return try {
            persistReading(
                newValue = newValue,
                newNightValue = newNightValue ?: 0f,
                isEditMode = state.isEditMode,
                meterData = meterData
            )
            val successRes = if (state.isEditMode) {
                ru.pepega.meterapp3.R.string.reading_fixed
            } else {
                ru.pepega.meterapp3.R.string.reading_saved
            }
            _screenState.value = screenState.value.copy(
                isSaving = false,
                messageResId = null,
                isMessageError = false,
                isEditMode = false,
                anomalyDialogState = null
            )
            AddReadingSubmitResult.Saved(successRes)
        } catch (_: Exception) {
            _screenState.value = screenState.value.copy(
                isSaving = false,
                messageResId = R.string.enter_number,
                isMessageError = true
            )
            AddReadingSubmitResult.ValidationError(R.string.enter_number)
        }
    }

    suspend fun confirmAnomalousSave(): AddReadingSubmitResult {
        val state = screenState.value
        val pendingReading = pendingAnomalousReading ?: return AddReadingSubmitResult.ValidationError(R.string.enter_number)
        _screenState.value = state.copy(isSaving = true)
        return try {
            persistReading(
                newValue = pendingReading.dayValue,
                newNightValue = pendingReading.nightValue,
                isEditMode = state.isEditMode,
                meterData = state.meterData
            )
            pendingAnomalousReading = null
            _screenState.value = screenState.value.copy(
                isSaving = false,
                messageResId = null,
                isMessageError = false,
                isEditMode = false,
                anomalyDialogState = null
            )
            val successRes = if (state.isEditMode) R.string.reading_fixed else R.string.reading_saved
            AddReadingSubmitResult.Saved(successRes)
        } catch (_: Exception) {
            _screenState.value = screenState.value.copy(
                isSaving = false,
                messageResId = R.string.enter_number,
                isMessageError = true
            )
            AddReadingSubmitResult.ValidationError(R.string.enter_number)
        }
    }

    private suspend fun persistReading(
        newValue: Float,
        newNightValue: Float,
        isEditMode: Boolean,
        meterData: MeterData
    ) {
        val isInitialReading = !isEditMode && meterData.lastUpdate <= 0L
        val newEntry = if (!isEditMode && meterData.lastUpdate > 0) {
            MeterReadingHistoryEntry(
                value = meterData.current,
                timestamp = meterData.lastUpdate,
                secondaryValue = if (meterConfig.tariffType == MeterTariffType.DUAL) meterData.currentNight else null
            )
        } else null

        repository.saveMeterData(
            meterConfig.id,
            meterData.copy(
                previous = when {
                    isEditMode -> meterData.previous
                    isInitialReading -> newValue
                    else -> meterData.current
                },
                current = newValue,
                previousNight = if (meterConfig.tariffType == MeterTariffType.DUAL) {
                    when {
                        isEditMode -> meterData.previousNight
                        isInitialReading -> newNightValue
                        else -> meterData.currentNight
                    }
                } else {
                    meterData.previousNight
                },
                currentNight = if (meterConfig.tariffType == MeterTariffType.DUAL) {
                    newNightValue
                } else {
                    meterData.currentNight
                },
                lastUpdate = System.currentTimeMillis()
            )
        )
        newEntry?.let { repository.addHistoryEntry(meterConfig.id, it) }
    }

    private fun setValidationError(messageResId: Int): AddReadingSubmitResult.ValidationError {
        _screenState.value = screenState.value.copy(
            messageResId = messageResId,
            isMessageError = true,
            anomalyDialogState = null
        )
        return AddReadingSubmitResult.ValidationError(messageResId)
    }

    private fun shouldShowConsumptionAnomaly(
        meterData: MeterData,
        newValue: Float,
        newNightValue: Float
    ): Boolean {
        if (meterData.lastUpdate <= 0L) return false

        val currentConsumption = ((newValue - meterData.current) + if (meterConfig.tariffType == MeterTariffType.DUAL) {
            newNightValue - meterData.currentNight
        } else {
            0f
        }).coerceAtLeast(0f)
        if (currentConsumption <= 0f) return false

        val previousConsumption = calculatePreviousConsumption(meterData) ?: return false
        return previousConsumption > 0f && currentConsumption > previousConsumption * ANOMALY_MULTIPLIER
    }

    private fun calculatePreviousConsumption(meterData: MeterData): Float? {
        val previousEntry = meterData.history.lastOrNull() ?: return null

        val dayConsumption = (meterData.current - previousEntry.value).coerceAtLeast(0f)
        val nightConsumption = if (meterConfig.tariffType == MeterTariffType.DUAL) {
            (meterData.currentNight - (previousEntry.secondaryValue ?: 0f)).coerceAtLeast(0f)
        } else {
            0f
        }

        val totalConsumption = dayConsumption + nightConsumption
        return totalConsumption.takeIf { it > 0f }
    }

    private fun buildConsumptionAnomalyDialogState(): ConsumptionAnomalyDialogState {
        val jokeArrayRes = getJokeArrayRes()
        val joke = appContext.resources.getStringArray(jokeArrayRes).random()
        return ConsumptionAnomalyDialogState(
            title = getAnomalyDialogTitle(jokeArrayRes),
            joke = joke
        )
    }

    @ArrayRes
    private fun getJokeArrayRes(): Int {
        val meterId = meterConfig.id.lowercase()
        val meterName = meterConfig.name.lowercase()

        return when {
            meterConfig.unit == "кВт·ч" -> R.array.jokes_electricity
            meterConfig.unit == "Гкал" -> R.array.jokes_heat
            meterConfig.unit == "м³" && (meterId.contains("gas") || meterName.contains("газ")) -> R.array.jokes_gas
            meterConfig.unit == "м³" && (
                meterId.contains("hot") ||
                    meterName.contains("горяч") ||
                    meterName.contains("hot")
                ) -> R.array.jokes_hot_water
            meterConfig.unit == "м³" -> R.array.jokes_cold_water
            else -> R.array.jokes_universal
        }
    }

    private fun getAnomalyDialogTitle(@ArrayRes jokeArrayRes: Int): String {
        return when (jokeArrayRes) {
            R.array.jokes_heat -> "Жара пошла!"
            R.array.jokes_electricity -> "Шок-контент! ⚡️"
            R.array.jokes_gas -> "Газ в пол!"
            R.array.jokes_hot_water -> "Кипяток какой-то!"
            R.array.jokes_cold_water -> "Ничего себе, потоп!"
            else -> "Ничего себе!"
        }
    }

    private fun hasReadingThisMonth(meterData: MeterData): Boolean {
        if (meterData.lastUpdate == 0L) return false
        val calendar = Calendar.getInstance()
        val lastUpdateCalendar = Calendar.getInstance().apply {
            timeInMillis = meterData.lastUpdate
        }
        return calendar.get(Calendar.MONTH) == lastUpdateCalendar.get(Calendar.MONTH) &&
            calendar.get(Calendar.YEAR) == lastUpdateCalendar.get(Calendar.YEAR)
    }
}

class AddReadingViewModelFactory(
    private val appContext: Context,
    private val repository: MeterRepository,
    private val meterConfig: MeterConfig,
    private val initialText: String?,
    private val initialNightText: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddReadingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddReadingViewModel(appContext, repository, meterConfig, initialText, initialNightText) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
