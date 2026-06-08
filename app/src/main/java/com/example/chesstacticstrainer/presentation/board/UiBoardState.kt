package com.example.chesstacticstrainer.presentation.board

import com.example.chesstacticstrainer.domain.model.PieceColor
import com.example.chesstacticstrainer.domain.model.PieceType

data class UiBoardState(
    val pieceMap: Map<String, UiPiece> = emptyMap(),
    val selectedSquare: String? = null,
    val legalTargets: List<String> = emptyList(),
    val lastMoveFrom: String? = null,
    val lastMoveTo: String? = null,
    val checkedKingSquare: String? = null,
    val isFlipped: Boolean = false
)

data class UiPiece(val type: PieceType, val color: PieceColor) {
    val letter: String get() = when (type) {
        PieceType.KING -> "K"
        PieceType.QUEEN -> "Q"
        PieceType.ROOK -> "R"
        PieceType.BISHOP -> "B"
        PieceType.KNIGHT -> "N"
        PieceType.PAWN -> "P"
    }
}

fun squareToColRow(square: String, isFlipped: Boolean): Pair<Int, Int> {
    val file = square[0] - 'a'
    val rank = square[1] - '1'
    return if (isFlipped) {
        Pair(7 - file, rank)
    } else {
        Pair(file, 7 - rank)
    }
}

fun offsetToSquare(x: Float, y: Float, squareSize: Float, isFlipped: Boolean): String? {
    val col = (x / squareSize).toInt()
    val row = (y / squareSize).toInt()
    if (col !in 0..7 || row !in 0..7) return null
    val file = if (isFlipped) 7 - col else col
    val rank = if (isFlipped) row else 7 - row
    return "${'a' + file}${'1' + rank}"
}
