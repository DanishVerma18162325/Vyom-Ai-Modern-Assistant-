package com.example.domain.tools

import com.example.data.ai.ActionIntent

object ToolIntentDetector {

    fun detectAction(prompt: String): ActionIntent? {
        val p = prompt.trim()
        val lower = p.lowercase()

        // 1. Open App
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start app ")) {
            val appName = lower.removePrefix("open ").removePrefix("launch ").removePrefix("start app ").trim()
            return ActionIntent.OpenApp(appName, null)
        }

        // 2. Make Call
        if (lower.startsWith("call ") || lower.startsWith("phone ") || lower.startsWith("dial ")) {
            val target = p.substringAfter(" ").trim()
            return ActionIntent.MakeCall(phoneNumber = target, contactName = target)
        }

        // 3. Send Message
        if (lower.startsWith("send message to ") || lower.startsWith("message ") || lower.startsWith("text ")) {
            val parts = p.split(Regex("(?i)\\s+(saying|that|with message)\\s+"), limit = 2)
            val to = parts[0].substringAfter("to ").substringAfter("message ").substringAfter("text ").trim()
            val msg = if (parts.size > 1) parts[1].trim() else "Hello from VYOM"
            return ActionIntent.SendMessage(phoneNumber = to, message = msg, contactName = to)
        }

        // 4. Set Alarm
        if (lower.contains("alarm")) {
            val match = Regex("(\\d{1,2})(:(\\d{2}))?\\s*(am|pm)?").find(lower)
            if (match != null) {
                var hour = match.groupValues[1].toIntOrNull() ?: 7
                val minute = match.groupValues[3].toIntOrNull() ?: 0
                val ampm = match.groupValues[4]
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
                val label = p.substringAfter("for", "VYOM Alarm").substringBefore("at").trim()
                return ActionIntent.SetAlarm(hour = hour, minute = minute, label = if (label.isBlank()) "VYOM Alarm" else label)
            }
        }

        // 5. Set Timer
        if (lower.contains("timer")) {
            val match = Regex("(\\d+)\\s*(second|sec|minute|min|hour|hr)").find(lower)
            if (match != null) {
                val num = match.groupValues[1].toIntOrNull() ?: 5
                val unit = match.groupValues[2]
                val seconds = when {
                    unit.startsWith("sec") -> num
                    unit.startsWith("min") -> num * 60
                    unit.startsWith("hour") || unit.startsWith("hr") -> num * 3600
                    else -> num * 60
                }
                return ActionIntent.SetTimer(durationSeconds = seconds, label = "Timer ($num $unit)")
            }
        }

        // 6. Open Maps / Directions
        if (lower.startsWith("navigate to ") || lower.startsWith("find directions to ") || lower.startsWith("show on map ") || lower.startsWith("maps ")) {
            val query = lower.removePrefix("navigate to ").removePrefix("find directions to ").removePrefix("show on map ").removePrefix("maps ").trim()
            return ActionIntent.OpenMaps(query)
        }

        // 7. Open Settings
        if (lower.contains("settings")) {
            val setting = when {
                lower.contains("wifi") -> "wifi"
                lower.contains("bluetooth") -> "bluetooth"
                lower.contains("display") -> "display"
                lower.contains("sound") || lower.contains("volume") -> "sound"
                lower.contains("battery") -> "battery"
                else -> "general"
            }
            return ActionIntent.OpenSettings(setting)
        }

        // 8. Explicit Web Search
        if (lower.startsWith("search web for ") || lower.startsWith("google ") || lower.startsWith("web search ")) {
            val query = lower.removePrefix("search web for ").removePrefix("google ").removePrefix("web search ").trim()
            return ActionIntent.SearchWeb(query)
        }

        return null
    }
}
