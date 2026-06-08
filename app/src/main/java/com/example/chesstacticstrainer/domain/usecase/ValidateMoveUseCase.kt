package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.model.BoardState

class ValidateMoveUseCase(private val engine: ChessEngine) {

    data class Result(
        val isCorrect: Boolean,
        val newBoardState: BoardState,
        val computerReply: String?
    )

    operator fun invoke(
        boardState: BoardState,
        uciMove: String,
        solutionMoves: List<String>,
        moveIndex: Int
    ): Result {
        // solution[0] = opponent trigger (pre-applied at load time)
        // Player moves at odd indices:  1, 3, 5...  → moveIndex * 2 + 1
        // Computer replies at even indices > 0: 2, 4, 6... → moveIndex * 2 + 2
        val expectedUserMove = solutionMoves.getOrNull(moveIndex * 2 + 1)
        val isCorrect = expectedUserMove == null || uciMove == expectedUserMove

        val newBoard = engine.applyMove(boardState, uciMove)
        val computerReply = if (isCorrect) solutionMoves.getOrNull(moveIndex * 2 + 2) else null

        return Result(isCorrect, newBoard, computerReply)
    }
}
