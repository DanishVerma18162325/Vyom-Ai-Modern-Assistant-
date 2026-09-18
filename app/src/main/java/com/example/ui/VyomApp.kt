package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.VyomTheme
import com.example.ui.viewmodel.MainViewModel

enum class VyomScreen {
    HOME,
    LIVE_VOICE,
    VISUAL_SEARCH,
    COMMAND_CENTER,
    MEMORY,
    STUDY
}

@Composable
fun VyomApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(VyomScreen.HOME) }

    val conversations by viewModel.conversations.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentEngineType by viewModel.currentEngineType.collectAsState()
    val manualEngineOverride by viewModel.manualEngineOverride.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()

    val voiceState by viewModel.voiceState.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val assistantSpokenText by viewModel.assistantSpokenText.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen = VyomScreen.LIVE_VOICE
            viewModel.startListening()
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen = VyomScreen.VISUAL_SEARCH
        }
    }

    fun requestAudioAndLaunchLive() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            currentScreen = VyomScreen.LIVE_VOICE
            viewModel.startListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun requestCameraAndLaunchVisualSearch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            currentScreen = VyomScreen.VISUAL_SEARCH
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    VyomTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "vyomScreenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                VyomScreen.HOME -> {
                    HomeScreen(
                        messages = messages,
                        conversations = conversations,
                        activeConversationId = activeConversationId,
                        isGenerating = isGenerating,
                        currentEngineType = currentEngineType,
                        manualEngineOverride = manualEngineOverride,
                        onSetManualEngine = { viewModel.setManualEngine(it) },
                        onSendMessage = { prompt, bitmap ->
                            viewModel.sendMessage(prompt, bitmap)
                        },
                        onNewConversation = { viewModel.newConversation() },
                        onSelectConversation = { viewModel.selectConversation(it) },
                        onDeleteConversation = { viewModel.deleteConversation(it) },
                        onOpenLiveVoice = { requestAudioAndLaunchLive() },
                        onOpenVisualSearch = { requestCameraAndLaunchVisualSearch() },
                        onOpenCommandCenter = { currentScreen = VyomScreen.COMMAND_CENTER },
                        onOpenMemory = { currentScreen = VyomScreen.MEMORY },
                        onOpenStudy = { currentScreen = VyomScreen.STUDY },
                        onReadAloud = { viewModel.speakText(it) },
                        pendingAction = pendingAction,
                        onConfirmAction = { viewModel.confirmPendingAction() },
                        onDismissAction = { viewModel.dismissPendingAction() }
                    )
                }

                VyomScreen.LIVE_VOICE -> {
                    VyomLiveScreen(
                        voiceState = voiceState,
                        recognizedText = recognizedText,
                        assistantSpokenText = assistantSpokenText,
                        audioRms = audioRms,
                        activePersonality = viewModel.voiceManager.activePersonality,
                        onSelectPersonality = { viewModel.voiceManager.activePersonality = it },
                        onStartListening = { viewModel.startListening() },
                        onStopListening = { viewModel.stopListening() },
                        onInterrupt = { viewModel.interruptVoice() },
                        onClose = {
                            viewModel.stopListening()
                            currentScreen = VyomScreen.HOME
                        }
                    )
                }

                VyomScreen.VISUAL_SEARCH -> {
                    VisualSearchScreen(
                        onAnalyzeSelection = { promptPrefix, rect ->
                            currentScreen = VyomScreen.HOME
                            viewModel.sendMessage("$promptPrefix (Visual Screen Region: $rect)")
                        },
                        onClose = { currentScreen = VyomScreen.HOME }
                    )
                }

                VyomScreen.COMMAND_CENTER -> {
                    CommandCenterScreen(
                        currentEngineOverride = manualEngineOverride,
                        onSelectEngineOverride = { viewModel.setManualEngine(it) },
                        onOpenMemory = { currentScreen = VyomScreen.MEMORY },
                        onOpenStudy = { currentScreen = VyomScreen.STUDY },
                        onClearAllData = { viewModel.clearAllData() },
                        onBack = { currentScreen = VyomScreen.HOME }
                    )
                }

                VyomScreen.MEMORY -> {
                    MemoryScreen(
                        memories = memories,
                        onAddMemory = { key, value, cat -> viewModel.addMemory(key, value, cat) },
                        onDeleteMemory = { viewModel.deleteMemory(it) },
                        onClearAll = { viewModel.clearAllData() },
                        onBack = { currentScreen = VyomScreen.HOME }
                    )
                }

                VyomScreen.STUDY -> {
                    StudyScreen(
                        onLaunchStudyPrompt = { prompt ->
                            currentScreen = VyomScreen.HOME
                            viewModel.sendMessage(prompt)
                        },
                        onBack = { currentScreen = VyomScreen.HOME }
                    )
                }
            }
        }
    }
}
