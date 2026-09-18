package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.EngineType
import com.example.data.db.entity.MessageEntity
import com.example.ui.theme.*

@Composable
fun ChatMessageItem(
    message: MessageEntity,
    onReadAloud: (String) -> Unit,
    onRegenerate: () -> Unit = {},
    onFeedback: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            // Engine Badge Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(VyomCosmicIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    VyomOrb(size = 14.dp, state = OrbState.IDLE)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VYOM ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp
                    ),
                    color = VyomCosmicIndigo
                )
                Text(
                    text = " • ${message.engineName.ifBlank { "Cloud Intelligence" }}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = VyomTextTertiary
                )
            }
        }

        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) VyomSurfaceContainer else VyomWhite,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, VyomBorder) else null,
            tonalElevation = if (!isUser) 1.dp else 0.dp,
            shadowElevation = if (!isUser) 0.5.dp else 0.dp,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .testTag(if (isUser) "user_message_bubble" else "assistant_message_bubble")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Render message text with simple markdown styling
                FormattedMessageContent(content = message.content)

                // Generated Art Banner if creative engine
                if (message.content.startsWith("GENERATED_ART:")) {
                    val prompt = message.content.removePrefix("GENERATED_ART:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1B4B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4338CA))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                VyomOrb(size = 54.dp, state = OrbState.CREATING)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Generative Art Synthesis",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VyomWhite
                                    )
                                )
                                Text(
                                    text = "\"$prompt\"",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC7D2FE)),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar for Assistant Messages (Copy, Share, Read Aloud, Regenerate, Thumbs)
        if (!isUser) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp, start = 2.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("VYOM", message.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy text",
                        tint = VyomTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Read Aloud
                IconButton(
                    onClick = { onReadAloud(message.content) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Read aloud",
                        tint = VyomTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Share
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message.content)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share with"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = VyomTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Regenerate
                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Regenerate",
                        tint = VyomTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Thumbs Up / Down feedback
                IconButton(
                    onClick = { onFeedback(true) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (message.isLiked == true) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                        contentDescription = "Good response",
                        tint = if (message.isLiked == true) VyomCelestialBlue else VyomTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { onFeedback(false) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (message.isLiked == false) Icons.Rounded.ThumbDown else Icons.Rounded.ThumbDownOffAlt,
                        contentDescription = "Needs improvement",
                        tint = if (message.isLiked == false) Color(0xFFEF4444) else VyomTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedMessageContent(content: String) {
    val lines = remember(content) { content.split("\n") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = VyomTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.startsWith("## ") || line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("## ").removePrefix("# "),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = VyomTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.startsWith("```") -> {
                    // code block marker
                }
                line.startsWith("• ") || line.startsWith("* ") || line.startsWith("- ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                        Text("• ", color = VyomVioletGlow, fontWeight = FontWeight.Bold)
                        Text(
                            text = line.substring(2).replace("**", ""),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = VyomTextPrimary
                        )
                    }
                }
                else -> {
                    // Regular text
                    Text(
                        text = line.replace("**", ""),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp
                        ),
                        color = VyomTextPrimary
                    )
                }
            }
        }
    }
}
