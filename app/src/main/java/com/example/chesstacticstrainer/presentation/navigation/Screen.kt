package com.example.chesstacticstrainer.presentation.navigation

import com.example.chesstacticstrainer.domain.model.AnimalDifficulty

sealed class Screen(val route: String) {
    data object Home         : Screen("home")
    data object Puzzle       : Screen("puzzle/chess")
    data object XiangqiPuzzle : Screen("puzzle/xiangqi")
    data object AnimalGame   : Screen("game/animal/{difficulty}") {
        fun route(difficulty: AnimalDifficulty) = "game/animal/${difficulty.name}"
    }
    data object GoPuzzle     : Screen("puzzle/go")
    data object Stats        : Screen("stats")
    data object Settings     : Screen("settings")
}
