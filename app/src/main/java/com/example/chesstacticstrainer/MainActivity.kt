package com.example.chesstacticstrainer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chesstacticstrainer.presentation.AppStrings
import com.example.chesstacticstrainer.presentation.LocalStrings
import com.example.chesstacticstrainer.presentation.navigation.AppNavGraph
import com.example.chesstacticstrainer.presentation.navigation.Screen
import com.example.chesstacticstrainer.presentation.theme.ChessTacticsTrainerTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        val openPuzzle = intent?.getBooleanExtra("open_puzzle", false) == true

        setContent {
            ChessTacticsTrainerTheme {
                ChessApp(startOnPuzzle = openPuzzle)
            }
        }
    }
}

@Composable
private fun ChessApp(startOnPuzzle: Boolean = false) {
    val context = LocalContext.current
    val app = context.applicationContext as ChessTacticsApp
    val isEnglish by app.container.languageStore.observeIsEnglish().collectAsState(initial = false)
    val strings = if (isEnglish) AppStrings.ENGLISH else AppStrings.CHINESE
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Triple(Screen.Home, Icons.Filled.Home, strings.navHome),
        Triple(Screen.Stats, Icons.Filled.BarChart, strings.navStats),
        Triple(Screen.Settings, Icons.Filled.Settings, strings.navSettings),
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.first.route }

    CompositionLocalProvider(LocalStrings provides strings) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { (screen, icon, label) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                startOnPuzzle = startOnPuzzle,
                onToggleLanguage = {
                    scope.launch { app.container.languageStore.setIsEnglish(!isEnglish) }
                }
            )
        }
    }
}
