package com.example.chesstacticstrainer.presentation.board

import androidx.compose.ui.geometry.Offset
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoStone

data class GoViewport(
    val minCol: Int,
    val minRow: Int,
    val size: Int        // side length of the square viewport in board cells
) {
    val maxCol: Int get() = minCol + size - 1
    val maxRow: Int get() = minRow + size - 1
}

/**
 * Computes a square viewport that tightly encloses all [stones] (plus any [extraPoints]) with
 * [padding] cells of margin, clamped to the board edge.  Falls back to the full board when empty.
 * Pass first-level solution moves as [extraPoints] to guarantee the hint is always in viewport.
 */
fun computeViewport(
    stones: Map<GoPoint, GoStone>,
    boardSize: Int,
    extraPoints: List<GoPoint> = emptyList(),
    padding: Int = 2
): GoViewport {
    val allPoints = stones.keys + extraPoints
    if (allPoints.isEmpty()) return GoViewport(0, 0, boardSize)

    val rawMinCol = (allPoints.minOf { it.col } - padding).coerceAtLeast(0)
    val rawMaxCol = (allPoints.maxOf { it.col } + padding).coerceAtMost(boardSize - 1)
    val rawMinRow = (allPoints.minOf { it.row } - padding).coerceAtLeast(0)
    val rawMaxRow = (allPoints.maxOf { it.row } + padding).coerceAtMost(boardSize - 1)

    val width  = rawMaxCol - rawMinCol + 1
    val height = rawMaxRow - rawMinRow + 1
    val side   = maxOf(width, height).coerceAtMost(boardSize)

    // Centre the shorter axis
    var minCol = if (width < side) (rawMinCol - (side - width) / 2).coerceAtLeast(0) else rawMinCol
    var minRow = if (height < side) (rawMinRow - (side - height) / 2).coerceAtLeast(0) else rawMinRow

    // Ensure the viewport doesn't spill off the board after centring
    minCol = minCol.coerceAtMost(boardSize - side).coerceAtLeast(0)
    minRow = minRow.coerceAtMost(boardSize - side).coerceAtLeast(0)

    return GoViewport(minCol, minRow, side)
}

data class GoUiBoardState(
    val boardSize: Int,
    val stones: Map<GoPoint, GoStone> = emptyMap(),
    val lastMove: GoPoint? = null,
    val hintPoint: GoPoint? = null,
    val capturedByBlack: Int = 0,
    val capturedByWhite: Int = 0,
    val viewport: GoViewport? = null   // null = show full board
)

fun goPointToOffset(point: GoPoint, cellSize: Float, viewport: GoViewport): Offset =
    Offset(
        (point.col - viewport.minCol) * cellSize + cellSize / 2f,
        (point.row - viewport.minRow) * cellSize + cellSize / 2f
    )

fun offsetToGoPoint(x: Float, y: Float, cellSize: Float, boardSize: Int, viewport: GoViewport): GoPoint? {
    val col = (x / cellSize).toInt() + viewport.minCol
    val row = (y / cellSize).toInt() + viewport.minRow
    if (col !in 0 until boardSize || row !in 0 until boardSize) return null
    return GoPoint(col, row)
}

fun hoshiPoints(boardSize: Int): List<GoPoint> = when (boardSize) {
    9    -> listOf(GoPoint(2,2), GoPoint(6,2), GoPoint(4,4), GoPoint(2,6), GoPoint(6,6))
    13   -> listOf(GoPoint(3,3), GoPoint(9,3), GoPoint(6,6), GoPoint(3,9), GoPoint(9,9))
    19   -> listOf(
        GoPoint(3,3),  GoPoint(9,3),  GoPoint(15,3),
        GoPoint(3,9),  GoPoint(9,9),  GoPoint(15,9),
        GoPoint(3,15), GoPoint(9,15), GoPoint(15,15)
    )
    else -> emptyList()
}
