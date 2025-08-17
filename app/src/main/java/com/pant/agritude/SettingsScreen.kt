package com.pant.agritude

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

@Composable
fun SettingsScreen(strings: AppStrings, viewModel: SettingsViewModel, onLanguageToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = strings.languageSettings, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onLanguageToggle) {
                Text(text = if (strings.languageSettings == "Language") "தமிழ்" else "English")
            }
        }
    }
}

class SettingsViewModel : ViewModel() {
    // No state or logic needed for this simple screen, but ViewModel is included for future expansion.
}
