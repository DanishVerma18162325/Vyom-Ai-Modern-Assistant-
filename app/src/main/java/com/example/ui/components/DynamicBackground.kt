package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import java.util.Calendar

enum class BackgroundMode {
    DEFAULT,
    AI_THINKING,
    VOICE_ACTIVE,
    IMAGE_GENERATION
}

@Composable
fun DynamicBackground(
    mode: BackgroundMode = BackgroundMode.DEFAULT,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    // Subtle atmospheric glow colors based on time of day
    val (primaryGlow, secondaryGlow) = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> Pair(Color(0xFFFFF7ED), Color(0xFFFEF3C7)) // Morning: soft white + subtle warm glow
            in 12..16 -> Pair(Color(0xFFF0F7FF), Color(0xFFE0EDFF)) // Afternoon: clean white + soft blue atmospheric glow
            in 17..20 -> Pair(Color(0xFFF5F3FF), Color(0xFFEDE9FE)) // Evening: white + subtle violet/blue glow
            else -> Pair(Color(0xFFF8FAFC), Color(0xFFF1F5F9))      // Night: white/very-light gray minimal cool gradient
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dynamicBg")

    // Slow organic pulsation
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mode == BackgroundMode.AI_THINKING) 3000 else 8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    val rotateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateAnim"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Base clean pure white canvas
            drawRect(color = Color.White)

            when (mode) {
                BackgroundMode.VOICE_ACTIVE -> {
                    // Soft breathing radial atmospheric field
                    val radius = (canvasWidth * 0.7f) * (0.85f + 0.15f * pulseAnim)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEEF2FF).copy(alpha = 0.8f),
                                Color(0xFFE0E7FF).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f),
                            radius = radius
                        )
                    )
                }

                BackgroundMode.AI_THINKING -> {
                    // Animated subtle luminous light field
                    val offsetDistance = 60f * pulseAnim
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEDE9FE).copy(alpha = 0.75f),
                                Color(0xFFF5F3FF).copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f + offsetDistance, canvasHeight * 0.3f),
                            radius = canvasWidth * 0.65f
                        )
                    )
                }

                BackgroundMode.IMAGE_GENERATION -> {
                    // Soft moving particle field / gentle creative aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDF4FF).copy(alpha = 0.8f),
                                Color(0xFFF0FDF4).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.4f, canvasHeight * 0.5f),
                            radius = canvasWidth * 0.75f
                        )
                    )
                }

                BackgroundMode.DEFAULT -> {
                    // Time-dependent ambient subtle glow near top & bottom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryGlow.copy(alpha = 0.85f),
                                secondaryGlow.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.8f, canvasHeight * 0.15f),
                            radius = canvasWidth * 0.8f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondaryGlow.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.2f, canvasHeight * 0.85f),
                            radius = canvasWidth * 0.6f
                        )
                    )
                }
            }
        }
        content()
    }
}
