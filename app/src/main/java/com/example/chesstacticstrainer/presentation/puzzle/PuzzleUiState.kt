package com.example.chesstacticstrainer.presentation.puzzle

import com.example.chesstacticstrainer.domain.model.TacticExplanation
import com.example.chesstacticstrainer.presentation.board.UiBoardState

sealed interface PuzzleUiState {
    data object Loading : PuzzleUiState
    data class Error(val message: String) : PuzzleUiState
    data class Active(
        val puzzleId: String,
        val rating: Int,
        val boardState: UiBoardState,
        val moveIndex: Int = 0,
        val result: PuzzleResult? = null,
        val hintSquare: String? = null,
        val explanation: TacticExplanation? = null,
        val themes: List<String> = emptyList(),
        val aiExplanation: String? = null,
        val isLoadingAi: Boolean = false,
        val aiAvailable: Boolean = false,
        val showingSolution: Boolean = false
    ) : PuzzleUiState
}

enum class PuzzleResult { CORRECT, WRONG, COMPLETE }
