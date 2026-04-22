package ru.pepega.meterapp3

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.pepega.meterapp3.scanner.buildPreviewFrameMapping
import ru.pepega.meterapp3.scanner.cloudDigitCount
import ru.pepega.meterapp3.scanner.GoogleCloudTextRecognizer
import ru.pepega.meterapp3.scanner.imageProxyToBitmap
import ru.pepega.meterapp3.scanner.normalizeCloudCandidate
import ru.pepega.meterapp3.scanner.processCloudFrame
import ru.pepega.meterapp3.scanner.processImage
import ru.pepega.meterapp3.scanner.RecognitionResult
import ru.pepega.meterapp3.scanner.rememberScannerSessionState
import ru.pepega.meterapp3.ui.common.showShortUserMessage
import ru.pepega.meterapp3.ui.scanner.ScannerCandidatesDialog
import ru.pepega.meterapp3.ui.scanner.ScannerCameraPreview
import ru.pepega.meterapp3.ui.scanner.ScannerCloudCaptureButton
import ru.pepega.meterapp3.ui.scanner.ScannerMessageBanner
import ru.pepega.meterapp3.ui.scanner.ScannerPermissionCard
import ru.pepega.meterapp3.ui.scanner.ScannerTopBar
import ru.pepega.meterapp3.ui.scanner.ScannerViewportOverlay
import ru.pepega.meterapp3.ui.scanner.ScannerViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

private fun prioritizeCloudCandidatesByExpectedLength(
    candidates: List<String>,
    expectedLength: Int?
): List<String> {
    if (expectedLength == null || expectedLength <= 0 || candidates.isEmpty()) return candidates

    val exactMatches = candidates.filter { cloudDigitCount(it) == expectedLength }
    if (exactMatches.isNotEmpty()) return exactMatches

    val nearbyMatches = candidates.filter { kotlin.math.abs(cloudDigitCount(it) - expectedLength) == 1 }
    if (nearbyMatches.isNotEmpty()) {
        return nearbyMatches.sortedWith(
            compareBy<String> { kotlin.math.abs(cloudDigitCount(it) - expectedLength) }
                .thenBy { cloudDigitCount(it) }
        )
    }

    return candidates.sortedWith(
        compareBy<String> { kotlin.math.abs(cloudDigitCount(it) - expectedLength) }
            .thenBy { cloudDigitCount(it) }
    )
}

private fun isCloudCandidateAccepted(
    candidate: String,
    expectedLength: Int?
): Boolean {
    val digitCount = cloudDigitCount(candidate)
    if (digitCount == 0 || digitCount > 12) return false
    if (expectedLength == null || expectedLength <= 0) return true

    val minAcceptedLength = max(1, expectedLength - 1)
    val maxAcceptedLength = expectedLength + 2
    return digitCount in minAcceptedLength..maxAcceptedLength
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun MeterScannerScreen(
    expectedLength: Int? = null,
    previousReadingValue: Long? = null,
    onBack: () -> Unit,
    onTextRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    val appPreferences = rememberAppPreferences()
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scannerSettings = appPreferences.scannerSettings()
    val scannerApiKey = scannerSettings.apiKey
    val cloudRecognizer = remember(scannerApiKey) { GoogleCloudTextRecognizer(scannerApiKey) }
    val scannerViewModel: ScannerViewModel = viewModel()
    val uiState by scannerViewModel.uiState.collectAsState()
    val sessionState = rememberScannerSessionState(
        context = context,
        cameraProviderFuture = cameraProviderFuture,
        expectedLength = expectedLength,
        onTextRecognized = onTextRecognized
    )

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    DisposableEffect(cloudRecognizer) {
        onDispose {
            scannerViewModel.resetUiState()
            cloudRecognizer.close()
            sessionState.cancelFocusLock()
            sessionState.camera?.cameraControl?.enableTorch(false)
        }
    }

    DisposableEffect(sessionState.camera) {
        onDispose {
            sessionState.cancelFocusLock()
            sessionState.camera?.cameraControl?.enableTorch(false)
        }
    }

    LaunchedEffect(uiState.messageResId) {
        uiState.messageResId?.let { showShortUserMessage(context, context.getString(it)) }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            ScannerCameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraProviderFuture = cameraProviderFuture,
                modifier = Modifier.fillMaxSize(),
                onImageAnalysisCreated = { analysis ->
                    sessionState.imageAnalysis = analysis
                },
                onCameraBound = { boundCamera ->
                    sessionState.camera = boundCamera
                },
                onAnalyzeFrame = { preview, imageProxy ->
                    sessionState.previewView = preview
                    val mediaImage = imageProxy.image
                    val previewMapping = if (preview.width > 0 && preview.height > 0) {
                        buildPreviewFrameMapping(
                            previewView = preview,
                            frameHeightPx = with(density) { 70.dp.toPx() }
                        )
                    } else {
                        null
                    }
                    if (mediaImage == null || previewMapping == null) {
                        imageProxy.close()
                        return@ScannerCameraPreview
                    }
                    if (!sessionState.analyzerGate.compareAndSet(false, true)) {
                        imageProxy.close()
                        return@ScannerCameraPreview
                    }

                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val frameWidth = if (rotation == 90 || rotation == 270) mediaImage.height else mediaImage.width
                    val frameHeight = if (rotation == 90 || rotation == 270) mediaImage.width else mediaImage.height
                    processImage(previewMapping, frameWidth, frameHeight)

                    scope.launch {
                        try {
                            if (uiState.pendingCloudCapture) {
                                sessionState.lockFocusOnRoi(preview, previewMapping)
                            }
                            if (scannerViewModel.shouldRunCloudCapture(sessionState.isFocusLocked)) {
                                val frameBitmap = imageProxyToBitmap(imageProxy)
                                    ?.let { processCloudFrame(it, rotation, previewMapping) }
                                if (frameBitmap != null) {
                                    scannerViewModel.startCloudRecognition()
                                    val cloudResult = cloudRecognizer.recognizeText(frameBitmap)
                                    scannerViewModel.finishCloudRecognition()
                                    when (cloudResult) {
                                        is RecognitionResult.Success -> {
                                            val normalized = prioritizeCloudCandidatesByExpectedLength(
                                                candidates = cloudResult.candidates
                                                    .map(::normalizeCloudCandidate)
                                                    .filter { isCloudCandidateAccepted(it, expectedLength) }
                                                    .distinct(),
                                                expectedLength = expectedLength
                                            )
                                            when {
                                                normalized.size == 1 -> {
                                                    sessionState.handleRecognitionSuccess(normalized.first())
                                                    return@launch
                                                }

                                                normalized.size > 1 -> {
                                                    scannerViewModel.showCandidateSelection(normalized)
                                                }

                                                else -> {
                                                    scannerViewModel.showMessage(R.string.scanner_try_again_or_flash)
                                                }
                                            }
                                        }

                                        is RecognitionResult.Error -> {
                                            scannerViewModel.showMessage(cloudResult.messageResId)
                                        }
                                    }
                                }
                            }
                        } finally {
                            sessionState.analyzerGate.set(false)
                            imageProxy.close()
                        }
                    }
                }
            )
        } else {
            ScannerPermissionCard(
                onRequestPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        ScannerViewportOverlay(
            modifier = Modifier.align(Alignment.Center),
            isFocusLocked = sessionState.isFocusLocked
        )

        ScannerTopBar(
            isFocusLocked = sessionState.isFocusLocked,
            hasDetectedText = sessionState.hasDetectedText,
            isFlashOn = uiState.isFlashOn,
            scannerModeLabel = "CLOUD",
            onBack = {
                scannerViewModel.resetUiState()
                sessionState.releaseScannerResources()
                onBack()
            },
            onFlashToggle = {
                val isFlashOn = !uiState.isFlashOn
                scannerViewModel.setFlashEnabled(isFlashOn)
                sessionState.camera?.cameraControl?.enableTorch(isFlashOn)
            }
        )

        uiState.messageResId?.let { messageResId ->
            ScannerMessageBanner(
                modifier = Modifier.align(Alignment.TopCenter),
                message = context.getString(messageResId)
            )
        }

        ScannerCloudCaptureButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            isProcessing = uiState.isProcessing,
            pendingCapture = uiState.pendingCloudCapture,
            onCapture = {
                scannerViewModel.requestCloudCapture(scannerApiKey)
            }
        )
    }

    ScannerCandidatesDialog(
        showSelectionDialog = uiState.showSelectionDialog,
        candidates = uiState.candidates,
        onDismiss = {
            scannerViewModel.dismissCandidateSelection()
        },
        onCandidateClick = { candidate ->
            scannerViewModel.resetUiState()
            sessionState.releaseScannerResources()
            onTextRecognized(candidate)
        }
    )
}
