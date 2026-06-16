package com.example.chesstacticstrainer.presentation.board

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoStone

// ── Board palette ─────────────────────────────────────────────────────────────
private val BoardBg      = Color(0xFFDCB483)
private val LineColor    = Color(0xFF5C3A1E)
private val HoshiColor   = Color(0xFF5C3A1E)
private val HintGreen    = Color(0x8800CC44)
private val LastMoveDot  = Color(0xCCFF4444)
private val StoneShadow  = Color(0x40000000)
private val WhiteStroke  = Color(0xFF333333)
private val EdgeLine     = Color(0xFF3A1E05)   // slightly darker for board-edge lines

@Composable
fun GoBoardComponent(
    state: GoUiBoardState,
    onPointTapped: (GoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    // Viewport is fixed per-puzzle (set once when puzzle loads).
    // Fall back to a full-board viewport when absent (e.g. 9×9 bundled puzzles).
    val viewport = state.viewport ?: GoViewport(0, 0, state.boardSize)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { canvasSize = it }
            .pointerInput(viewport, canvasSize) {
                if (canvasSize.width == 0) return@pointerInput
                val cellSize = canvasSize.width / viewport.size.toFloat()
                detectTapGestures { offset ->
                    offsetToGoPoint(offset.x, offset.y, cellSize, state.boardSize, viewport)
                        ?.let { onPointTapped(it) }
                }
            }
    ) {
        val cellSize = size.width / viewport.size.toFloat()
        drawBoard(state, cellSize, viewport)
        drawHints(state, cellSize, viewport)
        drawStones(state, cellSize, viewport)
        drawLastMoveMark(state, cellSize, viewport)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Canvas coordinate for a board point given the active viewport. */
private fun pointToCanvas(point: GoPoint, cellSize: Float, viewport: GoViewport): Offset =
    Offset(
        (point.col - viewport.minCol) * cellSize + cellSize / 2f,
        (point.row - viewport.minRow) * cellSize + cellSize / 2f
    )

/** True when this board point is inside the visible viewport. */
private fun GoPoint.inViewport(v: GoViewport) =
    col in v.minCol..v.maxCol && row in v.minRow..v.maxRow

// ── Board grid ────────────────────────────────────────────────────────────────

private fun DrawScope.drawBoard(state: GoUiBoardState, cellSize: Float, viewport: GoViewport) {
    drawRect(BoardBg)

    val n         = viewport.size
    val halfCell  = cellSize / 2f
    val lineColor = LineColor.toArgb()
    val edgeColor = EdgeLine.toArgb()

    drawIntoCanvas { canvas ->
        val innerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color       = lineColor
            style       = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val edgePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color       = edgeColor
            style       = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
        }

        // Horizontal lines — use edge paint when on the board boundary
        for (rowIdx in 0 until n) {
            val boardRow = viewport.minRow + rowIdx
            val y        = rowIdx * cellSize + halfCell
            val paint    = if (boardRow == 0 || boardRow == state.boardSize - 1) edgePaint else innerPaint
            canvas.nativeCanvas.drawLine(halfCell, y, (n - 1) * cellSize + halfCell, y, paint)
        }
        // Vertical lines
        for (colIdx in 0 until n) {
            val boardCol = viewport.minCol + colIdx
            val x        = colIdx * cellSize + halfCell
            val paint    = if (boardCol == 0 || boardCol == state.boardSize - 1) edgePaint else innerPaint
            canvas.nativeCanvas.drawLine(x, halfCell, x, (n - 1) * cellSize + halfCell, paint)
        }

        // Hoshi star points — only those visible in the viewport
        val hoshiFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            style = android.graphics.Paint.Style.FILL
        }
        val hoshiR = cellSize * 0.1f
        for (pt in hoshiPoints(state.boardSize)) {
            if (!pt.inViewport(viewport)) continue
            val cx = (pt.col - viewport.minCol) * cellSize + halfCell
            val cy = (pt.row - viewport.minRow) * cellSize + halfCell
            canvas.nativeCanvas.drawCircle(cx, cy, hoshiR, hoshiFill)
        }
    }
}

// ── Hint highlight ────────────────────────────────────────────────────────────

private fun DrawScope.drawHints(state: GoUiBoardState, cellSize: Float, viewport: GoViewport) {
    val point = state.hintPoint ?: return
    if (!point.inViewport(viewport)) return
    drawCircle(HintGreen, cellSize * 0.45f, pointToCanvas(point, cellSize, viewport))
}

// ── Stones ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawStones(state: GoUiBoardState, cellSize: Float, viewport: GoViewport) {
    val stoneR  = cellSize * 0.45f
    val shadowR = stoneR + 1f

    for ((point, color) in state.stones) {
        if (!point.inViewport(viewport)) continue
        val center = pointToCanvas(point, cellSize, viewport)
        drawCircle(StoneShadow, shadowR, center.copy(y = center.y + stoneR * 0.08f))
        when (color) {
            GoStone.BLACK -> drawCircle(Color.Black, stoneR, center)
            GoStone.WHITE -> {
                drawCircle(Color.White, stoneR, center)
                drawCircle(WhiteStroke, stoneR, center, style = Stroke(width = 1.5f))
            }
        }
    }
}

// ── Last-move marker ──────────────────────────────────────────────────────────

private fun DrawScope.drawLastMoveMark(state: GoUiBoardState, cellSize: Float, viewport: GoViewport) {
    val point = state.lastMove ?: return
    if (!point.inViewport(viewport)) return
    val stone = state.stones[point] ?: return
    val markerColor = if (stone == GoStone.BLACK) Color.White else Color.Black
    drawCircle(markerColor, cellSize * 0.13f, pointToCanvas(point, cellSize, viewport))
}
