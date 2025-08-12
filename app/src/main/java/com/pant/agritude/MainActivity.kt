// MainActivity.kt
package com.pant.agritude

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.pant.agritude.MessageDao
import com.pant.agritude.MessageEntity
import com.pant.agritude.ui.theme.AgriTudeTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Locale

// Routes for navigation.
object AgriTudeDestinations {
    const val CHAT = "chat"
    const val DASHBOARD = "dashboard"
    const val STORIES = "stories"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

// MainActivity is the main entry point of the app.
class MainActivity : ComponentActivity() {
    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        val apiService = RetrofitClient.apiService

        val database = Room.databaseBuilder(
            applicationContext,
            AgriTudeDatabase::class.java, "agritude_database"
        ).build()

        val repository = AgriTudeRepository(database.messageDao(), apiService)

        // API அழைப்பு சரியாக வேலை செய்கிறதா என்று சோதிக்க ஒரு எடுத்துக்காட்டு.
        lifecycleScope.launch {
            try {
                val filters = mapOf("filters[state]" to "Tamil Nadu")
                // getMandiPrices என மாற்றப்பட்டுள்ளது
                val response = apiService.getMandiPrices(
                    apiKey = "579b464db66ec23bdd000001da78fa78988a42c75a8cf43773001557",
                    filters = filters
                )
                Log.d("API_CALL", "Success: ${response.body()?.records?.size} records found.")
                response.body()?.records?.forEach { record ->
                    Log.d("API_CALL", "Commodity: ${record.commodity}, Price: ${record.price}")
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "Error fetching data: ${e.message}", e)
            }
        }

        setContent {
            val systemLanguage = Locale.getDefault().language
            var currentLanguage by remember {
                mutableStateOf(if (systemLanguage == "ta") Strings.tamil else Strings.english)
            }
            AgriTudeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AgriTudeApp(navController, currentLanguage, repository) {
                        currentLanguage = if (currentLanguage == Strings.tamil) Strings.english else Strings.tamil
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriTudeApp(navController: NavHostController, currentLanguage: AppStrings, repository: AgriTudeRepository, onLanguageToggle: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentLanguage.appName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onLanguageToggle) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Change Language"
                        )
                    }
                    IconButton(onClick = { navController.navigate(AgriTudeDestinations.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = { navController.navigate(AgriTudeDestinations.PROFILE) }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        bottomBar = {
            AgriTudeBottomNavigation(navController, currentLanguage)
        }
    ) { innerPadding ->
        val chatViewModel: ChatScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = ChatViewModelFactory(repository))
        val dashboardViewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = DashboardViewModelFactory(repository))

        NavHost(navController = navController, startDestination = AgriTudeDestinations.CHAT, modifier = Modifier.padding(innerPadding)) {
            composable(AgriTudeDestinations.CHAT) { ChatScreen(currentLanguage, chatViewModel) }
            composable(AgriTudeDestinations.DASHBOARD) { DashboardScreen(currentLanguage, dashboardViewModel) }
            composable(AgriTudeDestinations.STORIES) { StoriesScreen(currentLanguage) }
            composable(AgriTudeDestinations.PROFILE) { ProfileScreen(currentLanguage) }
            composable(AgriTudeDestinations.SETTINGS) { SettingsScreen(currentLanguage, onLanguageToggle) }
        }
    }
}

@Composable
fun AgriTudeBottomNavigation(navController: NavController, strings: AppStrings) {
    val items = listOf(
        Pair(strings.tabChat, Icons.Filled.Chat),
        Pair(strings.tabDashboard, Icons.Filled.Dashboard),
        Pair(strings.tabStories, Icons.Filled.MenuBook)
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, item ->
            val route = when (index) {
                0 -> AgriTudeDestinations.CHAT
                1 -> AgriTudeDestinations.DASHBOARD
                else -> AgriTudeDestinations.STORIES
            }
            NavigationBarItem(
                icon = { Icon(item.second, contentDescription = item.first) },
                label = { Text(item.first) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AgriTudeAppPreview() {
    AgriTudeTheme {
        val navController = rememberNavController()
        val mockApiService = MockAgriTudeApiService()
        val emptyRepo = AgriTudeRepository(
            messageDao = object : MessageDao {
                override fun getAllMessages(): Flow<List<MessageEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override suspend fun insert(message: MessageEntity) {}
            },
            apiService = mockApiService
        )
        AgriTudeApp(navController, Strings.tamil, emptyRepo) {}
    }
}
