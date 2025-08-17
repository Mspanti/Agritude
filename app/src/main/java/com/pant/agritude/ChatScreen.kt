package com.pant.agritude.chatscreen

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.pant.agritude.AppStrings
import com.pant.agritude.ChatScreenViewModel
import java.util.Locale

// ஒரு தனிப்பட்ட மெசேஜுக்கான டேட்டா கிளாஸ்
data class Message(val text: String, val sender: String, val timestamp: Long, val imageUrl: Uri? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentLanguage: AppStrings,
    chatViewModel: ChatScreenViewModel
) {
    // ChatScreenViewModel-இன் uiState-ஐப் பெறுகிறது
    val chatUiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // குரல் உள்ளீட்டிற்கான லாஞ்சர் (Launcher)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        chatViewModel.handleVoiceRecognitionResult(result)
    }

    // ஃபைல் உள்ளீட்டிற்கான லாஞ்சர் (Launcher)
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.addFile(it)
        }
    }

    // மெசேஜ் உள்ளீடு மற்றும் அனுப்பும் பகுதி
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark background
            .padding(16.dp)
    ) {
        // சாட் வரலாறு மற்றும் நீக்கும் பட்டன்கள்
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showHistoryDialog = true }) {
                Icon(
                    Icons.Default.History,
                    contentDescription = currentLanguage.chatHistory,
                    tint = Color.White
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = currentLanguage.deleteHistory,
                    tint = Color.Red
                )
            }
        }

        // மெசேஜ்களைக் காட்ட உதவும் பகுதி
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            reverseLayout = true
        ) {
            items(chatUiState.messages.reversed()) { message ->
                MessageBubble(message)
            }
        }

        // சாட் நிலை (status) உரை
        if (chatUiState.isLLMGenerating) {
            Text(
                currentLanguage.llmGenerating,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
                color = Color.White
            )
        }

        // மெசேஜ் உள்ளீடு மற்றும் அனுப்பும் பகுதி
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // குரல் உள்ளீடு பட்டன்
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                try {
                    speechRecognizerLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Speech recognition failed: ${e.message}")
                }
            }) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = currentLanguage.voiceInput,
                    tint = Color.White
                )
            }

            // ஃபைல் உள்ளீடு பட்டன்
            IconButton(onClick = {
                fileLauncher.launch("image/*")
            }) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = currentLanguage.attachFile,
                    tint = Color.White
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(currentLanguage.askQuestion, color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 4,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.DarkGray,
                    containerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            // மெசேஜ் அனுப்பும் பட்டன்
            IconButton(onClick = {
                if (textInput.isNotBlank()) {
                    chatViewModel.handleUserInput(textInput)
                    textInput = ""
                }
            }) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = currentLanguage.send,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // சாட் வரலாறு டயலாக்
    if (showHistoryDialog) {
        HistoryDialog(
            history = chatUiState.chatHistory,
            onSelect = { selectedChat ->
                chatViewModel.loadChat(selectedChat)
                showHistoryDialog = false
            },
            onDismiss = { showHistoryDialog = false }
        )
    }

    // நீக்கும் உறுதிப்படுத்தல் டயலாக்
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(currentLanguage.deleteChatHistory, color = Color.White) },
            text = { Text(currentLanguage.deleteConfirmation, color = Color.White) },
            confirmButton = {
                Button(onClick = {
                    chatViewModel.deleteChatHistory()
                    showDeleteDialog = false
                }) {
                    Text(currentLanguage.delete)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(currentLanguage.cancel)
                }
            },
            containerColor = Color(0xFF333333)
        )
    }
}

// மெசேஜ் குமிழ்களை (bubbles) வடிவமைக்கும் Composable
@Composable
fun MessageBubble(message: Message) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) Color(0xFF5D5D5D) else Color(0xFF424242) // Darker shades for bubbles
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(12.dp)
        ) {
            Column {
                if (message.text.isNotBlank()) {
                    Text(text = message.text, color = textColor)
                }
                if (message.imageUrl != null) {
                    // இங்கே ஒரு படத்தைக் காட்டுகிறோம்
                    Image(
                        painter = rememberAsyncImagePainter(model = message.imageUrl),
                        contentDescription = "அனுப்பப்பட்ட படம்",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

// சாட் வரலாறு டயலாக்
@Composable
fun HistoryDialog(
    history: List<List<Message>>,
    onSelect: (List<Message>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("சாட் வரலாறு", color = Color.White) },
        text = {
            LazyColumn {
                items(history.withIndex().toList()) { (index, chat) ->
                    TextButton(
                        onClick = { onSelect(chat) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("சாட் ${index + 1}: ${chat.firstOrNull()?.text ?: "வெற்று சாட்"}", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("மூடு", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = Color(0xFF333333)
    )
}
