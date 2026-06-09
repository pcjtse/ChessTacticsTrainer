package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository

class GetNextPuzzleUseCase(
    private val repository: PuzzleRepository,
    private val engine: ChessEngine
) {
    suspend operator fun invoke(): Result<Pair<Puzzle, String>> = runCatching {
        val puzzle = repository.getNextPuzzle().getOrThrow()
        // Lichess puzzle format: puzzle.fen IS the puzzle starting position (after opponent's blunder).
        // solution[0] = player's first move, solution[1] = computer reply, etc.
        val startFen = puzzle.fen
        val firstMove = puzzle.solutionMoves.firstOrNull()
        val firstMoveBoard = engine.loadFen(startFen)
        val isLegal = if (firstMove != null) engine.isMoveLegal(firstMoveBoard, firstMove) else false
        val sideToMove = engine.sideToMove(firstMoveBoard)
        Log.d("CTT", "Puzzle ${puzzle.id}: solution=${puzzle.solutionMoves}")
        Log.d("CTT", "  startFen=$startFen")
        Log.d("CTT", "  sideToMove=$sideToMove firstMove=$firstMove legal=$isLegal")
        Pair(puzzle, startFen)
    }

    suspend fun markSolved(puzzleId: String) {
        repository.markPuzzleSolved(puzzleId)
    }

    suspend fun removeBroken(puzzleId: String) {
        repository.removePuzzle(puzzleId)
    }
}
