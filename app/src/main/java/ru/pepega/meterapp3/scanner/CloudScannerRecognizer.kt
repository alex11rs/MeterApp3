package ru.pepega.meterapp3.scanner

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ru.pepega.meterapp3.R
import java.io.ByteArrayOutputStream
import java.io.IOException

class GoogleCloudTextRecognizer(private val apiKey: String) : MeterTextRecognizer {
    private val client = OkHttpClient()

    override suspend fun recognizeText(bitmap: Bitmap): RecognitionResult {
        if (apiKey.isBlank()) return RecognitionResult.Error(R.string.scanner_api_key_required)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        val base64 = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)

        val json = JSONObject().apply {
            put("requests", JSONArray().put(JSONObject().apply {
                put("image", JSONObject().apply { put("content", base64) })
                put("features", JSONArray().put(JSONObject().apply {
                    put("type", "DOCUMENT_TEXT_DETECTION")
                }))
            }))
        }

        val request = Request.Builder()
            .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                parseResponse(body, response.isSuccessful)
            } catch (_: IOException) {
                RecognitionResult.Error(R.string.scanner_error_network)
            } catch (_: Exception) {
                RecognitionResult.Error(R.string.scanner_error_cloud)
            }
        }
    }

    private fun parseResponse(jsonStr: String, isSuccessful: Boolean): RecognitionResult {
        if (!isSuccessful) return mapCloudError(jsonStr)

        return try {
            val root = JSONObject(jsonStr)
            if (root.has("error")) return mapCloudError(jsonStr)

            val candidates = mutableSetOf<String>()
            val resp = root.getJSONArray("responses").getJSONObject(0)

            val fullText = resp.optJSONObject("fullTextAnnotation")?.optString("text")
            if (!fullText.isNullOrBlank()) {
                candidates.addAll(extractCloudCandidates(fullText))
            }

            val annotations = resp.optJSONArray("textAnnotations")
            if (annotations != null && annotations.length() > 0) {
                val text = annotations.getJSONObject(0).getString("description")
                candidates.addAll(extractCloudCandidates(text))
            }

            RecognitionResult.Success(candidates.toList())
        } catch (_: Exception) {
            RecognitionResult.Error(R.string.scanner_error_cloud)
        }
    }

    private fun mapCloudError(jsonStr: String): RecognitionResult {
        return try {
            val status = JSONObject(jsonStr).optJSONObject("error")?.optString("status").orEmpty()
            when (status) {
                "PERMISSION_DENIED", "UNAUTHENTICATED", "INVALID_ARGUMENT" ->
                    RecognitionResult.Error(R.string.scanner_error_api_key)
                else -> RecognitionResult.Error(R.string.scanner_error_cloud)
            }
        } catch (_: Exception) {
            RecognitionResult.Error(R.string.scanner_error_cloud)
        }
    }
}
