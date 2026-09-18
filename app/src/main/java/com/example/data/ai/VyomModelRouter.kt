package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ai.engines.*
import com.example.data.ai.network.GeminiApiClient

class VyomModelRouter(
    context: Context,
    apiClient: GeminiApiClient
) {
    val cloudEngine = VyomCloudEngine(apiClient)
    val onDeviceEngine = VyomOnDeviceEngine(context)
    val visionEngine = VyomVisionEngine(apiClient)
    val voiceEngine = VyomVoiceEngine(apiClient)
    val reasoningEngine = VyomReasoningEngine(apiClient)
    val imageEngine = VyomImageEngine(apiClient)

    var manualEngineOverride: EngineType? = null

    fun selectEngine(prompt: String, hasImage: Boolean = false, isVoiceMode: Boolean = false): AIProvider {
        // Respect manual override if user specified one
        manualEngineOverride?.let { override ->
            return when (override) {
                EngineType.CLOUD -> cloudEngine
                EngineType.ON_DEVICE -> onDeviceEngine
                EngineType.VISION -> visionEngine
                EngineType.VOICE -> voiceEngine
                EngineType.REASONING -> reasoningEngine
                EngineType.CREATE -> imageEngine
            }
        }

        if (hasImage) {
            return visionEngine
        }

        if (isVoiceMode) {
            return voiceEngine
        }

        val p = prompt.lowercase().trim()

        // Image creation intent
        if (p.startsWith("generate image") || p.startsWith("create image") || p.startsWith("draw ") ||
            p.startsWith("paint ") || p.contains("wallpaper") || p.contains("poster")) {
            return imageEngine
        }

        // Deep reasoning intent (math, coding, logic proofs)
        if (p.contains("proof") || p.contains("step by step") || p.contains("solve equation") ||
            p.contains("compare and analyze") || p.contains("derive") || p.contains("algorithm") ||
            p.contains("debug code") || p.contains("why does")) {
            return reasoningEngine
        }

        // Simple lightweight task -> Route to On-Device if supported or appropriate
        if (p.startsWith("summarize:") || p.startsWith("rewrite:") || p.contains("calculate") ||
            p.matches(Regex(".*[0-9]+\\s*[+\\-*/x]\\s*[0-9]+.*")) || p.length < 20) {
            return onDeviceEngine
        }

        // Default to Cloud Engine
        return cloudEngine
    }
}
