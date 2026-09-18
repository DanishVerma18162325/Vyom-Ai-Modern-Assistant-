package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val projectId: Long? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val engineName: String = "VYOM ENGINE",
    val engineType: String = "CLOUD", // CLOUD, ON_DEVICE, VISION, VOICE, REASONING, IMAGE
    val imageUrl: String? = null,
    val toolCallJson: String? = null,
    val citationsJson: String? = null,
    val isLiked: Boolean? = null
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "General", // Preference, Instruction, Workflow, Project, Study
    val isSensitive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerDescription: String,
    val actionDescription: String,
    val actionCommand: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String = "folder",
    val customInstructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
