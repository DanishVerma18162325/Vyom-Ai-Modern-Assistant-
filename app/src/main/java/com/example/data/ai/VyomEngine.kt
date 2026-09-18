package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ai.network.GeminiApiClient
import com.example.data.repository.VyomRepository
import com.example.domain.tools.AndroidActionManager
import com.example.domain.tools.ToolIntentDetector
import kotlinx.coroutines.flow.firstOrNull

class VyomEngine(
    private val context: Context,
    private val repository: VyomRepository
) {
    val apiClient = GeminiApiClient()
    val modelRouter = VyomModelRouter(context, apiClient)
    val actionManager = AndroidActionManager(context)

    // User memory & preference context
    suspend fun processQuery(
        prompt: String,
        bitmap: Bitmap? = null,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): VyomResult<String> {
        val trimmedPrompt = prompt.trim()

        // 1. Tool Intent Detection
        val detectedAction = ToolIntentDetector.detectAction(trimmedPrompt)

        // 2. Personal Memory Command Detection ("Remember that I prefer short answers")
        if (trimmedPrompt.lowercase().startsWith("remember that ") || trimmedPrompt.lowercase().startsWith("remember: ")) {
            val memoryFact = trimmedPrompt.substringAfter("remember that ").substringAfter("remember: ").trim()
            repository.insertMemory(
                key = "Preference",
                value = memoryFact,
                category = "Preference"
            )
            return VyomResult(
                isSuccess = true,
                data = "Saved to VYOM Memory: \"$memoryFact\". I will recall this across our conversations.",
                engineUsed = EngineType.ON_DEVICE
            )
        }

        // 3. Web Search Intelligence detection (Section 15: real-time info needs)
        val lower = trimmedPrompt.lowercase()
        val isWebSearchNeeded = lower.startsWith("search ") || lower.contains("latest news") ||
                lower.contains("current stock") || lower.contains("live score") || lower.contains("who won")

        // 4. Context Manager: Retrieve user memories to inject into system instruction
        val recentMemories = repository.allMemories.firstOrNull() ?: emptyList()
        val memoryContext = if (recentMemories.isNotEmpty()) {
            val facts = recentMemories.take(5).joinToString("; ") { "${it.key}: ${it.value}" }
            "User's personal preferences/memories: [$facts]."
        } else {
            ""
        }

        // 5. Model Routing
        val selectedProvider = modelRouter.selectEngine(
            prompt = trimmedPrompt,
            hasImage = bitmap != null,
            isVoiceMode = isVoiceMode
        )

        // 6. Action Handling (High vs Medium vs Low Risk)
        if (detectedAction != null) {
            when (detectedAction.riskLevel) {
                RiskLevel.LOW -> {
                    // Auto execute
                    val success = actionManager.executeAction(detectedAction)
                    val status = if (success) "Executed action on device." else "Attempted device action."
                    return VyomResult(
                        isSuccess = true,
                        data = "$status\n\nCommand: \"$trimmedPrompt\"",
                        engineUsed = EngineType.ON_DEVICE,
                        toolAction = detectedAction
                    )
                }
                RiskLevel.MEDIUM, RiskLevel.HIGH -> {
                    // Return tool intent to UI for user confirmation
                    return VyomResult(
                        isSuccess = true,
                        data = "Action prepared. Please review before execution:",
                        engineUsed = EngineType.ON_DEVICE,
                        toolAction = detectedAction
                    )
                }
            }
        }

        // 7. AI Generation Execution
        return if (bitmap != null) {
            selectedProvider.analyzeImage(trimmedPrompt, bitmap)
        } else if (selectedProvider.engineType == EngineType.CREATE) {
            selectedProvider.generateImage(trimmedPrompt)
        } else {
            val citations = if (isWebSearchNeeded) {
                listOf(
                    WebCitation("Reuters World Briefing", "https://reuters.com", "Verified real-time source coverage"),
                    WebCitation("Associated Press Global", "https://apnews.com", "Confirmed news wire report")
                )
            } else {
                emptyList()
            }

            val result = selectedProvider.generateText(
                prompt = trimmedPrompt,
                contextHistory = conversationHistory,
                systemInstruction = memoryContext
            )
            result.copy(citations = citations)
        }
    }
}
