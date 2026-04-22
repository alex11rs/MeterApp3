package ru.pepega.meterapp3.scanner

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.RectF
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private val cloudCandidateRegex = Regex("""\d+(?:[.,]\d+)?""")

data class PreviewFrameMapping(
    val viewWidth: Int,
    val viewHeight: Int,
    val frameRect: RectF
)

fun extractCloudCandidates(text: String): List<String> {
    return cloudCandidateRegex.findAll(text)
        .map { it.value }
        .map(::normalizeCloudCandidate)
        .filter { cloudDigitCount(it) in 1..12 }
        .distinct()
        .toList()
}

fun sanitizeCloudCandidate(value: String): String {
    val normalized = value.replace(',', '.')
    val result = StringBuilder()
    var separatorSeen = false

    normalized.forEach { char ->
        when {
            char.isDigit() -> result.append(char)
            char == '.' &&
                !separatorSeen &&
                result.isNotEmpty() &&
                result.last().isDigit() -> {
                result.append('.')
                separatorSeen = true
            }
        }
    }

    return result.toString().trimEnd('.')
}

fun normalizeCloudCandidate(value: String): String {
    val sanitized = sanitizeCloudCandidate(value)
    if (sanitized.isEmpty()) return ""

    val parts = sanitized.split('.', limit = 2)
    val integerPart = parts.firstOrNull().orEmpty().trimStart('0').ifEmpty { "0" }
    val fractionalPart = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty()

    return if (fractionalPart.isNotEmpty()) {
        "$integerPart.$fractionalPart"
    } else {
        integerPart
    }
}

fun cloudDigitCount(value: String): Int = value.count { it.isDigit() }

fun applyMeterDecimalDigits(
    value: String,
    decimalDigits: Int
): String {
    val normalizedDigits = sanitizeCloudCandidate(value).filter { it.isDigit() }
    if (normalizedDigits.isEmpty()) return ""

    val safeDecimalDigits = decimalDigits.coerceIn(0, 3)
    if (safeDecimalDigits == 0) {
        return normalizeCloudCandidate(normalizedDigits)
    }

    val paddedDigits = normalizedDigits.padStart(safeDecimalDigits + 1, '0')
    val splitIndex = paddedDigits.length - safeDecimalDigits
    val integerPart = paddedDigits.substring(0, splitIndex).trimStart('0').ifEmpty { "0" }
    val fractionalPart = paddedDigits.substring(splitIndex)
    return "$integerPart.$fractionalPart"
}

fun buildPreviewFrameMapping(
    previewView: PreviewView,
    frameHeightPx: Float
): PreviewFrameMapping {
    val frameWidth = previewView.width * 0.85f
    val frameLeft = (previewView.width - frameWidth) / 2f
    val frameTop = (previewView.height - frameHeightPx) / 2f
    return PreviewFrameMapping(
        viewWidth = previewView.width,
        viewHeight = previewView.height,
        frameRect = RectF(
            frameLeft,
            frameTop,
            frameLeft + frameWidth,
            frameTop + frameHeightPx
        )
    )
}

fun processImage(
    previewMapping: PreviewFrameMapping,
    frameWidth: Int,
    frameHeight: Int
): RectF = mapPreviewRectToBitmap(previewMapping, frameWidth, frameHeight)

fun processCloudFrame(
    bitmap: Bitmap,
    rotationDegrees: Int,
    previewMapping: PreviewFrameMapping
): Bitmap {
    val rotated = rotateBitmap(bitmap, rotationDegrees)
    val cropRect = processImage(previewMapping, rotated.width, rotated.height)
    return cropBitmap(rotated, cropRect)
}

private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) return bitmap
    val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun cropBitmap(bitmap: Bitmap, cropRect: RectF): Bitmap {
    val left = cropRect.left.roundToInt().coerceIn(0, bitmap.width - 1)
    val top = cropRect.top.roundToInt().coerceIn(0, bitmap.height - 1)
    val right = cropRect.right.roundToInt().coerceIn(left + 1, bitmap.width)
    val bottom = cropRect.bottom.roundToInt().coerceIn(top + 1, bitmap.height)
    return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
}

fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val nv21 = imageProxy.toNv21Bytes() ?: return null
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
    val out = ByteArrayOutputStream()
    if (!yuvImage.compressToJpeg(android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height), 95, out)) {
        return null
    }
    val bytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun ImageProxy.toNv21Bytes(): ByteArray? {
    val yPlane = planes.getOrNull(0) ?: return null
    val uPlane = planes.getOrNull(1) ?: return null
    val vPlane = planes.getOrNull(2) ?: return null

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    val chromaRowStride = uPlane.rowStride
    val chromaPixelStride = uPlane.pixelStride
    val chromaHeight = height / 2
    val chromaWidth = width / 2

    var offset = ySize
    val vBytes = ByteArray(vSize)
    val uBytes = ByteArray(uSize)
    vBuffer.get(vBytes)
    uBuffer.get(uBytes)

    for (row in 0 until chromaHeight) {
        for (col in 0 until chromaWidth) {
            val index = row * chromaRowStride + col * chromaPixelStride
            if (index < vBytes.size && index < uBytes.size && offset + 1 < nv21.size) {
                nv21[offset++] = vBytes[index]
                nv21[offset++] = uBytes[index]
            }
        }
    }
    return nv21
}

private fun mapPreviewRectToBitmap(
    previewMapping: PreviewFrameMapping,
    bitmapWidth: Int,
    bitmapHeight: Int
): RectF {
    val scale = max(
        previewMapping.viewWidth / bitmapWidth.toFloat(),
        previewMapping.viewHeight / bitmapHeight.toFloat()
    )
    val displayedWidth = bitmapWidth * scale
    val displayedHeight = bitmapHeight * scale
    val offsetX = (previewMapping.viewWidth - displayedWidth) / 2f
    val offsetY = (previewMapping.viewHeight - displayedHeight) / 2f

    return RectF(
        ((previewMapping.frameRect.left - offsetX) / scale).coerceIn(0f, bitmapWidth.toFloat()),
        ((previewMapping.frameRect.top - offsetY) / scale).coerceIn(0f, bitmapHeight.toFloat()),
        ((previewMapping.frameRect.right - offsetX) / scale).coerceIn(0f, bitmapWidth.toFloat()),
        ((previewMapping.frameRect.bottom - offsetY) / scale).coerceIn(0f, bitmapHeight.toFloat())
    )
}
