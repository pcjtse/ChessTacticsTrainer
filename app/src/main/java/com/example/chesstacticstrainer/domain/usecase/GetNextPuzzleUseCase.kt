package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository

class GetNextPuzzleUseCase(
    private val repository: PuzzleRepository,
    private val engine: ChessEngine
) {
    suspend operator fun invoke(): Result<Pair<Puzzle, String>> = runCatching {
        val puzzle = repository.getNextPuzzle().getOrThrow()
        // Per Lichess puzzle format: puzzle.fen is the position BEFORE the opponent's trigger move.
        // solution[0] = opponent's trigger (pre-applied to arrive at the puzzle position).
        // solution[1] = player's first move, solution[2] = computer reply, etc.
        val triggerMove = puzzle.solutionMoves.firstOrNull()
        val startFen = if (triggerMove != null) {
            engine.applyMove(engine.loadFen(puzzle.fen), triggerMove).fen
        } else {
            puzzle.fen
        }
        Pair(puzzle, startFen)
    }

    suspend fun markSolved(puzzleId: String) {
        repository.markPuzzleSolved(puzzleId)
    }
}
