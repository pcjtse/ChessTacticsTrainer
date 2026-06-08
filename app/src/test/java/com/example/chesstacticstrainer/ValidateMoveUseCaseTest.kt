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

    // Italian Game position (white to move); solutionMoves follows Lichess format:
    // [0] = opponent's first move (applied before showing puzzle)
    // [1] = first user move (solutionMoves[moveIndex*2+1] with moveIndex=0)
    // [2] = computer reply
    // [3] = second user move
    private val fen = "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
    private val solutionMoves = listOf("f3g5", "d7d5", "g5f7")

    private lateinit var boardAfterOpponent: BoardState

    @Before
    fun setup() {
        engine = ChessLibEngine()
        useCase = ValidateMoveUseCase(engine)
        // Apply opponent's first move (solutionMoves[0]) to get the board shown to user
        val initial = engine.loadFen(fen)
        boardAfterOpponent = engine.applyMove(initial, solutionMoves[0])
    }

    @Test
    fun `correct user move returns isCorrect true`() {
        // User plays solutionMoves[1] = "d7d5"
        val result = useCase(boardAfterOpponent, solutionMoves[1], solutionMoves, moveIndex = 0)
        assertTrue(result.isCorrect)
    }

    @Test
    fun `wrong user move returns isCorrect false`() {
        val result = useCase(boardAfterOpponent, "e5e4", solutionMoves, moveIndex = 0)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `wrong move has no computer reply`() {
        val result = useCase(boardAfterOpponent, "e5e4", solutionMoves, moveIndex = 0)
        assertNull(result.computerReply)
    }

    @Test
    fun `correct move with computer reply sets computerReply`() {
        val result = useCase(boardAfterOpponent, solutionMoves[1], solutionMoves, moveIndex = 0)
        assertNotNull(result.computerReply)
        assertEquals(solutionMoves[2], result.computerReply)
    }

    @Test
    fun `final correct move has null computer reply`() {
        // When solutionMoves has no index 2, computerReply must be null
        val twoMoveSolution = listOf("f3g5", "d7d5") // only [0]=opponent, [1]=user, no [2]
        val result = useCase(boardAfterOpponent, twoMoveSolution[1], twoMoveSolution, moveIndex = 0)
        assertTrue(result.isCorrect)
        assertNull(result.computerReply)
    }

    @Test
    fun `second user move uses correct index`() {
        // After first user+computer pair, moveIndex=1, expected user move = solutionMoves[3]
        val longSolution = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5")
        // moveIndex=1: expects solutionMoves[3] = "b8c6"
        val startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val b0 = engine.loadFen(startFen)
        val b1 = engine.applyMove(b0, longSolution[0]) // after e2e4
        val b2 = engine.applyMove(b1, longSolution[1]) // after e7e5
        val b3 = engine.applyMove(b2, longSolution[2]) // after g1f3 (computer reply)
        // moveIndex=1: user plays longSolution[3] = "b8c6"
        val result = useCase(b3, longSolution[3], longSolution, moveIndex = 1)
        assertTrue(result.isCorrect)
    }
}
