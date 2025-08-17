package com.pant.agritude

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(strings: AppStrings, viewModel: ProfileViewModel) {
    val userProfile by viewModel.userProfile.collectAsState(initial = UserEntity(
        name = "",
        location = "",
        farmSize = 0.0
    ))
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var location by remember { mutableStateOf(userProfile?.location ?: "") }
    var farmSize by remember { mutableStateOf(userProfile?.farmSize.toString()) }

    LaunchedEffect(userProfile) {
        name = userProfile?.name ?: ""
        location = userProfile?.location ?: ""
        farmSize = userProfile?.farmSize.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.profileTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("பெயர்") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("இடம்") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = farmSize,
            onValueChange = { farmSize = it },
            label = { Text("பண்ணை அளவு (ஏக்கரில்)") },
            modifier = Modifier.fillMaxWidth(),
            // The KeyboardType reference has been corrected
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateProfile(
                    name = name,
                    location = location,
                    farmSize = farmSize.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "சுயவிவரத்தைப் புதுப்பி")
        }
    }
}

// User Profile ViewModel
class ProfileViewModel(private val repository: AgriTudeRepository) : ViewModel() {
    val userProfile = repository.userProfile

    fun updateProfile(name: String, location: String, farmSize: Double) {
        viewModelScope.launch {
            repository.updateProfile(UserEntity(name = name, location = location, farmSize = farmSize))
        }
    }
}

// ViewModel Factory
class ProfileViewModelFactory(private val repository: AgriTudeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
