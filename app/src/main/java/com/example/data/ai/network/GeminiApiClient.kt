package com.example.data.ai.network

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun hasValidApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key.length > 5
    }

    suspend fun generateContent(
        model: String = "gemini-3.5-flash",
        prompt: String,
        contextHistory: List<Pair<String, String>> = emptyList(),
        systemInstruction: String? = null,
        bitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!hasValidApiKey()) {
            throw IllegalStateException("API Key not configured in Secrets panel")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootJson = JSONObject()

        // System instruction
        if (!systemInstruction.isNullOrBlank()) {
            val sysContent = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemInstruction))
            sysContent.put("parts", sysParts)
            rootJson.put("systemInstruction", sysContent)
        }

        // Contents
        val contentsArray = JSONArray()
        for (item in contextHistory) {
            val role = if (item.first == "user") "user" else "model"
            val contentObj = JSONObject()
            contentObj.put("role", role)
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", item.second))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Current user message
        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val currentParts = JSONArray()
        currentParts.put(JSONObject().put("text", prompt))

        // Multimodal image attachment
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val base64Data = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", base64Data)
            currentParts.put(JSONObject().put("inlineData", inlineData))
        }

        currentContent.put("parts", currentParts)
        contentsArray.put(currentContent)
        rootJson.put("contents", contentsArray)

        // Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                throw RuntimeException(errorMsg)
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val textBuilder = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        textBuilder.append(part.optString("text", ""))
                    }
                    return@withContext textBuilder.toString()
                }
            }
            "VYOM completed the request without text output."
        }
    }

    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!hasValidApiKey()) {
            throw IllegalStateException("API Key not configured in Secrets panel")
        }

        val model = "gemini-2.5-flash-image"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()
        partsArr.put(JSONObject().put("text", prompt))
        contentObj.put("parts", partsArr)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        val modalities = JSONArray()
        modalities.put("TEXT")
        modalities.put("IMAGE")
        genConfig.put("responseModalities", modalities)
        val imgConfig = JSONObject()
        imgConfig.put("aspectRatio", aspectRatio)
        imgConfig.put("imageSize", "1K")
        genConfig.put("imageConfig", imgConfig)
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("Image generation failed: ${response.code}")
            }
            // Parse response for image data
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val mime = inlineData.optString("mimeType", "image/png")
                            val data = inlineData.optString("data")
                            return@withContext "data:$mime;base64,$data"
                        }
                    }
                }
            }
            "GENERATED_ART:$prompt"
        }
    }
}
