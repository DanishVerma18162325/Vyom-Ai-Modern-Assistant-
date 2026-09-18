package com.example.data.ai

import android.graphics.Bitmap

interface AIProvider {
    val name: String
    val engineType: EngineType

    suspend fun generateText(
        prompt: String,
        contextHistory: List<Pair<String, String>> = emptyList(), // role to text
        systemInstruction: String? = null
    ): VyomResult<String>

    suspend fun analyzeImage(
        prompt: String,
        bitmap: Bitmap
    ): VyomResult<String>

    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1"
    ): VyomResult<String> // returns URL, base64 or placeholder
}
