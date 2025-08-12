package com.pant.agritude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This ViewModel handles the UI-related data for the ChatScreen.
class ChatScreenViewModel(private val repository: AgriTudeRepository) : ViewModel() {

    // A MutableStateFlow to hold the list of messages from the database.
    private val _allMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val allMessages: StateFlow<List<MessageEntity>> = _allMessages

    // Initialize the ViewModel.
    init {
        // Collect messages from the repository's Flow and update the StateFlow.
        // This is done within the ViewModel's lifecycle scope.
        viewModelScope.launch {
            repository.allMessages.collect { messages ->
                _allMessages.value = messages
            }
        }
    }

    // A function to add a new message to the database.
    fun addMessage(text: String, isUser: Boolean) {
        viewModelScope.launch {
            // Timestamp logic needs to be implemented here, or in the repository.
            val message = MessageEntity(text = text, isUser = isUser, timestamp = "now")
            repository.insert(message)
        }
    }
}

// A factory class to create instances of ChatScreenViewModel.
class ChatViewModelFactory(private val repository: AgriTudeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
