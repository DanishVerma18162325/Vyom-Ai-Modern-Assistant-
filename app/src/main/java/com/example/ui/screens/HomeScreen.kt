package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ActionIntent
import com.example.data.ai.EngineType
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.MessageEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    messages: List<MessageEntity>,
    conversations: List<ConversationEntity>,
    activeConversationId: Long?,
    isGenerating: Boolean,
    currentEngineType: EngineType,
    manualEngineOverride: EngineType?,
    onSetManualEngine: (EngineType?) -> Unit,
    onSendMessage: (String, Bitmap?) -> Unit,
    onNewConversation: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onOpenLiveVoice: () -> Unit,
    onOpenVisualSearch: () -> Unit,
    onOpenCommandCenter: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenStudy: () -> Unit,
    onReadAloud: (String) -> Unit,
    pendingAction: ActionIntent?,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isEngineMenuOpen by remember { mutableStateOf(false) }
    var isAttachMenuOpen by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Load bitmap
            try {
                val context = listState.layoutInfo.totalItemsCount // trigger
                // We'll let user know attachment ready
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    val backgroundMode = when {
        isGenerating -> BackgroundMode.AI_THINKING
        else -> BackgroundMode.DEFAULT
    }

    DynamicBackground(mode = backgroundMode, modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Top Navigation Bar
                Surface(
                    color = Color.White.copy(alpha = 0.94f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // History Drawer Button
                        IconButton(
                            onClick = { isDrawerOpen = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("home_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Conversations History",
                                tint = VyomTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // VYOM Monogram & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNewConversation() }
                        ) {
                            VyomOrb(size = 28.dp, state = if (isGenerating) OrbState.THINKING else OrbState.IDLE)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "VYOM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 17.sp
                                    ),
                                    color = VyomTextPrimary
                                )
                                Text(
                                    text = "Your Intelligence. Everywhere.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = VyomTextTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Engine Pill (Tappable to switch engine)
                        Box {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isEngineMenuOpen = true }
                                    .testTag("engine_selector_pill"),
                                shape = RoundedCornerShape(14.dp),
                                color = VyomSurfaceContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (currentEngineType == EngineType.ON_DEVICE) Color(0xFF10B981) else VyomCelestialBlue)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = manualEngineOverride?.displayName ?: "Auto",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = VyomTextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        tint = VyomTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Engine Selector Dropdown
                            DropdownMenu(
                                expanded = isEngineMenuOpen,
                                onDismissRequest = { isEngineMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Auto (Model Router)") },
                                    onClick = {
                                        onSetManualEngine(null)
                                        isEngineMenuOpen = false
                                    }
                                )
                                HorizontalDivider()
                                EngineType.values().forEach { engine ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(engine.displayName, fontWeight = FontWeight.SemiBold)
                                                Text(engine.description, fontSize = 11.sp, color = VyomTextTertiary)
                                            }
                                        },
                                        onClick = {
                                            onSetManualEngine(engine)
                                            isEngineMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Command Center Button
                        IconButton(
                            onClick = onOpenCommandCenter,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("home_command_center_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DashboardCustomize,
                                contentDescription = "Command Center",
                                tint = VyomTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                ComposerBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank() || attachedBitmap != null) {
                            onSendMessage(inputText, attachedBitmap)
                            inputText = ""
                            attachedBitmap = null
                        }
                    },
                    onMicClick = {
                        // Dictation or start live voice
                        onOpenLiveVoice()
                    },
                    onCameraClick = {
                        onOpenVisualSearch()
                    },
                    onLiveClick = onOpenLiveVoice,
                    onAttachClick = { isAttachMenuOpen = true },
                    attachedImage = attachedBitmap,
                    onRemoveImage = { attachedBitmap = null },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    // Hero Empty State (Welcome & Suggestions)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(0.15f))

                        // Hero Visual Monogram & Greeting
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VyomOrb(
                                size = 88.dp,
                                state = if (isGenerating) OrbState.THINKING else OrbState.IDLE,
                                onClick = onOpenLiveVoice
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "$greeting.",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = VyomTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "How may VYOM assist you right now?",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp
                                ),
                                color = VyomTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.weight(0.35f))

                        // Quick Capabilities Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onOpenStudy() },
                                shape = RoundedCornerShape(16.dp),
                                color = VyomWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Study Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onOpenMemory() },
                                shape = RoundedCornerShape(16.dp),
                                color = VyomWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Memory", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Horizontal AI Suggestions
                        DynamicSuggestionCards(
                            onSuggestionClick = { prompt ->
                                onSendMessage(prompt, null)
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    // Chat Feed
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                onReadAloud = onReadAloud,
                                onRegenerate = {
                                    // re-trigger last user prompt
                                    val lastUserMsg = messages.lastOrNull { it.role == "user" }
                                    if (lastUserMsg != null) {
                                        onSendMessage(lastUserMsg.content, null)
                                    }
                                }
                            )
                        }

                        // Thinking Indicator
                        if (isGenerating) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VyomOrb(size = 28.dp, state = OrbState.THINKING)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "VYOM is processing...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = VyomTextTertiary,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Plus / Attachment Modal Sheet
                if (isAttachMenuOpen) {
                    ModalBottomSheet(
                        onDismissRequest = { isAttachMenuOpen = false },
                        containerColor = VyomWhite
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .navigationBarsPadding()
                        ) {
                            Text(
                                text = "VYOM Capabilities",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VyomTextPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                CapabilityGridItem(
                                    title = "Study Mode",
                                    icon = Icons.Rounded.School,
                                    tint = Color(0xFF7C3AED),
                                    onClick = {
                                        isAttachMenuOpen = false
                                        onOpenStudy()
                                    }
                                )
                                CapabilityGridItem(
                                    title = "Visual Search",
                                    icon = Icons.Rounded.CenterFocusWeak,
                                    tint = Color(0xFF0284C7),
                                    onClick = {
                                        isAttachMenuOpen = false
                                        onOpenVisualSearch()
                                    }
                                )
                                CapabilityGridItem(
                                    title = "Live Voice",
                                    icon = Icons.Rounded.Bolt,
                                    tint = Color(0xFF059669),
                                    onClick = {
                                        isAttachMenuOpen = false
                                        onOpenLiveVoice()
                                    }
                                )
                                CapabilityGridItem(
                                    title = "Memory",
                                    icon = Icons.Rounded.Psychology,
                                    tint = Color(0xFFEA580C),
                                    onClick = {
                                        isAttachMenuOpen = false
                                        onOpenMemory()
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

                // Action Authorization Confirmation Sheet
                if (pendingAction != null) {
                    ActionConfirmationSheet(
                        action = pendingAction,
                        onConfirm = onConfirmAction,
                        onDismiss = onDismissAction
                    )
                }

                // Drawer (Conversations History)
                if (isDrawerOpen) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { isDrawerOpen = false },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable { isDrawerOpen = false }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(310.dp)
                                    .clickable(enabled = false) {},
                                color = VyomWhite,
                                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .statusBarsPadding()
                                ) {
                                    // New Chat Button
                                    Button(
                                        onClick = {
                                            onNewConversation()
                                            isDrawerOpen = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = VyomCosmicIndigo)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, tint = VyomWhite)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("New Conversation", color = VyomWhite)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Recent Chats",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = VyomTextTertiary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        items(conversations) { conv ->
                                            val isSelected = conv.id == activeConversationId
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSelectConversation(conv.id)
                                                        isDrawerOpen = false
                                                    },
                                                color = if (isSelected) VyomSurfaceContainer else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ChatBubbleOutline,
                                                        contentDescription = null,
                                                        tint = if (isSelected) VyomCosmicIndigo else VyomTextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = conv.title,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                            fontSize = 14.sp
                                                        ),
                                                        color = VyomTextPrimary,
                                                        maxLines = 1,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    IconButton(
                                                        onClick = { onDeleteConversation(conv.id) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Close,
                                                            contentDescription = "Delete",
                                                            tint = VyomTextTertiary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                    // Quick Links at bottom
                                    TextButton(
                                        onClick = {
                                            isDrawerOpen = false
                                            onOpenCommandCenter()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = VyomTextSecondary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Command Center & Settings", color = VyomTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapabilityGridItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.12f),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = VyomTextPrimary)
    }
}
