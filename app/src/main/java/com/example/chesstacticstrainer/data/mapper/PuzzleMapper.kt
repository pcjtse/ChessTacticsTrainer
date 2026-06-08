package com.example.chesstacticstrainer.data.mapper

import com.example.chesstacticstrainer.data.remote.dto.PuzzleResponseDto
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveList

fun PuzzleResponseDto.toDomain(): Puzzle = Puzzle(
    id = puzzle.id,
    fen = reconstructFen(game.pgn, puzzle.initialPly),
    solutionMoves = puzzle.solution,
    themes = puzzle.themes,
    rating = puzzle.rating
)

private fun reconstructFen(pgn: String, targetPly: Int): String {
    return try {
        val cleanedMoves = pgn
            .replace(Regex("\\{[^}]+\\}"), "")
            .replace(Regex("\\([^)]+\\)"), "")
            .replace(Regex("[!?+#]+"), "")
            .replace(Regex("\\d+\\.+\\s*"), " ")
            .replace(Regex("(1-0|0-1|1/2-1/2|\\*)"), "")
            .trim()

        val tokens = cleanedMoves.split(Regex("\\s+")).filter { it.isNotBlank() }
        val sanText = tokens.take(targetPly).joinToString(" ")

        val moveList = MoveList()
        moveList.loadFromSan(sanText)

        val board = Board()
        for (i in 0 until minOf(targetPly, moveList.size)) {
            board.doMove(moveList[i])
        }
        board.fen
    } catch (e: Exception) {
        Board().fen
    }
}
