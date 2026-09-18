package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    SEARCHING,
    CREATING,
    ERROR
}

@Composable
fun VyomOrb(
    state: OrbState = OrbState.IDLE,
    size: Dp = 80.dp,
    audioRms: Float = 0f,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vyomOrbAnim")

    // Continuous rotation for orbits and thinking state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2400
                    OrbState.SEARCHING -> 1800
                    OrbState.CREATING -> 3000
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Expanding rings for listening
    val ringExpansion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringExpansion"
    )

    val orbModifier = modifier
        .size(size)
        .testTag("vyom_orb")
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(
        modifier = orbModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f
            val dynamicRadius = when (state) {
                OrbState.SPEAKING -> baseRadius * (1f + (audioRms * 0.25f))
                OrbState.LISTENING -> baseRadius * (1f + (ringExpansion * 0.08f))
                else -> baseRadius * breathingPulse
            }

            // Colors based on state
            val (coreColor1, coreColor2, ringColor) = when (state) {
                OrbState.IDLE -> Triple(Color(0xFF2E3A59), Color(0xFF1E2438), Color(0xFF4338CA))
                OrbState.LISTENING -> Triple(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFF60A5FA))
                OrbState.THINKING -> Triple(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF818CF8))
                OrbState.SPEAKING -> Triple(Color(0xFF0D9488), Color(0xFF0F766E), Color(0xFF2DD4BF))
                OrbState.SEARCHING -> Triple(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF38BDF8))
                OrbState.CREATING -> Triple(Color(0xFF9333EA), Color(0xFFC026D3), Color(0xFFE879F9))
                OrbState.ERROR -> Triple(Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFFF87171))
            }

            // 1. Outer Expanding Rings (when Listening or Speaking)
            if (state == OrbState.LISTENING) {
                val outerRadius1 = baseRadius + (baseRadius * 0.5f * ringExpansion)
                val outerAlpha1 = (1f - ringExpansion).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = ringColor.copy(alpha = outerAlpha1),
                    radius = outerRadius1,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                val outerRadius2 = baseRadius + (baseRadius * 0.3f * ((ringExpansion + 0.5f) % 1f))
                val outerAlpha2 = (1f - ((ringExpansion + 0.5f) % 1f)).coerceIn(0f, 1f) * 0.35f
                drawCircle(
                    color = ringColor.copy(alpha = outerAlpha2),
                    radius = outerRadius2,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 2. Speaking Waveform Bars radiating around orb
            if (state == OrbState.SPEAKING) {
                val barCount = 12
                val angleStep = 360f / barCount
                for (i in 0 until barCount) {
                    val angleRad = Math.toRadians((i * angleStep + rotation * 0.3f).toDouble())
                    val barHeight = 8.dp.toPx() + (sin((i + rotation * 0.1f)) * 6.dp.toPx() * (audioRms + 0.5f)).toFloat()
                    val startX = center.x + (dynamicRadius + 4.dp.toPx()) * cos(angleRad).toFloat()
                    val startY = center.y + (dynamicRadius + 4.dp.toPx()) * sin(angleRad).toFloat()
                    val endX = center.x + (dynamicRadius + 4.dp.toPx() + barHeight) * cos(angleRad).toFloat()
                    val endY = center.y + (dynamicRadius + 4.dp.toPx() + barHeight) * sin(angleRad).toFloat()
                    drawLine(
                        color = ringColor.copy(alpha = 0.7f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Orbital Ring Trajectory (Angled Ellipse)
            rotate(rotation, center) {
                drawCircle(
                    color = ringColor.copy(alpha = 0.35f),
                    radius = dynamicRadius * 1.25f,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                // Orbiting satellite bead (celestial node)
                val beadAngleRad = Math.toRadians(0.0)
                val beadX = center.x + (dynamicRadius * 1.25f) * cos(beadAngleRad).toFloat()
                val beadY = center.y + (dynamicRadius * 1.25f) * sin(beadAngleRad).toFloat()
                drawCircle(
                    color = ringColor,
                    radius = 3.dp.toPx(),
                    center = Offset(beadX, beadY)
                )
            }

            // 4. Glowing Core Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor1.copy(alpha = 0.95f),
                        coreColor2.copy(alpha = 1f)
                    ),
                    center = center,
                    radius = dynamicRadius
                ),
                radius = dynamicRadius,
                center = center
            )

            // Subtle inner glow border
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = dynamicRadius - 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 5. Central Original VYOM "V" Monogram
            // Draw clean geometric stylized V: Left wing, vertex point, right wing + upper celestial dot
            val vWidth = dynamicRadius * 0.65f
            val vHeight = dynamicRadius * 0.7f
            val topY = center.y - (vHeight * 0.32f)
            val bottomY = center.y + (vHeight * 0.38f)
            val leftX = center.x - (vWidth * 0.5f)
            val rightX = center.x + (vWidth * 0.5f)

            val vPath = Path().apply {
                moveTo(leftX, topY)
                lineTo(center.x, bottomY)
                lineTo(rightX, topY)
            }

            drawPath(
                path = vPath,
                color = Color.White,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Upper celestial apex dot (symbolizing Akash / infinite space)
            drawCircle(
                color = Color.White,
                radius = 2.2.dp.toPx(),
                center = Offset(center.x, topY - 5.dp.toPx())
            )
        }
    }
}
