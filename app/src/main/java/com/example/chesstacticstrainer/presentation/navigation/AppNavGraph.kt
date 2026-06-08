package com.example.chesstacticstrainer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chesstacticstrainer.presentation.home.HomeScreen
import com.example.chesstacticstrainer.presentation.puzzle.PuzzleScreen
import com.example.chesstacticstrainer.presentation.settings.SettingsScreen
import com.example.chesstacticstrainer.presentation.stats.StatsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startOnPuzzle: Boolean = false
) {
    LaunchedEffect(startOnPuzzle) {
        if (startOnPuzzle) navController.navigate(Screen.Puzzle.route)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onStartPuzzle = { navController.navigate(Screen.Puzzle.route) })
        }
        composable(Screen.Puzzle.route) {
            PuzzleScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
