package com.example.chesstacticstrainer.presentation.board

import androidx.compose.ui.geometry.Offset
import com.example.chesstacticstrainer.domain.model.XiangqiColor
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType
import kotlin.math.roundToInt

data class XiangqiUiBoardState(
    val pieceMap: Map<String, XiangqiUiPiece> = emptyMap(),
    val selectedSquare: String? = null,
    val legalTargets: List<String> = emptyList(),
    val lastMoveFrom: String? = null,
    val lastMoveTo: String? = null,
    val checkedGeneralSquare: String? = null,
    val isFlipped: Boolean = false
)

data class XiangqiUiPiece(val type: XiangqiPieceType, val color: XiangqiColor) {
    /** Asset key matching assets/pieces/xiangqi/<key>.svg */
    val svgKey: String get() {
        val c = if (color == XiangqiColor.RED) 'r' else 'b'
        val t = when (type) {
            XiangqiPieceType.GENERAL  -> 'K'
            XiangqiPieceType.ADVISOR  -> 'A'
            XiangqiPieceType.ELEPHANT -> 'E'
            XiangqiPieceType.HORSE    -> 'H'
            XiangqiPieceType.CHARIOT  -> 'R'
            XiangqiPieceType.CANNON   -> 'C'
            XiangqiPieceType.SOLDIER  -> 'P'
        }
        return "$c$t"
    }
}

/**
 * Convert a UCCI square string (e.g. "e9") to a (col, row) pixel-grid position.
 *
 * The Xiangqi board has 9 files (0–8) and 10 ranks (0–9, where 0 is Black's back row).
 * We use a half-cell margin so that:
 *   pixel_x = (col + 0.5) * cellW,  pixel_y = (row + 0.5) * cellH
 * Canvas size is set to aspect 9:10 with cellW = width/9, cellH = height/10.
 */
fun xiangqiSquareToOffset(square: String, cellW: Float, cellH: Float, isFlipped: Boolean): Offset {
    val file = square[0] - 'a'          // 0–8
    val rank = square[1].digitToInt()   // 0–9
    // isFlipped=false: Red at bottom — rank 0 (Black) → row 0 (top), rank 9 (Red) → row 9 (bottom)
    // isFlipped=true:  Black at bottom — rank 9 (Red) → row 0 (top), rank 0 (Black) → row 9 (bottom)
    val col = if (isFlipped) 8 - file else file
    val row = if (isFlipped) 9 - rank else rank
    return Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
}

/** Convert a tap position to the nearest UCCI square, or null if outside the board. */
fun offsetToXiangqiSquare(x: Float, y: Float, cellW: Float, cellH: Float, isFlipped: Boolean): String? {
    val col = (x / cellW - 0.5f).roundToInt()
    val row = (y / cellH - 0.5f).roundToInt()
    if (col !in 0..8 || row !in 0..9) return null
    val file = if (isFlipped) 8 - col else col
    val rank = if (isFlipped) 9 - row else row
    return "${'a' + file}$rank"
}
