package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.ActionIntent
import com.example.data.ai.EngineType
import com.example.data.ai.VyomModelRouter
import com.example.data.ai.network.GeminiApiClient
import com.example.domain.tools.ToolIntentDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies VYOM branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VYOM", appName)
    }

    @Test
    fun `tool intent detector parses natural device commands correctly`() {
        // App open
        val openAction = ToolIntentDetector.detectAction("open youtube")
        assertTrue(openAction is ActionIntent.OpenApp)
        assertEquals("youtube", (openAction as ActionIntent.OpenApp).appName)

        // Alarm
        val alarmAction = ToolIntentDetector.detectAction("set alarm for 7:30 am")
        assertTrue(alarmAction is ActionIntent.SetAlarm)
        assertEquals(7, (alarmAction as ActionIntent.SetAlarm).hour)
        assertEquals(30, alarmAction.minute)

        // Timer
        val timerAction = ToolIntentDetector.detectAction("set timer for 10 minutes")
        assertTrue(timerAction is ActionIntent.SetTimer)
        assertEquals(600, (timerAction as ActionIntent.SetTimer).durationSeconds)

        // Call
        val callAction = ToolIntentDetector.detectAction("call 9876543210")
        assertTrue(callAction is ActionIntent.MakeCall)
        assertEquals("9876543210", (callAction as ActionIntent.MakeCall).phoneNumber)
    }

    @Test
    fun `model router dynamically routes tasks to appropriate engines`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = VyomModelRouter(context, GeminiApiClient())

        // Creative task -> Creative Intelligence
        val imageEngine = router.selectEngine("generate image of deep space galaxy")
        assertEquals(EngineType.CREATE, imageEngine.engineType)

        // Reasoning task -> Reasoning Intelligence
        val reasoningEngine = router.selectEngine("solve equation step by step: 3x + 15 = 45")
        assertEquals(EngineType.REASONING, reasoningEngine.engineType)

        // Vision task when image present -> Vision Intelligence
        val visionEngine = router.selectEngine("What is this?", hasImage = true)
        assertEquals(EngineType.VISION, visionEngine.engineType)

        // Lightweight quick task -> On-Device Intelligence
        val onDeviceEngine = router.selectEngine("summarize: Quick test text.")
        assertEquals(EngineType.ON_DEVICE, onDeviceEngine.engineType)
    }
}

