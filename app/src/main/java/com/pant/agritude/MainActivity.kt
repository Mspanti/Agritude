package com.pant.agritude



import android.app.Application

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

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import androidx.navigation.NavHostController

import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable

import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController

import androidx.room.Room

import com.pant.agritude.chatscreen.ChatScreen

import com.pant.agritude.ui.theme.AgriTudeTheme

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.flowOf

import kotlinx.coroutines.launch

import java.util.Locale

// Navigation routes for the app.
object AgriTudeDestinations {
    const val CHAT = "chat"
    const val DASHBOARD = "dashboard"
    const val STORIES = "stories"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

// MainActivity is the main entry point of the app.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // API Service initialization
        val apiService = RetrofitClient.apiService

        // Room Database initialization
        val database = Room.databaseBuilder(
            applicationContext,
            AgriTudeDatabase::class.java, "agritude_database"
        ).build()

        // Repository initialization
        val repository = AgriTudeRepository(database.messageDao(), database.userDao(), apiService)

        // API Call test example
        lifecycleScope.launch {
            try {
                val filters = mapOf("filters[state]" to "Tamil Nadu")
                val response = apiService.getMandiPrices(
                    apiKey = "579b464db66ec23bdd000001da78fa78988a42c75a8cf43773001557",
                    filters = filters
                )
                Log.d("API_CALL", "Success: ${response.body()?.records?.size} records found.")
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
                    AgriTudeApp(application, navController, currentLanguage, repository) {
                        currentLanguage = if (currentLanguage == Strings.tamil) Strings.english else Strings.tamil
                    }
                }
            }
        }
    }
}

// A Factory class to handle ViewModel with Application and Repository dependency.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriTudeApp(
    application: Application,
    navController: NavHostController,
    currentLanguage: AppStrings,
    repository: AgriTudeRepository,
    onLanguageToggle: () -> Unit
) {
    val chatViewModel: ChatScreenViewModel = viewModel(
        factory = ChatViewModelFactory(application = application)
    )

    val dashboardViewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = DashboardViewModelFactory(repository))
    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = ProfileViewModelFactory(repository))
    val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

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
        NavHost(navController = navController, startDestination = AgriTudeDestinations.CHAT, modifier = Modifier.padding(innerPadding)) {
            composable(AgriTudeDestinations.CHAT) { ChatScreen(currentLanguage, chatViewModel) }
            composable(AgriTudeDestinations.DASHBOARD) { DashboardScreen(currentLanguage, dashboardViewModel) }
            composable(AgriTudeDestinations.STORIES) { StoriesScreen(currentLanguage) }
            composable(AgriTudeDestinations.PROFILE) { ProfileScreen(currentLanguage, profileViewModel) }
            composable(AgriTudeDestinations.SETTINGS) { SettingsScreen(currentLanguage, settingsViewModel, onLanguageToggle) }
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
        val apiService = RetrofitClient.apiService
        val emptyRepo = AgriTudeRepository(
            messageDao = object : MessageDao {
                override fun getAllMessages(): Flow<List<MessageEntity>> = flowOf(emptyList())
                override suspend fun insert(message: MessageEntity): Long {
                    return 0L
                }
                override suspend fun update(message: MessageEntity) {}
                override suspend fun deleteAll() {}
            },
            userDao = object : UserDao {
                override suspend fun insert(user: UserEntity) {}
                override fun getUser(): Flow<UserEntity?> = flowOf(null)
            },
            apiService = apiService
        )
        val application = android.app.Application()
        AgriTudeApp(application, navController, Strings.tamil, emptyRepo) {}
    }
}
