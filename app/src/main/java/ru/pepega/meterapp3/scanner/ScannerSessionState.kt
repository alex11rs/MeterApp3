package ru.pepega.meterapp3.scanner

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val FOCUS_LOCK_TIMEOUT_MS = 3_000L

class ScannerSessionState(
    private val context: Context,
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    private val scopeLauncher: ((suspend CoroutineScope.() -> Unit) -> Job),
    private val onTextRecognized: (String) -> Unit,
    val expectedLength: Int? = null
) {
    val analyzerGate = AtomicBoolean(false)

    var imageAnalysis by mutableStateOf<ImageAnalysis?>(null)
    var previewView by mutableStateOf<PreviewView?>(null)
    var camera by mutableStateOf<androidx.camera.core.Camera?>(null)
    var isFocusLocked by mutableStateOf(false)
    var hasDetectedText by mutableStateOf(false)
    var focusResetJob by mutableStateOf<Job?>(null)

    fun cancelFocusLock(resetIndicator: Boolean = true) {
        focusResetJob?.cancel()
        focusResetJob = null
        camera?.cameraControl?.cancelFocusAndMetering()
        if (resetIndicator) {
            isFocusLocked = false
            hasDetectedText = false
        }
    }


    fun vibrateSuccess() {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    fun finishRecognition(value: String) {
        imageAnalysis?.clearAnalyzer()
        analyzerGate.set(false)
        vibrateSuccess()
        cancelFocusLock()
        camera?.cameraControl?.enableTorch(false)
        runCatching { cameraProviderFuture.get().unbindAll() }
        onTextRecognized(value)
    }

    fun lockFocusOnRoi(preview: PreviewView?, previewMapping: PreviewFrameMapping?) {
        val activeCamera = camera ?: return
        val activePreview = preview ?: return
        val mapping = previewMapping ?: return
        if (isFocusLocked) return

        startFocusMetering(
            cameraControl = activeCamera.cameraControl,
            preview = activePreview,
            mapping = mapping,
            autoCancelDurationMs = null
        )
        isFocusLocked = true
        hasDetectedText = true
        focusResetJob?.cancel()
        focusResetJob = scopeLauncher {
            delay(FOCUS_LOCK_TIMEOUT_MS)
            activeCamera.cameraControl.cancelFocusAndMetering()
            isFocusLocked = false
            hasDetectedText = false
            focusResetJob = null
        }
    }

    private fun startFocusMetering(
        cameraControl: CameraControl,
        preview: PreviewView,
        mapping: PreviewFrameMapping,
        autoCancelDurationMs: Long?
    ) {
        val point = preview.meteringPointFactory.createPoint(
            mapping.frameRect.centerX(),
            mapping.frameRect.centerY()
        )
        val builder = FocusMeteringAction.Builder(point)
        val action = if (autoCancelDurationMs == null) {
            builder.disableAutoCancel().build()
        } else {
            builder.setAutoCancelDuration(autoCancelDurationMs, java.util.concurrent.TimeUnit.MILLISECONDS).build()
        }
        cameraControl.startFocusAndMetering(action)
    }

    fun releaseScannerResources() {
        imageAnalysis?.clearAnalyzer()
        analyzerGate.set(false)
        cancelFocusLock()
        camera?.cameraControl?.enableTorch(false)
        runCatching { cameraProviderFuture.get().unbindAll() }
    }

    fun handleRecognitionSuccess(value: String) {
        vibrateSuccess()
        releaseScannerResources()
        onTextRecognized(value)
    }
}

@Composable
fun rememberScannerSessionState(
    context: Context,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    expectedLength: Int? = null,
    onTextRecognized: (String) -> Unit
): ScannerSessionState {
    val scope = rememberCoroutineScope()
    return remember(context, cameraProviderFuture, expectedLength, onTextRecognized) {
        ScannerSessionState(
            context = context,
            cameraProviderFuture = cameraProviderFuture,
            scopeLauncher = { block -> scope.launch(block = block) },
            onTextRecognized = onTextRecognized,
            expectedLength = expectedLength
        )
    }
}
