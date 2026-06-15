package com.example.chesstacticstrainer.data.mapper

import com.example.chesstacticstrainer.data.remote.dto.PychessPuzzleDto
import com.example.chesstacticstrainer.domain.model.Puzzle

/**
 * Maps a pychess puzzle DTO to our domain Puzzle model.
 *
 * pychess uses ranks 1–10 in move notation (rank 1 = Red's back row = UCCI rank 9).
 * Conversion: UCCI rank = 10 − pychess rank
 * Example: "h8h10" → "h2h0"
 *
 * The FEN in the `f` field already uses UCCI row ordering (top-to-bottom = rank 0 to 9)
 * so no conversion is needed for the FEN itself.
 */
fun PychessPuzzleDto.toDomain(): Puzzle = Puzzle(
    id            = id,
    fen           = fen,
    solutionMoves = moves.split(",").filter { it.isNotBlank() }.map { pychessToUcci(it) },
    themes        = listOf(evalToTheme(eval)),
    rating        = 1200  // pychess doesn't expose puzzle rating; default used for display
)

/** Converts a pychess coordinate move (ranks 1–10) to UCCI (ranks 0–9). */
fun pychessToUcci(move: String): String {
    if (move.isBlank()) return move
    var i = 0
    val fromFile = move.getOrNull(i++) ?: return move
    var fromRankStr = ""
    while (i < move.length && move[i].isDigit()) fromRankStr += move[i++]
    val toFile = move.getOrNull(i++) ?: return move
    var toRankStr = ""
    while (i < move.length && move[i].isDigit()) toRankStr += move[i++]
    val fromRank = 10 - (fromRankStr.toIntOrNull() ?: return move)
    val toRank   = 10 - (toRankStr.toIntOrNull()   ?: return move)
    if (fromRank !in 0..9 || toRank !in 0..9) return move
    return "$fromFile$fromRank$toFile$toRank"
}

private fun evalToTheme(eval: String): String = when {
    eval.startsWith("#1") -> "mateIn1"
    eval.startsWith("#2") -> "mateIn2"
    eval.startsWith("#3") -> "mateIn3"
    eval.startsWith("#4") -> "mateIn4"
    eval.startsWith("#5") -> "mateIn5"
    else -> "tactics"
}
