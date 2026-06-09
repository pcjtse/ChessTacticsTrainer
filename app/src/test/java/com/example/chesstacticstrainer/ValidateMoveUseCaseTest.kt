package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.model.BoardState
import com.example.chesstacticstrainer.domain.usecase.ValidateMoveUseCase
import com.example.chesstacticstrainer.engine.ChessLibEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateMoveUseCaseTest {

    private lateinit var useCase: ValidateMoveUseCase
    private lateinit var engine: ChessLibEngine

    // Italian Game, after White plays Ng5 (f3g5). This is the puzzle starting position
    // (FEN IS the position shown to the player — no trigger move needed).
    // Lichess format: solution[0] = player's first move, solution[1] = computer reply, etc.
    private val puzzleFen = "r1bqkb1r/pppp1ppp/2n2n2/4p1N1/2B1P3/8/PPPP1PPP/RNBQK2R b KQkq - 5 4"
    private val solutionMoves = listOf("d7d5", "g5f7")

    private lateinit var puzzleBoard: BoardState

    @Before
    fun setup() {
        engine = ChessLibEngine()
        useCase = ValidateMoveUseCase(engine)
        // Load puzzle FEN directly — no trigger application
        puzzleBoard = engine.loadFen(puzzleFen)
    }

    @Test
    fun `correct user move returns isCorrect true`() {
        // Player plays solutionMoves[0] = "d7d5"
        val result = useCase(puzzleBoard, solutionMoves[0], solutionMoves, moveIndex = 0)
        assertTrue(result.isCorrect)
    }

    @Test
    fun `wrong user move returns isCorrect false`() {
        val result = useCase(puzzleBoard, "e5e4", solutionMoves, moveIndex = 0)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `wrong move has no computer reply`() {
        val result = useCase(puzzleBoard, "e5e4", solutionMoves, moveIndex = 0)
        assertNull(result.computerReply)
    }

    @Test
    fun `correct move with computer reply sets computerReply`() {
        val result = useCase(puzzleBoard, solutionMoves[0], solutionMoves, moveIndex = 0)
        assertNotNull(result.computerReply)
        assertEquals(solutionMoves[1], result.computerReply)
    }

    @Test
    fun `final correct move has null computer reply`() {
        // solution has only [0]=player move, no [1] computer reply
        val oneMoveSolution = listOf("d7d5")
        val result = useCase(puzzleBoard, oneMoveSolution[0], oneMoveSolution, moveIndex = 0)
        assertTrue(result.isCorrect)
        assertNull(result.computerReply)
    }

    @Test
    fun `second user move uses correct index`() {
        // Solution: [0]=player, [1]=cpu, [2]=player, [3]=cpu
        // moveIndex=1 expects solutionMoves[2]
        val longSolution = listOf("e2e4", "e7e5", "g1f3", "b8c6")
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val b0 = engine.loadFen(startFen)
        val b1 = engine.applyMove(b0, longSolution[0]) // after e2e4 (player move)
        val b2 = engine.applyMove(b1, longSolution[1]) // after e7e5 (computer reply)
        // Now moveIndex=1: player plays longSolution[2] = "g1f3"
        val result = useCase(b2, longSolution[2], longSolution, moveIndex = 1)
        assertTrue(result.isCorrect)
    }
}
