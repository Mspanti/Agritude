package com.pant.agritude

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// This interface defines the Data Access Object (DAO) for the MessageEntity.
// It contains methods for interacting with the database.
@Dao
interface MessageDao {

    // Retrieves all messages from the database, ordered by timestamp in descending order.
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    // Inserts a new message into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    // Updates an existing message in the database.
    @Update
    suspend fun update(message: MessageEntity)

    // Deletes all messages from the database.
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}
