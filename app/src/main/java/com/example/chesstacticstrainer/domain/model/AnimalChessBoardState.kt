package com.example.chesstacticstrainer.domain.model

data class AnimalChessBoardState(
    val fen: String,
    val pieceMap: Map<String, AnimalPiece>,
    val sideToMove: AnimalColor
)

data class AnimalPiece(val type: AnimalType, val color: AnimalColor)

enum class AnimalType(val rank: Int) {
    ELEPHANT(8), LION(7), TIGER(6), LEOPARD(5),
    WOLF(4), DOG(3), CAT(2), MOUSE(1)
}

enum class AnimalColor {
    RED, BLUE;
    fun opposite(): AnimalColor = if (this == RED) BLUE else RED
}
