package com.example.data.repository

import com.example.data.db.VyomDatabase
import com.example.data.db.entity.*
import kotlinx.coroutines.flow.Flow

class VyomRepository(private val database: VyomDatabase) {

    // Conversations
    val activeConversations: Flow<List<ConversationEntity>> =
        database.conversationDao().getActiveConversations()

    val archivedConversations: Flow<List<ConversationEntity>> =
        database.conversationDao().getArchivedConversations()

    suspend fun getConversationById(id: Long): ConversationEntity? =
        database.conversationDao().getConversationById(id)

    suspend fun createConversation(title: String, projectId: Long? = null): Long {
        val entity = ConversationEntity(
            title = title,
            projectId = projectId
        )
        return database.conversationDao().insertConversation(entity)
    }

    suspend fun updateConversation(conversation: ConversationEntity) =
        database.conversationDao().updateConversation(conversation)

    suspend fun setPinned(id: Long, isPinned: Boolean) =
        database.conversationDao().setPinned(id, isPinned)

    suspend fun setArchived(id: Long, isArchived: Boolean) =
        database.conversationDao().setArchived(id, isArchived)

    suspend fun deleteConversation(id: Long) {
        database.messageDao().deleteMessagesForConversation(id)
        database.conversationDao().deleteConversation(id)
    }

    // Messages
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForConversation(conversationId)

    suspend fun insertMessage(message: MessageEntity): Long {
        val messageId = database.messageDao().insertMessage(message)
        // update conversation updated time
        val conv = database.conversationDao().getConversationById(message.conversationId)
        if (conv != null) {
            database.conversationDao().updateConversation(
                conv.copy(updatedAt = System.currentTimeMillis())
            )
        }
        return messageId
    }

    suspend fun updateMessage(message: MessageEntity) =
        database.messageDao().updateMessage(message)

    // Memories
    val allMemories: Flow<List<MemoryEntity>> =
        database.memoryDao().getAllMemories()

    fun searchMemories(query: String): Flow<List<MemoryEntity>> =
        database.memoryDao().searchMemories(query)

    suspend fun insertMemory(key: String, value: String, category: String = "General", isSensitive: Boolean = false): Long {
        return database.memoryDao().insertMemory(
            MemoryEntity(key = key, value = value, category = category, isSensitive = isSensitive)
        )
    }

    suspend fun deleteMemory(memory: MemoryEntity) =
        database.memoryDao().deleteMemory(memory)

    suspend fun deleteMemoryById(id: Long) =
        database.memoryDao().deleteMemoryById(id)

    suspend fun clearMemories() =
        database.memoryDao().clearAllMemories()

    // Routines
    val allRoutines: Flow<List<RoutineEntity>> =
        database.routineDao().getAllRoutines()

    suspend fun insertRoutine(routine: RoutineEntity): Long =
        database.routineDao().insertRoutine(routine)

    suspend fun updateRoutine(routine: RoutineEntity) =
        database.routineDao().updateRoutine(routine)

    suspend fun deleteRoutine(routine: RoutineEntity) =
        database.routineDao().deleteRoutine(routine)

    // Projects
    val allProjects: Flow<List<ProjectEntity>> =
        database.projectDao().getAllProjects()

    suspend fun insertProject(project: ProjectEntity): Long =
        database.projectDao().insertProject(project)

    suspend fun deleteProject(project: ProjectEntity) =
        database.projectDao().deleteProject(project)
}
