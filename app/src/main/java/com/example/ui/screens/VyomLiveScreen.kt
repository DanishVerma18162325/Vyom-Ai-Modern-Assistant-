package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.engines.VoicePersonality
import com.example.domain.voice.VoiceState
import com.example.ui.components.BackgroundMode
import com.example.ui.components.DynamicBackground
import com.example.ui.components.OrbState
import com.example.ui.components.VyomOrb
import com.example.ui.theme.*

@Composable
fun VyomLiveScreen(
    voiceState: VoiceState,
    recognizedText: String,
    assistantSpokenText: String,
    audioRms: Float,
    activePersonality: VoicePersonality,
    onSelectPersonality: (VoicePersonality) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onInterrupt: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState = when (voiceState) {
        VoiceState.IDLE -> OrbState.IDLE
        VoiceState.LISTENING -> OrbState.LISTENING
        VoiceState.THINKING -> OrbState.THINKING
        VoiceState.SPEAKING -> OrbState.SPEAKING
        VoiceState.ERROR -> OrbState.ERROR
    }

    DynamicBackground(
        mode = BackgroundMode.VOICE_ACTIVE,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Exit button & Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onStopListening()
                        onClose()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VyomSurfaceContainer)
                        .testTag("live_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Exit Live Mode",
                        tint = VyomTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VyomWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (voiceState == VoiceState.SPEAKING) Color(0xFF10B981) else VyomCelestialBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VYOM LIVE • ${activePersonality.title}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = VyomTextPrimary
                        )
                    }
                }

                // Interrupt button
                IconButton(
                    onClick = onInterrupt,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VyomSurfaceContainer)
                        .testTag("live_interrupt_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.StopCircle,
                        contentDescription = "Interrupt",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Center: Large Animated Orbital Orb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VyomOrb(
                    size = 180.dp,
                    state = orbState,
                    audioRms = audioRms,
                    onClick = {
                        if (voiceState == VoiceState.LISTENING) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Voice status label
                Text(
                    text = when (voiceState) {
                        VoiceState.IDLE -> "Tap orb or mic to speak"
                        VoiceState.LISTENING -> "Listening to you..."
                        VoiceState.THINKING -> "VYOM is reasoning..."
                        VoiceState.SPEAKING -> "VYOM is speaking..."
                        VoiceState.ERROR -> "Voice standby"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = when (voiceState) {
                        VoiceState.LISTENING -> VyomCelestialBlue
                        VoiceState.SPEAKING -> Color(0xFF0D9488)
                        else -> VyomTextSecondary
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle: Transcription / Spoken Text
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = VyomWhite.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                ) {
                    Text(
                        text = when {
                            voiceState == VoiceState.SPEAKING && assistantSpokenText.isNotBlank() ->
                                assistantSpokenText
                            recognizedText.isNotBlank() ->
                                "\"$recognizedText\""
                            else ->
                                "Speak freely in English, Hindi, or Hinglish."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = VyomTextPrimary,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Bottom: Personality Selector & Mic Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VOICE PERSONALITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = VyomTextTertiary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Personalities row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(VoicePersonality.values()) { personality ->
                        val isSelected = personality == activePersonality
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) VyomCosmicIndigo else VyomWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) VyomCosmicIndigo else VyomBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPersonality(personality) }
                        ) {
                            Text(
                                text = personality.title.removePrefix("VYOM "),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) VyomWhite else VyomTextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Big Mic Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (voiceState == VoiceState.LISTENING) onStopListening() else onStartListening()
                            }
                            .testTag("live_mic_toggle"),
                        shape = CircleShape,
                        color = if (voiceState == VoiceState.LISTENING) VyomCelestialBlue else VyomCosmicIndigo,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (voiceState == VoiceState.LISTENING) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                                contentDescription = "Toggle Mic",
                                tint = VyomWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
