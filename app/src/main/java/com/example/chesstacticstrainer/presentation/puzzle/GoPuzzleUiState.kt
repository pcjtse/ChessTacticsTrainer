package com.example.chesstacticstrainer.presentation.puzzle

import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.presentation.board.GoUiBoardState

sealed interface GoPuzzleUiState {
    data object Loading : GoPuzzleUiState
    data class Error(val message: String) : GoPuzzleUiState
    data class Active(
        val puzzleId: String,
        val puzzleName: String,
        val difficulty: Int,
        val boardState: GoUiBoardState,
        val result: PuzzleResult? = null,
        val hintPoint: GoPoint? = null,
        val lastMoveWasCorrect: Boolean = false,
        val showingSolution: Boolean = false,
        val aiAvailable: Boolean = false,
        val isLoadingAi: Boolean = false,
        val aiExplanation: String? = null
    ) : GoPuzzleUiState
}
