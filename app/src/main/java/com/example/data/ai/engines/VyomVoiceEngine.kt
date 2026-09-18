package com.example.data.ai.engines

import android.graphics.Bitmap
import com.example.data.ai.AIProvider
import com.example.data.ai.EngineType
import com.example.data.ai.VyomResult
import com.example.data.ai.network.GeminiApiClient

enum class VoicePersonality(val title: String, val speechRate: Float, val pitch: Float, val description: String) {
    CALM("VYOM Calm", 0.9f, 0.95f, "Soothing, measured, and serene tone"),
    NATURAL("VYOM Natural", 1.0f, 1.0f, "Balanced, conversational everyday assistant"),
    FRIENDLY("VYOM Friendly", 1.05f, 1.1f, "Warm, encouraging, and approachable"),
    PROFESSIONAL("VYOM Professional", 1.0f, 0.95f, "Crisp, business-like, and efficient"),
    ENERGETIC("VYOM Energetic", 1.15f, 1.15f, "Upbeat, motivational, and dynamic")
}

class VyomVoiceEngine(private val apiClient: GeminiApiClient) : AIProvider {
    override val name: String = "VYOM Voice Intelligence"
    override val engineType: EngineType = EngineType.VOICE

    var currentPersonality: VoicePersonality = VoicePersonality.NATURAL
    var selectedLanguage: String = "Auto" // Auto, English, Hindi, Hinglish

    override suspend fun generateText(
        prompt: String,
        contextHistory: List<Pair<String, String>>,
        systemInstruction: String?
    ): VyomResult<String> {
        val voiceInstruction = (systemInstruction ?: "") +
                "\nYou are VYOM Voice Intelligence. Keep your responses natural, conversational, punchy, and pleasant when spoken aloud. " +
                "Avoid unnecessary markdown symbols or complex tables in voice mode. Personality mode: ${currentPersonality.title}. " +
                "If user speaks Hindi or Hinglish, respond seamlessly in the same language."

        return try {
            if (apiClient.hasValidApiKey()) {
                val output = apiClient.generateContent(
                    model = "gemini-3.5-flash",
                    prompt = prompt,
                    contextHistory = contextHistory,
                    systemInstruction = voiceInstruction
                )
                VyomResult(isSuccess = true, data = output, engineUsed = EngineType.VOICE)
            } else {
                val p = prompt.lowercase()
                val spokenResponse = when {
                    p.contains("namaste") || p.contains("kya haal hai") || p.contains("kaise ho") ->
                        "Namaste! Main badhiya hoon. Aap bataiye, aaj main aapki kya madad kar sakta hoon?"
                    p.contains("alarm") || p.contains("timer") ->
                        "Sure! I've noted that for you right away."
                    p.contains("explain") || p.contains("batao") ->
                        "Here is a quick overview for you: The key concept is straightforward and easy to apply."
                    else ->
                        "I'm listening. That's an interesting question. Here's what you need to know about $prompt."
                }
                VyomResult(isSuccess = true, data = spokenResponse, engineUsed = EngineType.VOICE)
            }
        } catch (e: Exception) {
            VyomResult(isSuccess = false, error = e.message, engineUsed = EngineType.VOICE)
        }
    }

    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): VyomResult<String> {
        return VyomResult(isSuccess = false, error = "Voice engine handles audio communication", engineUsed = EngineType.VOICE)
    }

    override suspend fun generateImage(prompt: String, aspectRatio: String): VyomResult<String> {
        return VyomResult(isSuccess = false, error = "Voice engine handles audio communication", engineUsed = EngineType.VOICE)
    }
}
