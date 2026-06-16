package com.example.chesstacticstrainer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.presentation.home.HomeScreen
import com.example.chesstacticstrainer.presentation.puzzle.AnimalGameScreen
import com.example.chesstacticstrainer.presentation.puzzle.GoPuzzleScreen
import com.example.chesstacticstrainer.presentation.puzzle.PuzzleScreen
import com.example.chesstacticstrainer.presentation.puzzle.XiangqiPuzzleScreen
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
        navController    = navController,
        startDestination = Screen.Home.route,
        modifier         = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartChess   = { navController.navigate(Screen.Puzzle.route) },
                onStartXiangqi = { navController.navigate(Screen.XiangqiPuzzle.route) },
                onStartAnimal  = { diff -> navController.navigate(Screen.AnimalGame.route(diff)) },
                onStartGo      = { navController.navigate(Screen.GoPuzzle.route) }
            )
        }
        composable(Screen.Puzzle.route) {
            PuzzleScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.XiangqiPuzzle.route) {
            XiangqiPuzzleScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route     = Screen.AnimalGame.route,
            arguments = listOf(navArgument("difficulty") { defaultValue = "MEDIUM" })
        ) { backStackEntry ->
            val diffName   = backStackEntry.arguments?.getString("difficulty") ?: "MEDIUM"
            val difficulty = AnimalDifficulty.entries.find { it.name == diffName } ?: AnimalDifficulty.MEDIUM
            AnimalGameScreen(
                difficulty     = difficulty,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GoPuzzle.route) {
            GoPuzzleScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
