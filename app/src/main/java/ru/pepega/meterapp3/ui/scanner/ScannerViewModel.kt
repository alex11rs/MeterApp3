package ru.pepega.meterapp3.ui.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScannerUiState(
    val isProcessing: Boolean = false,
    val isFlashOn: Boolean = false,
    val pendingCloudCapture: Boolean = false,
    val cloudRequestInFlight: Boolean = false,
    val showSelectionDialog: Boolean = false,
    val candidates: List<String> = emptyList(),
    val messageResId: Int? = null
)

class ScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun setFlashEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isFlashOn = enabled) }
    }

    fun requestCloudCapture(apiKey: String): Boolean {
        if (apiKey.isBlank()) {
            showMessage(ru.pepega.meterapp3.R.string.scanner_api_key_required)
            return false
        }
        _uiState.update {
            it.copy(
                pendingCloudCapture = true,
                messageResId = null
            )
        }
        return true
    }

    fun shouldRunCloudCapture(isFocusLocked: Boolean): Boolean {
        val state = _uiState.value
        return state.pendingCloudCapture && !state.cloudRequestInFlight && isFocusLocked
    }

    fun startCloudRecognition() {
        _uiState.update {
            it.copy(
                isProcessing = true,
                pendingCloudCapture = false,
                cloudRequestInFlight = true
            )
        }
    }

    fun finishCloudRecognition() {
        _uiState.update {
            it.copy(
                isProcessing = false,
                cloudRequestInFlight = false
            )
        }
    }

    fun showCandidateSelection(candidates: List<String>) {
        _uiState.update {
            it.copy(
                candidates = candidates,
                showSelectionDialog = true
            )
        }
    }

    fun dismissCandidateSelection() {
        _uiState.update {
            it.copy(
                showSelectionDialog = false,
                isProcessing = false
            )
        }
    }

    fun showMessage(messageResId: Int) {
        _uiState.update { it.copy(messageResId = messageResId) }
    }

    fun resetUiState() {
        _uiState.value = ScannerUiState()
    }
}
