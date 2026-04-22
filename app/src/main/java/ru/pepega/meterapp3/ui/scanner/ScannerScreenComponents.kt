package ru.pepega.meterapp3.ui.scanner

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import kotlin.math.roundToInt

@Composable
fun ScannerPermissionCard(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.camera_permission_needed),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(R.string.camera_permission_message),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onRequestPermission) {
                    Text(stringResource(R.string.allow))
                }
            }
        }
    }
}

@Composable
fun ScannerViewportOverlay(
    modifier: Modifier = Modifier,
    isFocusLocked: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f)) {
        val rw = size.width * 0.85f
        val rh = 70.dp.toPx()
        val left = (size.width - rw) / 2
        val top = (size.height - rh) / 2
        drawRect(Color.Black.copy(alpha = 0.6f))
        drawRoundRect(
            Color.Transparent,
            Offset(left, top),
            Size(rw, rh),
            CornerRadius(12.dp.toPx()),
            blendMode = BlendMode.Clear
        )
    }

    val alpha by rememberInfiniteTransition(label = "scanner").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scanner_alpha"
    )
    Box(
        modifier
            .fillMaxWidth(0.85f)
            .height(70.dp)
            .border(
                2.dp,
                if (isFocusLocked) Color.Green else Color.White.copy(alpha),
                RoundedCornerShape(12.dp)
            )
    )
}

@Composable
fun ScannerTopBar(
    isFocusLocked: Boolean,
    hasDetectedText: Boolean,
    isFlashOn: Boolean,
    scannerModeLabel: String,
    onBack: () -> Unit,
    onFlashToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
        Text(
            text = if (isFocusLocked && hasDetectedText) {
                "${stringResource(R.string.scanner_title)} $scannerModeLabel LOCK"
            } else {
                "${stringResource(R.string.scanner_title)} $scannerModeLabel"
            },
            color = if (isFocusLocked && hasDetectedText) Color.Green else Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp,
            style = MaterialTheme.typography.labelLarge
        )
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier
                .size(44.dp)
                .background(if (isFlashOn) Color.Yellow else Color.White.copy(0.2f), CircleShape)
        ) {
            Text("Flash", fontSize = 12.sp, color = if (isFlashOn) Color.Black else Color.White)
        }
    }
}

@Composable
fun ScannerMessageBanner(
    modifier: Modifier = Modifier,
    message: String
) {
    Card(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 72.dp, start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5C1D1D).copy(alpha = 0.92f))
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun ScannerZoomControls(
    modifier: Modifier = Modifier,
    zoomRatio: Float,
    minZoomRatio: Float,
    maxZoomRatio: Float,
    onZoomRatioChange: (Float) -> Unit
) {
    if (maxZoomRatio <= minZoomRatio) return

    Card(
        modifier = modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth(0.85f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Zoom ${(zoomRatio * 10).roundToInt() / 10f}x",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = zoomRatio.coerceIn(minZoomRatio, maxZoomRatio),
                onValueChange = onZoomRatioChange,
                valueRange = minZoomRatio..maxZoomRatio
            )
        }
    }
}

@Composable
fun ScannerCloudCaptureButton(
    modifier: Modifier = Modifier,
    isProcessing: Boolean,
    pendingCapture: Boolean,
    onCapture: () -> Unit
) {
    Box(
        modifier
            .padding(bottom = 60.dp)
    ) {
        if (isProcessing) {
            CircularProgressIndicator(Modifier.size(92.dp), Color.Green, 4.dp)
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(4.dp, Color.White, CircleShape)
                .padding(8.dp)
                .clickable(
                    enabled = !isProcessing,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onCapture()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (isProcessing || pendingCapture) Color.Gray else Color.Red,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun ScannerCandidatesDialog(
    showSelectionDialog: Boolean,
    candidates: List<String>,
    onDismiss: () -> Unit,
    onCandidateClick: (String) -> Unit
) {
    if (!showSelectionDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.results_title)) },
        text = {
            Column {
                candidates.forEach { candidate ->
                    Button(
                        onClick = { onCandidateClick(candidate) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(candidate)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
