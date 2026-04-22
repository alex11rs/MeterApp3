package ru.pepega.meterapp3.scanner

import android.graphics.Bitmap

sealed interface RecognitionResult {
    data class Success(val candidates: List<String>) : RecognitionResult
    data class Error(val messageResId: Int) : RecognitionResult
}

interface MeterTextRecognizer {
    suspend fun recognizeText(bitmap: Bitmap): RecognitionResult
    fun close() = Unit
}
