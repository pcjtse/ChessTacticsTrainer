package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.model.PieceColor
import com.example.chesstacticstrainer.domain.model.PieceType
import com.example.chesstacticstrainer.engine.ChessLibEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChessLibEngineTest {

    private lateinit var engine: ChessLibEngine

    @Before
    fun setup() {
        engine = ChessLibEngine()
    }

    @Test
    fun `loadFen starting position has 32 pieces`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        assertEquals(32, state.pieceMap.size)
    }

    @Test
    fun `loadFen identifies white king on e1`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        val king = state.pieceMap["e1"]
        assertNotNull(king)
        assertEquals(PieceType.KING, king!!.type)
        assertEquals(PieceColor.WHITE, king.color)
    }

    @Test
    fun `sideToMove returns white on starting position`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        assertEquals("white", engine.sideToMove(state))
    }

    @Test
    fun `applyMove e2e4 updates board correctly`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        val newState = engine.applyMove(state, "e2e4")
        assertNull(newState.pieceMap["e2"])
        assertNotNull(newState.pieceMap["e4"])
        assertEquals(PieceType.PAWN, newState.pieceMap["e4"]!!.type)
    }

    @Test
    fun `getLegalMovesFromSquare e2 returns 2 moves at start`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        val moves = engine.getLegalMovesFromSquare(state, "e2")
        assertEquals(2, moves.size)
        assertTrue(moves.any { it.uci == "e2e3" })
        assertTrue(moves.any { it.uci == "e2e4" })
    }

    @Test
    fun `isInCheck returns true when king is in check`() {
        // Fool's mate position - white king is in check
        val checkFen = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3"
        val state = engine.loadFen(checkFen)
        assertTrue(engine.isInCheck(state))
    }

    @Test
    fun `isCheckmate returns true on fool's mate`() {
        val mateFen = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3"
        val state = engine.loadFen(mateFen)
        assertTrue(engine.isCheckmate(state))
    }

    @Test
    fun `isMoveLegal returns false for illegal move`() {
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val state = engine.loadFen(startFen)
        assertFalse(engine.isMoveLegal(state, "e2e5"))
    }
}
