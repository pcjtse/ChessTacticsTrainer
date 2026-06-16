package com.example.chesstacticstrainer.presentation.board

import androidx.compose.ui.geometry.Offset
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalType
import kotlin.math.roundToInt

data class AnimalUiBoardState(
    val pieceMap: Map<String, AnimalUiPiece> = emptyMap(),
    val selectedSquare: String? = null,
    val legalTargets: List<String> = emptyList(),
    val lastMoveFrom: String? = null,
    val lastMoveTo: String? = null,
    val isFlipped: Boolean = false
)

data class AnimalUiPiece(val type: AnimalType, val color: AnimalColor) {
    val emoji: String get() = when (type) {
        AnimalType.ELEPHANT -> "🐘"
        AnimalType.LION     -> "🦁"
        AnimalType.TIGER    -> "🐯"
        AnimalType.LEOPARD  -> "🐆"
        AnimalType.WOLF     -> "🐺"
        AnimalType.DOG      -> "🐶"
        AnimalType.CAT      -> "🐱"
        AnimalType.MOUSE    -> "🐭"
    }
}

fun animalSquareToOffset(square: String, cellW: Float, cellH: Float, isFlipped: Boolean): Offset {
    val col = square[0] - 'a'          // 0-6
    val row = square[1].digitToInt()   // 0-8
    val displayCol = if (isFlipped) 6 - col else col
    val displayRow = if (isFlipped) 8 - row else row
    return Offset((displayCol + 0.5f) * cellW, (displayRow + 0.5f) * cellH)
}

fun offsetToAnimalSquare(x: Float, y: Float, cellW: Float, cellH: Float, isFlipped: Boolean): String? {
    val col = (x / cellW - 0.5f).roundToInt()
    val row = (y / cellH - 0.5f).roundToInt()
    if (col !in 0..6 || row !in 0..8) return null
    val file = if (isFlipped) 6 - col else col
    val rank = if (isFlipped) 8 - row else row
    return "${'a' + file}$rank"
}
