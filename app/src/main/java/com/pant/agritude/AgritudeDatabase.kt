package com.pant.agritude

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This abstract class defines the main database and the list of entities and DAOs.
// It is the entry point for all database operations.
@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AgriTudeDatabase : RoomDatabase() {

    // The DAO for the MessageEntity.
    abstract fun messageDao(): MessageDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
