package com.example.chesstacticstrainer.presentation.board

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

// ── Palette ──────────────────────────────────────────────────────────────────
private val LandColor     = Color(0xFFF0E0B0)   // warm parchment
private val WaterColor    = Color(0xFF1A5F9C)   // deep river blue
private val WaterBorder   = Color(0xFF0D3F6B)   // darker water border
private val WaterSheen    = Color(0xFF2E7FC4)   // lighter sheen on water
private val TrapColor     = Color(0xFF5D3A1A)   // dark wood brown
private val DenColor      = Color(0xFFFFD700)   // bright gold
private val DenBorder     = Color(0xFFC8A000)   // gold border
private val BoardBorder   = Color(0xFF2E5E1A)   // dark jungle green
private val GridLine      = Color(0x99362E0A)   // warm dark grid
private val HighlightLM   = Color(0xBBF9A825)   // last move amber
private val HighlightSel  = Color(0xDDFFD54F)   // selection bright yellow
private val LegalDotColor = Color(0xCC43A047)   // legal move green
private val RedTeam       = Color(0xFFD32F2F)
private val BlueTeam      = Color(0xFF1565C0)

// ── Terrain ──────────────────────────────────────────────────────────────────
private val WATER_SQUARES = setOf("b3","b4","b5","c3","c4","c5","e3","e4","e5","f3","f4","f5")
private val BLUE_TRAPS    = setOf("c0","e0","d1")
private val RED_TRAPS     = setOf("c8","e8","d7")
private const val BLUE_DEN = "d0"
private const val RED_DEN  = "d8"

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
                    val sq = offsetToAnimalSquare(offset.x, offset.y, cellW, cellH, state.isFlipped)
                    if (sq != null) onSquareTapped(sq)
                }
            }
    ) {
        val cellW = size.width / 7f
        val cellH = size.height / 9f

        drawRealBoard(cellW, cellH)
        drawAnimalHighlights(state, cellW, cellH)
        drawAnimalSelectedHighlight(state, cellW, cellH)
        drawAnimalLegalDots(state, cellW, cellH)
        drawAnimalPieces(state, cellW, cellH)
    }
}

// ── Board Drawing ─────────────────────────────────────────────────────────────

private fun DrawScope.drawRealBoard(cellW: Float, cellH: Float) {
    val boardW = cellW * 7
    val boardH = cellH * 9

    // ① Fill all land
    drawRect(LandColor, size = Size(boardW, boardH))

    // ② Draw each square's terrain
    for (row in 0..8) {
        for (col in 0..6) {
            val sq   = "${'a' + col}$row"
            val left = col * cellW
            val top  = row * cellH
            val rect = Size(cellW, cellH)

            when {
                WATER_SQUARES.contains(sq) -> {
                    drawRect(WaterColor, Offset(left, top), rect)
                    // Sheen stripe across top third of water cell
                    drawRect(WaterSheen.copy(alpha = 0.25f), Offset(left, top), Size(cellW, cellH * 0.35f))
                }
                BLUE_TRAPS.contains(sq) || RED_TRAPS.contains(sq) -> {
                    drawRect(TrapColor, Offset(left, top), rect)
                    // cross-hatch lines on trap
                    drawLine(Color(0x55FFFFFF), Offset(left, top), Offset(left + cellW, top + cellH), 1f)
                    drawLine(Color(0x55FFFFFF), Offset(left + cellW, top), Offset(left, top + cellH), 1f)
                }
                sq == BLUE_DEN || sq == RED_DEN -> {
                    drawRect(DenColor, Offset(left, top), rect)
                }
            }
        }
    }

    // ③ Water lake borders — draw a stroked rect around each 2×3 lake block
    // Left lake: cols b-c (1-2), rows 3-5
    val lakeLeft1 = Offset(1 * cellW, 3 * cellH)
    val lakeSize1 = Size(2 * cellW, 3 * cellH)
    drawRect(WaterBorder, lakeLeft1, lakeSize1, style = Stroke(width = 2.5f))

    // Right lake: cols e-f (4-5), rows 3-5
    val lakeLeft2 = Offset(4 * cellW, 3 * cellH)
    val lakeSize2 = Size(2 * cellW, 3 * cellH)
    drawRect(WaterBorder, lakeLeft2, lakeSize2, style = Stroke(width = 2.5f))

    // Den borders (gold outline)
    drawRect(DenBorder, Offset(3 * cellW, 0f), Size(cellW, cellH), style = Stroke(width = 2.5f))
    drawRect(DenBorder, Offset(3 * cellW, 8 * cellH), Size(cellW, cellH), style = Stroke(width = 2.5f))

    // ④ Grid lines
    for (col in 0..7) {
        drawLine(GridLine, Offset(col * cellW, 0f), Offset(col * cellW, boardH), strokeWidth = 0.8f)
    }
    for (row in 0..9) {
        drawLine(GridLine, Offset(0f, row * cellH), Offset(boardW, row * cellH), strokeWidth = 0.8f)
    }

    // ⑤ Board border
    drawRect(BoardBorder, Offset.Zero, Size(boardW, boardH), style = Stroke(width = 4f))

    // ⑥ Labels drawn via native canvas
    drawIntoCanvas { canvas ->
        val denPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            color     = android.graphics.Color.argb(220, 80, 40, 0)
        }

        // Den labels: ★ + 兽穴
        for (den in listOf(BLUE_DEN, RED_DEN)) {
            val center = animalSquareToOffset(den, cellW, cellH, false)
            denPaint.textSize = cellH * 0.24f
            canvas.nativeCanvas.drawText("★", center.x, center.y - cellH * 0.1f, denPaint)
            denPaint.textSize = cellH * 0.20f
            canvas.nativeCanvas.drawText("兽穴", center.x, center.y + cellH * 0.28f, denPaint)
        }

        // Trap labels: 陷阱
        val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            color     = android.graphics.Color.WHITE
            alpha     = 190
            textSize  = cellH * 0.19f
        }
        for (trap in BLUE_TRAPS + RED_TRAPS) {
            val center = animalSquareToOffset(trap, cellW, cellH, false)
            canvas.nativeCanvas.drawText("陷阱", center.x, center.y + labelPaint.textSize * 0.4f, labelPaint)
        }

        // River label between the two lakes
        val riverPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            color     = android.graphics.Color.argb(180, 255, 255, 255)
            textSize  = cellH * 0.22f
        }
        val riverY = 4 * cellH + cellH * 0.6f
        canvas.nativeCanvas.drawText("河流", 3.5f * cellW, riverY, riverPaint)
    }
}

// ── Highlights ────────────────────────────────────────────────────────────────

private fun DrawScope.drawAnimalHighlights(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    val radius = minOf(cellW, cellH) * 0.44f
    state.lastMoveFrom?.let {
        drawCircle(HighlightLM, radius, animalSquareToOffset(it, cellW, cellH, state.isFlipped))
    }
    state.lastMoveTo?.let {
        drawCircle(HighlightLM, radius, animalSquareToOffset(it, cellW, cellH, state.isFlipped))
    }
}

private fun DrawScope.drawAnimalSelectedHighlight(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    state.selectedSquare?.let {
        val radius = minOf(cellW, cellH) * 0.44f
        drawCircle(HighlightSel, radius, animalSquareToOffset(it, cellW, cellH, state.isFlipped))
    }
}

private fun DrawScope.drawAnimalLegalDots(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    val r = minOf(cellW, cellH) * 0.44f
    for (target in state.legalTargets) {
        val center = animalSquareToOffset(target, cellW, cellH, state.isFlipped)
        if (state.pieceMap.containsKey(target)) {
            drawCircle(LegalDotColor, r, center, style = Stroke(width = r * 0.18f))
        } else {
            drawCircle(LegalDotColor, r * 0.28f, center)
        }
    }
}

// ── Pieces ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawAnimalPieces(state: AnimalUiBoardState, cellW: Float, cellH: Float) {
    val radius      = minOf(cellW, cellH) * 0.42f
    val innerRadius = radius * 0.80f
    val shadowColor = Color(0x44000000)

    for ((square, piece) in state.pieceMap) {
        val center    = animalSquareToOffset(square, cellW, cellH, state.isFlipped)
        val teamColor = if (piece.color == com.example.chesstacticstrainer.domain.model.AnimalColor.RED)
            RedTeam else BlueTeam

        // Shadow
        drawCircle(shadowColor, radius * 1.05f, center.copy(y = center.y + radius * 0.12f))
        // Team disc
        drawCircle(teamColor, radius, center)
        // Team disc rim (slightly lighter)
        drawCircle(teamColor.copy(alpha = 0.6f), radius, center, style = Stroke(width = radius * 0.12f))
        // White inner
        drawCircle(Color.White, innerRadius, center)
        // Thin border on inner circle
        drawCircle(Color(0x33000000), innerRadius, center, style = Stroke(width = 1f))

        // Emoji
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize  = innerRadius * 1.25f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface  = Typeface.DEFAULT
            }
            val textY = center.y + paint.textSize * 0.37f
            canvas.nativeCanvas.drawText(piece.emoji, center.x, textY, paint)
        }
    }
}
