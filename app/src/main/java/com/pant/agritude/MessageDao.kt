package com.pant.agritude

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// This interface provides the methods for accessing the database.
// The methods are annotated with Room-specific annotations to define their functionality.
@Dao
interface MessageDao {
    // Inserts a new message into the "messages" table.
    // If a message with the same primary key already exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    // Retrieves all messages from the "messages" table, ordered by id.
    // The result is a Flow, which means any changes to the data will automatically
    // trigger an update to the UI.
    @Query("SELECT * FROM messages ORDER BY id ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>
}
