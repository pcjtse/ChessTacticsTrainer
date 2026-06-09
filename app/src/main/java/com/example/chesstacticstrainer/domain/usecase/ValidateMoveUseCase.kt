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
        // Player moves at even indices: 0, 2, 4... → moveIndex * 2
        // Computer replies at odd indices: 1, 3, 5... → moveIndex * 2 + 1
        val expectedUserMove = solutionMoves.getOrNull(moveIndex * 2)
        val isCorrect = expectedUserMove == null || uciMove == expectedUserMove

        val newBoard = engine.applyMove(boardState, uciMove)
        val computerReply = if (isCorrect) solutionMoves.getOrNull(moveIndex * 2 + 1) else null

        return Result(isCorrect, newBoard, computerReply)
    }
}
