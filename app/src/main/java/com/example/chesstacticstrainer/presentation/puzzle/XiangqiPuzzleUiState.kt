package com.example.chesstacticstrainer.presentation.puzzle

import com.example.chesstacticstrainer.domain.model.TacticExplanation
import com.example.chesstacticstrainer.presentation.board.XiangqiUiBoardState

sealed interface XiangqiPuzzleUiState {
    data object Loading : XiangqiPuzzleUiState
    data class Error(val message: String) : XiangqiPuzzleUiState
    data class Active(
        val puzzleId: String,
        val rating: Int,
        val boardState: XiangqiUiBoardState,
        val moveIndex: Int = 0,
        val result: PuzzleResult? = null,
        val hintSquare: String? = null,
        val explanation: TacticExplanation? = null,
        val themes: List<String> = emptyList(),
        val aiExplanation: String? = null,
        val isLoadingAi: Boolean = false,
        val aiAvailable: Boolean = false,
        val showingSolution: Boolean = false,
        val lastMoveWasCorrect: Boolean = false
    ) : XiangqiPuzzleUiState
}
