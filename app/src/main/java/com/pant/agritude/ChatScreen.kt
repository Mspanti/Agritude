package com.pant.agritude

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pant.agritude.ui.theme.UserBubbleColorDark
import com.pant.agritude.ui.theme.UserBubbleColorLight
import com.pant.agritude.ui.theme.UserBubbleContentColorDark
import com.pant.agritude.ui.theme.UserBubbleContentColorLight

// This Composable now takes the ViewModel as a parameter and observes its data.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(strings: AppStrings, viewModel: ChatScreenViewModel = viewModel()) {
    // Collect the messages from the ViewModel's StateFlow.
    val messages by viewModel.allMessages.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // LazyColumn to display messages.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }
        // Message input section.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(strings.chatInputLabel, color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        viewModel.addMessage(text, true) // Add user message
                        text = "" // Clear input field
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = strings.chatSendButton,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Composable for a message bubble.
@Composable
fun MessageBubble(message: MessageEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            // Display AgriTude's avatar
            // Make sure to add `agritude_avatar.png` to your res/drawable folder.
            Image(
                painter = painterResource(id = R.drawable.agritude_avatar),
                contentDescription = "AgriTude Avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier
                    .widthIn(min = 0.dp, max = 300.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) {
                        if (isSystemInDarkTheme()) UserBubbleColorDark else UserBubbleColorLight
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = if (message.isUser) {
                        if (isSystemInDarkTheme()) UserBubbleContentColorDark else UserBubbleContentColorLight
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Display User's avatar
            // Make sure to add `user_avatar.png` to your res/drawable folder.
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        }
    }
}
