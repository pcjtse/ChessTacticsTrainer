package com.example.chesstacticstrainer.presentation.board

import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalType

// ── Traditional woodblock-print palette ──────────────────────────────────────
private val BoardBg      = Color(0xFFFFF8F0)   // warm cream paper
private val InkRed       = Color(0xFFCC1A00)   // traditional Chinese vermillion
private val InkRedArgb   = InkRed.toArgb()
private val SelOverlay   = Color(0x44FF6600)   // selection tint
private val LastMoveOvl  = Color(0x33FFD700)   // last-move tint
private val LegalDot     = InkRed
private val RedTeamRing  = Color(0xFFCC1A00)
private val BlueTeamRing = Color(0xFF1A3399)
private val PieceWhite   = Color(0xFFFFF8F0)

// ── Terrain constants ─────────────────────────────────────────────────────────
private val WATER_SQUARES = setOf("b3","b4","b5","c3","c4","c5","e3","e4","e5","f3","f4","f5")
private val BLUE_TRAPS    = setOf("c0","e0","d1")
private val RED_TRAPS     = setOf("c8","e8","d7")
private const val BLUE_DEN = "d0"
private const val RED_DEN  = "d8"

private fun animalChineseName(type: AnimalType) = when (type) {
    AnimalType.ELEPHANT -> "象"
    AnimalType.LION     -> "狮"
    AnimalType.TIGER    -> "虎"
    AnimalType.LEOPARD  -> "豹"
    AnimalType.WOLF     -> "狼"
    AnimalType.DOG      -> "狗"
    AnimalType.CAT      -> "猫"
    AnimalType.MOUSE    -> "鼠"
}

private fun animalRank(type: AnimalType) = when (type) {
    AnimalType.ELEPHANT -> "8"
    AnimalType.LION     -> "7"
    AnimalType.TIGER    -> "6"
    AnimalType.LEOPARD  -> "5"
    AnimalType.WOLF     -> "4"
    AnimalType.DOG      -> "3"
    AnimalType.CAT      -> "2"
    AnimalType.MOUSE    -> "1"
}

@Composable
fun AnimalBoardComponent(
    state: AnimalUiBoardState,
    onSquareTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(7f / 9f)
            .onSizeChanged { canvasSize = it }
            .pointerInput(state, canvasSize) {
                if (canvasSize.width == 0) return@pointerInput
                val cellW = canvasSize.width / 7f
                val cellH = canvasSize.height / 9f
                detectTapGestures { offset ->
                    offsetToAnimalSquare(offset.x, offset.y, cellW, cellH, state.isFlipped)
                        ?.let { onSquareTapped(it) }
                }
            }
    ) {
        val cellW = size.width / 7f
        val cellH = size.height / 9f

        drawTraditionalBoard(cellW, cellH)
        drawCellHighlights(state, cellW, cellH)
        drawLegalMoveDots(state, cellW, cellH)
        drawPieces(state, cellW, cellH)
    }
}

// ── Board ─────────────────────────────────────────────────────────────────────

private fun DrawScope.drawTraditionalBoard(cellW: Float, cellH: Float) {
    val boardW = cellW * 7
    val boardH = cellH * 9

    // ① Cream background
    drawRect(BoardBg, size = Size(boardW, boardH))

    // ② Terrain fills (water cells get wave-pattern fill, others stay cream)
    for (row in 0..8) {
        for (col in 0..6) {
            val sq   = "${'a' + col}$row"
            val left = col * cellW
            val top  = row * cellH
            when {
                WATER_SQUARES.contains(sq)                            -> drawWaterWaves(left, top, cellW, cellH)
                BLUE_TRAPS.contains(sq) || RED_TRAPS.contains(sq)    -> drawTrapEmblem(left, top, cellW, cellH)
                sq == BLUE_DEN || sq == RED_DEN                       -> drawDenEmblem(left, top, cellW, cellH)
            }
        }
    }

    // ③ Grid lines in ink red
    val gridPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color       = InkRedArgb
        style       = android.graphics.Paint.Style.STROKE
        strokeWidth = 1.8f
    }
    drawIntoCanvas { canvas ->
        for (col in 0..7) {
            canvas.nativeCanvas.drawLine(col * cellW, 0f, col * cellW, boardH, gridPaint)
        }
        for (row in 0..9) {
            canvas.nativeCanvas.drawLine(0f, row * cellH, boardW, row * cellH, gridPaint)
        }
    }

    // ④ Thick outer border
    drawRect(
        InkRed, Offset.Zero, Size(boardW, boardH),
        style = Stroke(width = 5f)
    )

    // ⑤ Water lake outer borders (double-line effect)
    val lakeBorderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color       = InkRedArgb
        style       = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f
    }
    drawIntoCanvas { canvas ->
        // Left lake: cols b-c (1-2), rows 3-5
        canvas.nativeCanvas.drawRect(1 * cellW, 3 * cellH, 3 * cellW, 6 * cellH, lakeBorderPaint)
        // Right lake: cols e-f (4-5), rows 3-5
        canvas.nativeCanvas.drawRect(4 * cellW, 3 * cellH, 6 * cellW, 6 * cellH, lakeBorderPaint)
    }
}

// ── Water wave pattern ────────────────────────────────────────────────────────

private fun DrawScope.drawWaterWaves(left: Float, top: Float, cellW: Float, cellH: Float) {
    drawIntoCanvas { canvas ->
        val wavePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color       = InkRedArgb
            style       = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.2f
            strokeCap   = android.graphics.Paint.Cap.ROUND
        }

        val margin    = cellW * 0.07f
        val lineW     = cellW - 2 * margin
        val numLines  = 9
        val spacing   = cellH / (numLines + 1)
        val amplitude = cellH * 0.038f
        val cycles    = 3
        val halfPeriod = lineW / (cycles * 2f)

        for (i in 1..numLines) {
            val y    = top + i * spacing
            val path = Path()
            path.moveTo(left + margin, y)
            for (j in 0 until cycles * 2) {
                val cx = left + margin + j * halfPeriod + halfPeriod * 0.5f
                val cy = y + if (j % 2 == 0) -amplitude else amplitude
                val ex = left + margin + (j + 1) * halfPeriod
                path.quadTo(cx, cy, ex, y)
            }
            canvas.nativeCanvas.drawPath(path, wavePaint)
        }
    }
}

// ── Den emblem: double-frame → circle → 獸穴 ─────────────────────────────────

private fun DrawScope.drawDenEmblem(left: Float, top: Float, cellW: Float, cellH: Float) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color       = InkRedArgb
            style       = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }

        val pad1 = cellW * 0.10f                          // outer frame inset
        val pad2 = cellW * 0.18f                          // circle inset

        // Outer square frame
        canvas.nativeCanvas.drawRect(
            left + pad1, top + pad1,
            left + cellW - pad1, top + cellH - pad1, paint
        )

        // Circle inside frame
        val cx = left + cellW * 0.5f
        val cy = top  + cellH * 0.5f
        val r  = (cellW - 2 * pad2) * 0.5f
        canvas.nativeCanvas.drawCircle(cx, cy, r, paint)

        // 獸穴 text — two characters stacked inside circle
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color     = InkRedArgb
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            textSize  = r * 0.72f
        }
        canvas.nativeCanvas.drawText("獸", cx, cy - r * 0.08f, textPaint)
        canvas.nativeCanvas.drawText("穴", cx, cy + r * 0.82f, textPaint)
    }
}

// ── Trap emblem: double-frame → 陷阱 ─────────────────────────────────────────

private fun DrawScope.drawTrapEmblem(left: Float, top: Float, cellW: Float, cellH: Float) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color       = InkRedArgb
            style       = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.8f
        }

        val pad1 = cellW * 0.10f   // outer frame inset
        val pad2 = cellW * 0.20f   // inner label box inset

        // Outer square frame
        canvas.nativeCanvas.drawRect(
            left + pad1, top + pad1,
            left + cellW - pad1, top + cellH - pad1, paint
        )
        // Inner label rectangle (shorter height, centred)
        val innerTop    = top  + cellH * 0.30f
        val innerBottom = top  + cellH * 0.70f
        canvas.nativeCanvas.drawRect(
            left + pad2, innerTop,
            left + cellW - pad2, innerBottom, paint
        )

        // 陷阱 text centred in inner rect
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color     = InkRedArgb
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            textSize  = (innerBottom - innerTop) * 0.55f
        }
        val cy = (innerTop + innerBottom) * 0.5f + textPaint.textSize * 0.35f
        canvas.nativeCanvas.drawText("陷阱", left + cellW * 0.5f, cy, textPaint)
    }
}

// ── Highlights ────────────────────────────────────────────────────────────────

private fun DrawScope.drawCellHighlights(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    fun highlightCell(sq: String, color: Color) {
        val c   = sq[0] - 'a'
        val r   = sq[1].digitToInt()
        val dc  = if (state.isFlipped) 6 - c else c
        val dr  = if (state.isFlipped) 8 - r else r
        drawRect(color, Offset(dc * cellW, dr * cellH), Size(cellW, cellH))
    }

    state.lastMoveFrom?.let { highlightCell(it, LastMoveOvl) }
    state.lastMoveTo?.let   { highlightCell(it, LastMoveOvl) }
    state.selectedSquare?.let { highlightCell(it, SelOverlay) }
}

private fun DrawScope.drawLegalMoveDots(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    val dotR = minOf(cellW, cellH) * 0.14f
    val ringR = minOf(cellW, cellH) * 0.40f
    for (sq in state.legalTargets) {
        val center = animalSquareToOffset(sq, cellW, cellH, state.isFlipped)
        if (state.pieceMap.containsKey(sq)) {
            // Capture target: red ring
            drawCircle(LegalDot, ringR, center, style = Stroke(width = ringR * 0.18f))
        } else {
            // Empty square: small filled dot
            drawCircle(LegalDot, dotR, center)
        }
    }
}

// ── Pieces ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawPieces(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    val radius      = minOf(cellW, cellH) * 0.42f
    val innerRadius = radius * 0.82f
    val shadowColor = Color(0x30000000)

    for ((square, piece) in state.pieceMap) {
        val center    = animalSquareToOffset(square, cellW, cellH, state.isFlipped)
        val ringColor = if (piece.color == AnimalColor.RED) RedTeamRing else BlueTeamRing
        val ringArgb  = ringColor.toArgb()

        // Drop shadow
        drawCircle(shadowColor, radius, center.copy(y = center.y + radius * 0.10f))
        // Ring (team color)
        drawCircle(ringColor, radius, center)
        // Cream fill
        drawCircle(PieceWhite, innerRadius, center)

        drawIntoCanvas { canvas ->
            // Emoji
            val emojiPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize  = innerRadius * 1.15f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.nativeCanvas.drawText(piece.emoji, center.x, center.y + emojiPaint.textSize * 0.28f, emojiPaint)

            // Rank number badge — at top of ring
            val badgePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color     = ringArgb
                typeface  = Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.CENTER
                textSize  = radius * 0.36f
            }
            canvas.nativeCanvas.drawText(
                animalRank(piece.type),
                center.x,
                center.y - radius * 0.52f,
                badgePaint
            )

            // Chinese name badge — small text at bottom of ring
            val namePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color     = ringArgb
                typeface  = Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.CENTER
                textSize  = radius * 0.34f
            }
            canvas.nativeCanvas.drawText(
                animalChineseName(piece.type),
                center.x,
                center.y + radius * 0.72f,
                namePaint
            )
        }
    }
}
