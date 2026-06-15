package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.engine.XiangqiEngine
import com.example.chesstacticstrainer.domain.model.XiangqiBoardState

class ValidateXiangqiMoveUseCase(private val engine: XiangqiEngine) {

    data class Result(
        val isCorrect: Boolean,
        val newBoardState: XiangqiBoardState,
        val computerReply: String?
    )

    operator fun invoke(
        boardState: XiangqiBoardState,
        ucciMove: String,
        solutionMoves: List<String>,
        moveIndex: Int
    ): Result {
        val expected  = solutionMoves.getOrNull(moveIndex * 2)
        val isCorrect = expected == null || ucciMove == expected
        val newBoard  = engine.applyMove(boardState, ucciMove)
        val reply     = if (isCorrect) solutionMoves.getOrNull(moveIndex * 2 + 1) else null
        return Result(isCorrect, newBoard, reply)
    }
}
