package com.example.data.ai

enum class EngineType(val displayName: String, val description: String) {
    CLOUD("Cloud Intelligence", "High-capacity deep understanding and general queries"),
    ON_DEVICE("On-Device Intelligence", "Ultra-fast, private, on-device processing"),
    VISION("Vision Intelligence", "Multimodal visual perception and camera understanding"),
    VOICE("Voice Intelligence", "Streaming acoustic interaction and natural speech"),
    REASONING("Reasoning Intelligence", "Deep step-by-step logic, math, and code synthesis"),
    CREATE("Creative Intelligence", "Generative visual art, posters, and design synthesis")
}

data class VyomResult<out T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val error: String? = null,
    val engineUsed: EngineType = EngineType.CLOUD,
    val citations: List<WebCitation> = emptyList(),
    val toolAction: ActionIntent? = null
)

data class WebCitation(
    val title: String,
    val url: String,
    val snippet: String = ""
)

sealed class ActionIntent(val riskLevel: RiskLevel) {
    data class OpenApp(val appName: String, val packageName: String?) : ActionIntent(RiskLevel.LOW)
    data class MakeCall(val phoneNumber: String, val contactName: String?) : ActionIntent(RiskLevel.HIGH)
    data class SendMessage(val phoneNumber: String, val message: String, val contactName: String?) : ActionIntent(RiskLevel.HIGH)
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : ActionIntent(RiskLevel.MEDIUM)
    data class SetTimer(val durationSeconds: Int, val label: String) : ActionIntent(RiskLevel.MEDIUM)
    data class CreateCalendarEvent(val title: String, val minutesFromNow: Int) : ActionIntent(RiskLevel.MEDIUM)
    data class OpenMaps(val query: String) : ActionIntent(RiskLevel.LOW)
    data class OpenSettings(val settingType: String) : ActionIntent(RiskLevel.LOW)
    data class SearchWeb(val query: String) : ActionIntent(RiskLevel.LOW)
}

enum class RiskLevel {
    LOW,    // Auto-execute with feedback
    MEDIUM, // Execute with confirmation card / undo
    HIGH    // Require preview & explicit user authorization
}
