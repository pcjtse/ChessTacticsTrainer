package com.example.chesstacticstrainer.presentation.board

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.chesstacticstrainer.domain.model.PieceColor
import com.example.chesstacticstrainer.presentation.theme.DarkSquare
import com.example.chesstacticstrainer.presentation.theme.HighlightCheck
import com.example.chesstacticstrainer.presentation.theme.HighlightLastMove
import com.example.chesstacticstrainer.presentation.theme.HighlightSelected
import com.example.chesstacticstrainer.presentation.theme.LegalMoveDot
import com.example.chesstacticstrainer.presentation.theme.LightSquare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ChessBoardComponent(
    state: UiBoardState,
    onSquareTapped: (square: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var pieceBitmaps by remember { mutableStateOf<Map<String, ImageBitmap>?>(null) }

    LaunchedEffect(Unit) {
        pieceBitmaps = withContext(Dispatchers.IO) { PieceRenderer.load(context) }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { canvasSize = it }
            .pointerInput(state, canvasSize) {
                if (canvasSize.width == 0) return@pointerInput
                val squareSize = canvasSize.width / 8f
                detectTapGestures { offset ->
                    val sq = offsetToSquare(offset.x, offset.y, squareSize, state.isFlipped)
                    if (sq != null) onSquareTapped(sq)
                }
            }
    ) {
        val squareSize = size.width / 8f
        drawSquares(squareSize)
        // Last-move and check highlights drawn under pieces (square colour shows around piece)
        drawBoardHighlights(state, squareSize)
        val bitmaps = pieceBitmaps
        if (bitmaps != null) {
            drawPieces(state, squareSize, bitmaps)
        } else {
            drawPiecesPlaceholder(state, squareSize)
        }
        // Selected-piece highlight drawn on top of piece so it's clearly visible
        drawSelectedHighlight(state, squareSize)
        // Legal-move dots/rings drawn on top of pieces so capture rings are visible
        drawLegalMoveDots(state, squareSize)
    }
}

private fun DrawScope.drawSquares(squareSize: Float) {
    for (row in 0..7) {
        for (col in 0..7) {
            drawRect(
                color = if ((row + col) % 2 == 0) LightSquare else DarkSquare,
                topLeft = Offset(col * squareSize, row * squareSize),
                size = Size(squareSize, squareSize)
            )
        }
    }
}

private fun DrawScope.drawBoardHighlights(state: UiBoardState, squareSize: Float) {
    state.lastMoveFrom?.let { fillSquare(it, HighlightLastMove, squareSize, state.isFlipped) }
    state.lastMoveTo?.let { fillSquare(it, HighlightLastMove, squareSize, state.isFlipped) }
    state.checkedKingSquare?.let { fillSquare(it, HighlightCheck, squareSize, state.isFlipped) }
}

private fun DrawScope.drawSelectedHighlight(state: UiBoardState, squareSize: Float) {
    state.selectedSquare?.let { fillSquare(it, HighlightSelected, squareSize, state.isFlipped) }
}

private fun DrawScope.drawLegalMoveDots(state: UiBoardState, squareSize: Float) {
    for (target in state.legalTargets) {
        val (col, row) = squareToColRow(target, state.isFlipped)
        val center = Offset(col * squareSize + squareSize / 2f, row * squareSize + squareSize / 2f)
        if (state.pieceMap.containsKey(target)) {
            drawCircle(
                color = LegalMoveDot,
                radius = squareSize * 0.47f,
                center = center,
                style = Stroke(width = squareSize * 0.08f)
            )
        } else {
            drawCircle(color = LegalMoveDot, radius = squareSize * 0.15f, center = center)
        }
    }
}

private fun DrawScope.drawPieces(
    state: UiBoardState,
    squareSize: Float,
    bitmaps: Map<String, ImageBitmap>
) {
    for ((square, piece) in state.pieceMap) {
        val (col, row) = squareToColRow(square, state.isFlipped)
        val colorChar = if (piece.color == PieceColor.WHITE) 'w' else 'b'
        val bitmap = bitmaps["$colorChar${piece.letter}"] ?: continue
        drawImage(
            image = bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstOffset = IntOffset((col * squareSize).toInt(), (row * squareSize).toInt()),
            dstSize = IntSize(squareSize.toInt(), squareSize.toInt()),
            filterQuality = FilterQuality.High
        )
    }
}

// Minimal placeholder shown for the brief moment before SVGs finish loading.
private fun DrawScope.drawPiecesPlaceholder(state: UiBoardState, squareSize: Float) {
    for ((square, piece) in state.pieceMap) {
        val (col, row) = squareToColRow(square, state.isFlipped)
        val cx = col * squareSize + squareSize / 2f
        val cy = row * squareSize + squareSize / 2f
        val isWhite = piece.color == PieceColor.WHITE
        drawCircle(
            color = if (isWhite) Color(0xEEFFFDD0) else Color(0xEE1A1A1A),
            radius = squareSize * 0.38f,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.fillSquare(square: String, color: Color, squareSize: Float, isFlipped: Boolean) {
    val (col, row) = squareToColRow(square, isFlipped)
    drawRect(
        color = color,
        topLeft = Offset(col * squareSize, row * squareSize),
        size = Size(squareSize, squareSize)
    )
}
