package com.example.domain.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast
import com.example.data.ai.ActionIntent
import com.example.data.ai.RiskLevel

class AndroidActionManager(private val context: Context) {

    fun executeAction(action: ActionIntent): Boolean {
        return try {
            when (action) {
                is ActionIntent.OpenApp -> openApp(action.appName, action.packageName)
                is ActionIntent.MakeCall -> dialPhone(action.phoneNumber)
                is ActionIntent.SendMessage -> sendSms(action.phoneNumber, action.message)
                is ActionIntent.SetAlarm -> setAlarm(action.hour, action.minute, action.label)
                is ActionIntent.SetTimer -> setTimer(action.durationSeconds, action.label)
                is ActionIntent.CreateCalendarEvent -> createCalendarEvent(action.title, action.minutesFromNow)
                is ActionIntent.OpenMaps -> openMaps(action.query)
                is ActionIntent.OpenSettings -> openSettings(action.settingType)
                is ActionIntent.SearchWeb -> searchWeb(action.query)
            }
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Action unavailable: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun openApp(appName: String, packageName: String?) {
        val pm = context.packageManager
        val pkg = packageName ?: when (appName.lowercase()) {
            "youtube" -> "com.google.android.youtube"
            "chrome", "browser" -> "com.android.chrome"
            "maps" -> "com.google.android.apps.maps"
            "camera" -> null // handled below
            "settings" -> null // handled below
            "calculator" -> "com.google.android.calculator"
            "gmail", "email" -> "com.google.android.gm"
            "music", "spotify" -> "com.spotify.music"
            else -> null
        }

        if (pkg != null) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        // Fallbacks for standard actions
        when (appName.lowercase()) {
            "camera" -> {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            "settings" -> {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            else -> {
                // Search play store or web
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$appName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    searchWeb("open $appName")
                }
            }
        }
    }

    private fun dialPhone(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.filter { it.isDigit() || it == '+' }}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${phoneNumber.filter { it.isDigit() || it == '+' }}")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun setAlarm(hour: Int, minute: Int, label: String) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun setTimer(durationSeconds: Int, label: String) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun createCalendarEvent(title: String, minutesFromNow: Int) {
        val startTime = System.currentTimeMillis() + (minutesFromNow * 60 * 1000L)
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 60 * 60 * 1000L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openMaps(query: String) {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    private fun openSettings(settingType: String) {
        val action = when (settingType.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "privacy" -> Settings.ACTION_PRIVACY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun searchWeb(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }
}
