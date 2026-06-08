package com.example.chesstacticstrainer.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Puzzle : Screen("puzzle")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
}
