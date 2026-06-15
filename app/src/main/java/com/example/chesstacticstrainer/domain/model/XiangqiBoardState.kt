package com.example.chesstacticstrainer.domain.model

data class XiangqiBoardState(
    val fen: String,
    val pieceMap: Map<String, XiangqiPiece>,
    val sideToMove: XiangqiColor
)

data class XiangqiPiece(val type: XiangqiPieceType, val color: XiangqiColor)

enum class XiangqiPieceType { GENERAL, ADVISOR, ELEPHANT, HORSE, CHARIOT, CANNON, SOLDIER }

enum class XiangqiColor {
    RED, BLACK;
    fun opposite(): XiangqiColor = if (this == RED) BLACK else RED
}
