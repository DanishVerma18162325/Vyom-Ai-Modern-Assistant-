package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ActionIntent
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionConfirmationSheet(
    action: ActionIntent,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyomWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (action) {
                        is ActionIntent.MakeCall -> Icons.Rounded.Phone
                        is ActionIntent.SendMessage -> Icons.AutoMirrored.Rounded.Send
                        is ActionIntent.SetAlarm -> Icons.Rounded.Alarm
                        is ActionIntent.SetTimer -> Icons.Rounded.Timer
                        is ActionIntent.CreateCalendarEvent -> Icons.Rounded.Event
                        else -> Icons.Rounded.Security
                    },
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Authorization Required",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = VyomTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "VYOM requires your confirmation before triggering this device action:",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = VyomTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Details card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = VyomSurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (action) {
                        is ActionIntent.MakeCall -> {
                            Text("Action: Outgoing Voice Call", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Target: ${action.phoneNumber}", color = VyomTextSecondary, fontSize = 13.sp)
                        }
                        is ActionIntent.SendMessage -> {
                            Text("Action: Send SMS Message", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Recipient: ${action.phoneNumber}", color = VyomTextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Message: \"${action.message}\"", color = VyomTextPrimary, fontSize = 13.5.sp)
                        }
                        is ActionIntent.SetAlarm -> {
                            Text("Action: Set Device Alarm", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Time: ${action.hour}:${action.minute.toString().padStart(2, '0')}", color = VyomTextSecondary, fontSize = 13.sp)
                            Text("Label: ${action.label}", color = VyomTextSecondary, fontSize = 13.sp)
                        }
                        is ActionIntent.SetTimer -> {
                            Text("Action: Start Countdown Timer", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Duration: ${action.durationSeconds} seconds (${action.label})", color = VyomTextSecondary, fontSize = 13.sp)
                        }
                        is ActionIntent.CreateCalendarEvent -> {
                            Text("Action: Schedule Calendar Event", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Title: ${action.title}", color = VyomTextSecondary, fontSize = 13.sp)
                        }
                        else -> {
                            Text("Action: Device Operation", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_cancel_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = VyomTextSecondary)
                }

                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_confirm_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyomCosmicIndigo)
                ) {
                    Text("Confirm & Run", color = VyomWhite)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
