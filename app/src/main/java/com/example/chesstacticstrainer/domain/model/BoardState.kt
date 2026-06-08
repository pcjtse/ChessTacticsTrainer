package com.example.chesstacticstrainer.domain.model

data class BoardState(
    val fen: String,
    val pieceMap: Map<String, Piece>,
    val isCheck: Boolean
)

data class Piece(val type: PieceType, val color: PieceColor)

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

enum class PieceColor { WHITE, BLACK }
