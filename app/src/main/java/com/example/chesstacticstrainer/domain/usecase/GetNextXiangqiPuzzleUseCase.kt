package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.domain.engine.XiangqiEngine
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.repository.XiangqiPuzzleRepository

class GetNextXiangqiPuzzleUseCase(
    private val repository: XiangqiPuzzleRepository,
    private val engine: XiangqiEngine
) {
    suspend operator fun invoke(): Result<Pair<Puzzle, String>> = runCatching {
        val puzzle = repository.getNextPuzzle().getOrThrow()
        val board  = engine.loadFen(puzzle.fen)
        val first  = puzzle.solutionMoves.firstOrNull()
        val legal  = if (first != null) engine.isMoveLegal(board, first) else false
        Log.d("CTT-XQ", "Puzzle ${puzzle.id}: fen=${puzzle.fen} moves=${puzzle.solutionMoves} firstMoveLegal=$legal")
        Pair(puzzle, puzzle.fen)
    }

    suspend fun markSolved(puzzleId: String) = repository.markPuzzleSolved(puzzleId)
    suspend fun removeBroken(puzzleId: String) = repository.removePuzzle(puzzleId)
}
