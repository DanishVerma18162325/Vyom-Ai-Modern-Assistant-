package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.*
import com.example.data.db.entity.*

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        RoutineEntity::class,
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VyomDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun routineDao(): RoutineDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: VyomDatabase? = null

        fun getDatabase(context: Context): VyomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VyomDatabase::class.java,
                    "vyom_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
