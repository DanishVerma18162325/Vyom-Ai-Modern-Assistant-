package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ComposerBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onCameraClick: () -> Unit,
    onLiveClick: () -> Unit,
    onAttachClick: () -> Unit,
    attachedImage: Bitmap? = null,
    onRemoveImage: () -> Unit = {},
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = VyomWhite,
        shadowElevation = 3.dp,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attached image thumbnail preview
            if (attachedImage != null) {
                Row(
                    modifier = Modifier
                        .padding(start = 6.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Image(
                            bitmap = attachedImage.asImageBitmap(),
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, VyomBorder, RoundedCornerShape(12.dp))
                        )
                        Surface(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .clickable { onRemoveImage() },
                            shape = CircleShape,
                            color = VyomTextPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove image",
                                tint = VyomWhite,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Image attached for VYOM Vision",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = VyomTextSecondary
                    )
                }
            }

            // Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus / Actions button
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("composer_plus_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Actions & Attachments",
                        tint = VyomTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text field
                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = if (isRecording) "Listening to your voice..." else "Ask Vyom anything...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = if (isRecording) VyomCelestialBlue else VyomTextTertiary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("composer_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = VyomCosmicIndigo
                    ),
                    singleLine = false,
                    maxLines = 4
                )

                // Action Controls: [Mic] [Camera] [Voice Live] [Send]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (inputText.isBlank() && attachedImage == null) {
                        // Mic Button
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("composer_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                                contentDescription = "Voice Dictation",
                                tint = if (isRecording) VyomCelestialBlue else VyomTextSecondary,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        // Camera Button
                        IconButton(
                            onClick = onCameraClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("composer_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = "Camera Vision",
                                tint = VyomTextSecondary,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        // Vyom Live Real-Time Voice Button
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .testTag("composer_live_voice_button")
                                .clickable { onLiveClick() },
                            shape = CircleShape,
                            color = VyomSurfaceContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = "VYOM Live Voice Mode",
                                    tint = VyomCosmicIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Send Button
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .testTag("composer_send_button")
                                .clickable { onSend() },
                            shape = CircleShape,
                            color = VyomCosmicIndigo
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = VyomWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
