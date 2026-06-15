package com.example.chesstacticstrainer.presentation.board

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BoardBg        = Color(0xFFDEB887)
private val GridLine       = Color(0xFF5D3A1A)
private val RiverText      = Color(0xFF4A2E00)
private val PalaceLine     = Color(0xFF5D3A1A)
private val XqHighlightLM  = Color(0x88F9A825)
private val XqHighlightSel = Color(0xAAFFD54F)
private val XqHighlightChk = Color(0xAAEF5350)
private val XqLegalDot     = Color(0xBB43A047)

@Composable
fun XiangqiBoardComponent(
    state: XiangqiUiBoardState,
    onSquareTapped: (square: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var pieceBitmaps by remember { mutableStateOf<Map<String, ImageBitmap>?>(null) }

    LaunchedEffect(Unit) {
        pieceBitmaps = withContext(Dispatchers.IO) { XiangqiPieceRenderer.load(context) }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 10f)
            .onSizeChanged { canvasSize = it }
            .pointerInput(state, canvasSize) {
                if (canvasSize.width == 0) return@pointerInput
                val cellW = canvasSize.width / 9f
                val cellH = canvasSize.height / 10f
                detectTapGestures { offset ->
                    val sq = offsetToXiangqiSquare(offset.x, offset.y, cellW, cellH, state.isFlipped)
                    if (sq != null) onSquareTapped(sq)
                }
            }
    ) {
        val cellW = size.width / 9f
        val cellH = size.height / 10f

        drawBackground(cellW, cellH)
        drawHighlights(state, cellW, cellH)
        val bitmaps = pieceBitmaps
        if (bitmaps != null) drawPieces(state, cellW, cellH, bitmaps)
        drawSelectedHighlight(state, cellW, cellH)
        drawLegalDots(state, cellW, cellH)
    }
}

// ── Board chrome ─────────────────────────────────────────────────────────────

private fun DrawScope.drawBackground(cellW: Float, cellH: Float) {
    // Board background
    drawRect(BoardBg)

    val stroke = Stroke(width = 1.5f)
    val left   = 0.5f * cellW
    val right  = 8.5f * cellW
    val top    = 0.5f * cellH
    val bottom = 9.5f * cellH

    // Horizontal grid lines (10 lines at rows 0–9)
    for (row in 0..9) {
        val y = (row + 0.5f) * cellH
        drawLine(GridLine, Offset(left, y), Offset(right, y), strokeWidth = 1.5f)
    }

    // Vertical grid lines — all columns break at the river (ranks 4–5 gap)
    // On a real Xiangqi board no vertical lines cross the 楚河漢界 at all
    for (col in 0..8) {
        val x = (col + 0.5f) * cellW
        drawLine(GridLine, Offset(x, top),        Offset(x, 4.5f * cellH), strokeWidth = 1.5f)
        drawLine(GridLine, Offset(x, 5.5f * cellH), Offset(x, bottom),      strokeWidth = 1.5f)
    }

    // Palace diagonals — Black palace: files d–f (cols 3–5), ranks 0–2 (rows 0–2)
    val bp0 = Offset(3.5f * cellW, 0.5f * cellH)
    val bp1 = Offset(5.5f * cellW, 0.5f * cellH)
    val bp2 = Offset(3.5f * cellW, 2.5f * cellH)
    val bp3 = Offset(5.5f * cellW, 2.5f * cellH)
    drawLine(PalaceLine, bp0, bp3, strokeWidth = 1.2f)
    drawLine(PalaceLine, bp1, bp2, strokeWidth = 1.2f)

    // Palace diagonals — Red palace: files d–f (cols 3–5), ranks 7–9 (rows 7–9)
    val rp0 = Offset(3.5f * cellW, 7.5f * cellH)
    val rp1 = Offset(5.5f * cellW, 7.5f * cellH)
    val rp2 = Offset(3.5f * cellW, 9.5f * cellH)
    val rp3 = Offset(5.5f * cellW, 9.5f * cellH)
    drawLine(PalaceLine, rp0, rp3, strokeWidth = 1.2f)
    drawLine(PalaceLine, rp1, rp2, strokeWidth = 1.2f)

    // River text
    val riverY = 4.5f * cellH + cellH / 2f
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(200, 74, 46, 0)
            textSize = cellH * 0.52f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText("楚  河", 3f * cellW, riverY, paint)
        canvas.nativeCanvas.drawText("漢  界", 6f * cellW, riverY, paint)
    }
}

// ── Highlights ────────────────────────────────────────────────────────────────

private fun DrawScope.drawHighlights(state: XiangqiUiBoardState, cellW: Float, cellH: Float) {
    val pieceRadius = minOf(cellW, cellH) * 0.44f
    state.lastMoveFrom?.let { highlightCircle(it, XqHighlightLM, pieceRadius, cellW, cellH, state.isFlipped) }
    state.lastMoveTo?.let   { highlightCircle(it, XqHighlightLM, pieceRadius, cellW, cellH, state.isFlipped) }
    state.checkedGeneralSquare?.let { highlightCircle(it, XqHighlightChk, pieceRadius, cellW, cellH, state.isFlipped) }
}

private fun DrawScope.drawSelectedHighlight(state: XiangqiUiBoardState, cellW: Float, cellH: Float) {
    val pieceRadius = minOf(cellW, cellH) * 0.44f
    state.selectedSquare?.let { highlightCircle(it, XqHighlightSel, pieceRadius, cellW, cellH, state.isFlipped) }
}

private fun DrawScope.highlightCircle(
    square: String, color: Color, radius: Float,
    cellW: Float, cellH: Float, isFlipped: Boolean
) {
    val center = xiangqiSquareToOffset(square, cellW, cellH, isFlipped)
    drawCircle(color, radius, center)
}

// ── Pieces ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawPieces(
    state: XiangqiUiBoardState,
    cellW: Float,
    cellH: Float,
    bitmaps: Map<String, ImageBitmap>
) {
    val pieceSize = (minOf(cellW, cellH) * 0.88f).toInt()
    for ((square, piece) in state.pieceMap) {
        val center = xiangqiSquareToOffset(square, cellW, cellH, state.isFlipped)
        val half   = pieceSize / 2
        val bitmap = bitmaps[piece.svgKey]
        if (bitmap != null && bitmap.width > 1) {
            drawImage(
                image = bitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(bitmap.width, bitmap.height),
                dstOffset = IntOffset((center.x - half).toInt(), (center.y - half).toInt()),
                dstSize = IntSize(pieceSize, pieceSize),
                filterQuality = FilterQuality.High
            )
        }
    }
}

// ── Legal move indicators ─────────────────────────────────────────────────────

private fun DrawScope.drawLegalDots(state: XiangqiUiBoardState, cellW: Float, cellH: Float) {
    val pieceRadius = minOf(cellW, cellH) * 0.44f
    for (target in state.legalTargets) {
        val center = xiangqiSquareToOffset(target, cellW, cellH, state.isFlipped)
        if (state.pieceMap.containsKey(target)) {
            drawCircle(XqLegalDot, pieceRadius, center, style = Stroke(width = pieceRadius * 0.16f))
        } else {
            drawCircle(XqLegalDot, pieceRadius * 0.30f, center)
        }
    }
}
