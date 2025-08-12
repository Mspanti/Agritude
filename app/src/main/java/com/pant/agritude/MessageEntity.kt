package com.pant.agritude

import androidx.room.Entity
import androidx.room.PrimaryKey

// This data class defines a table in the Room database.
// Each instance of this class represents a row in the "messages" table.
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)
