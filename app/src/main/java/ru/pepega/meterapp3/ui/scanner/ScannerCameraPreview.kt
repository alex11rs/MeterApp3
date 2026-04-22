package ru.pepega.meterapp3.ui.scanner

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun ScannerCameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    modifier: Modifier = Modifier,
    onImageAnalysisCreated: (ImageAnalysis) -> Unit,
    onCameraBound: (androidx.camera.core.Camera?) -> Unit,
    onAnalyzeFrame: (PreviewView, ImageProxy) -> Unit
) {
    val currentOnImageAnalysisCreated by rememberUpdatedState(onImageAnalysisCreated)
    val currentOnCameraBound by rememberUpdatedState(onCameraBound)
    val currentOnAnalyzeFrame by rememberUpdatedState(onAnalyzeFrame)

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { previewUseCase ->
                        previewUseCase.setSurfaceProvider(surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setResolutionSelector(
                            ResolutionSelector.Builder()
                                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        Size(1920, 1080),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                                    )
                                )
                                .build()
                        )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { imageAnalysis ->
                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                currentOnAnalyzeFrame(this, imageProxy)
                            }
                        }

                    currentOnImageAnalysisCreated(analysis)

                    try {
                        provider.unbindAll()
                        currentOnCameraBound(
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        )
                    } catch (_: Exception) {
                        currentOnCameraBound(null)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier
    )
}
