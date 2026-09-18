package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ActionIntent
import com.example.data.ai.EngineType
import com.example.data.ai.VyomEngine
import com.example.data.db.VyomDatabase
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.MemoryEntity
import com.example.data.db.entity.MessageEntity
import com.example.data.repository.VyomRepository
import com.example.domain.voice.VoiceState
import com.example.domain.voice.VyomVoiceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VyomDatabase.getDatabase(application)
    val repository = VyomRepository(database)
    val vyomEngine = VyomEngine(application, repository)
    val voiceManager = VyomVoiceManager(application)

    val conversations: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = _activeConversationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentEngineType = MutableStateFlow(EngineType.CLOUD)
    val currentEngineType: StateFlow<EngineType> = _currentEngineType.asStateFlow()

    private val _manualEngineOverride = MutableStateFlow<EngineType?>(null)
    val manualEngineOverride: StateFlow<EngineType?> = _manualEngineOverride.asStateFlow()

    private val _pendingAction = MutableStateFlow<ActionIntent?>(null)
    val pendingAction: StateFlow<ActionIntent?> = _pendingAction.asStateFlow()

    private val _assistantSpokenText = MutableStateFlow("")
    val assistantSpokenText: StateFlow<String> = _assistantSpokenText.asStateFlow()

    val voiceState = voiceManager.voiceState
    val recognizedText = voiceManager.recognizedText
    val audioRms = voiceManager.audioRms

    init {
        // Automatically select the most recent conversation if any
        viewModelScope.launch {
            conversations.collect { list ->
                if (_activeConversationId.value == null && list.isNotEmpty()) {
                    _activeConversationId.value = list.first().id
                }
            }
        }
    }

    fun setManualEngine(engine: EngineType?) {
        _manualEngineOverride.value = engine
        vyomEngine.modelRouter.manualEngineOverride = engine
    }

    fun newConversation() {
        _activeConversationId.value = null
    }

    fun selectConversation(id: Long) {
        _activeConversationId.value = id
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = null
            }
        }
    }

    fun sendMessage(prompt: String, bitmap: Bitmap? = null, isVoiceMode: Boolean = false) {
        if (prompt.isBlank() && bitmap == null) return

        viewModelScope.launch {
            // Ensure we have an active conversation
            var convId = _activeConversationId.value
            if (convId == null) {
                val title = if (prompt.isNotBlank()) prompt.take(30) else "Vision Analysis"
                convId = repository.createConversation(title = title)
                _activeConversationId.value = convId
            }

            // Insert user message
            repository.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "user",
                    content = prompt,
                    imageUrl = if (bitmap != null) "ATTACHED_BITMAP" else null
                )
            )

            _isGenerating.value = true

            // Build context history
            val currentMsgs = repository.getMessages(convId).firstOrNull() ?: emptyList()
            val history = currentMsgs.takeLast(6).map { it.role to it.content }

            // Execute via central VyomEngine
            val result = vyomEngine.processQuery(
                prompt = prompt,
                bitmap = bitmap,
                conversationHistory = history,
                isVoiceMode = isVoiceMode
            )

            _isGenerating.value = false
            _currentEngineType.value = result.engineUsed

            val responseContent = result.data ?: (result.error ?: "VYOM completed the task.")

            // Insert assistant response
            repository.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    role = "assistant",
                    content = responseContent,
                    engineName = result.engineUsed.displayName,
                    engineType = result.engineUsed.name
                )
            )

            // If voice mode, speak response
            if (isVoiceMode) {
                _assistantSpokenText.value = responseContent
                voiceManager.speak(responseContent)
            }

            // If action needs confirmation
            if (result.toolAction != null) {
                _pendingAction.value = result.toolAction
            }
        }
    }

    fun confirmPendingAction() {
        val action = _pendingAction.value ?: return
        vyomEngine.actionManager.executeAction(action)
        _pendingAction.value = null
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
    }

    fun addMemory(key: String, value: String, category: String) {
        viewModelScope.launch {
            repository.insertMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val list = conversations.value
            for (c in list) {
                repository.deleteConversation(c.id)
            }
            repository.clearMemories()
            _activeConversationId.value = null
        }
    }

    fun startListening() {
        voiceManager.startListening { spoken ->
            if (spoken.isNotBlank()) {
                sendMessage(spoken, isVoiceMode = true)
            }
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun interruptVoice() {
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
