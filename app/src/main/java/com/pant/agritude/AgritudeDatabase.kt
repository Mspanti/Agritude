package com.pant.agritude

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This is the updated database class. It includes the new UserEntity.
@Database(entities = [MessageEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class AgriTudeDatabase : RoomDatabase() {

    // The DAO for the MessageEntity.
    abstract fun messageDao(): MessageDao

    // The new DAO for the UserEntity.
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AgriTudeDatabase? = null

        // This method provides a singleton instance of the database.
        fun getDatabase(context: Context): AgriTudeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgriTudeDatabase::class.java,
                    "agritude_database"
                )
                    // This will clear all data on schema changes, which is what we need to fix the KSP error.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}