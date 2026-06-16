package com.example.chesstacticstrainer.presentation.puzzle

import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.presentation.board.AnimalUiBoardState

sealed interface AnimalGameUiState {
    data object Loading : AnimalGameUiState

    data class Playing(
        val boardState: AnimalUiBoardState,
        val playerColor: AnimalColor = AnimalColor.RED,
        val currentTurn: AnimalColor = AnimalColor.RED,
        val isAiThinking: Boolean = false,
        val difficulty: AnimalDifficulty = AnimalDifficulty.MEDIUM,
        val capturedByPlayer: List<String> = emptyList(),
        val capturedByAi: List<String> = emptyList()
    ) : AnimalGameUiState

    data class GameOver(
        val boardState: AnimalUiBoardState,
        val playerWon: Boolean,
        val difficulty: AnimalDifficulty = AnimalDifficulty.MEDIUM
    ) : AnimalGameUiState
}
