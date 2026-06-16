package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.usecase.ValidateAnimalMoveUseCase
import com.example.chesstacticstrainer.engine.AnimalChessEngineImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateAnimalMoveUseCaseTest {

    private lateinit var engine: AnimalChessEngineImpl
    private lateinit var useCase: ValidateAnimalMoveUseCase

    @Before
    fun setup() {
        engine = AnimalChessEngineImpl()
        useCase = ValidateAnimalMoveUseCase(engine)
    }

    @Test
    fun `correct first move is accepted`() {
        // Red Lion at d1, solution = d1d0 (enter Blue den)
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1d0", listOf("d1d0"), moveIndex = 0)
        assertTrue("Correct first move should be accepted", result.isCorrect)
    }

    @Test
    fun `wrong move is rejected`() {
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1c1", listOf("d1d0"), moveIndex = 0)
        assertFalse("Wrong move should be rejected", result.isCorrect)
    }

    @Test
    fun `correct move returns computer reply`() {
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1d0", listOf("d1d0", "a8b8"), moveIndex = 0)
        assertTrue(result.isCorrect)
        assertEquals("Computer reply should be returned", "a8b8", result.computerReply)
    }

    @Test
    fun `wrong move returns no computer reply`() {
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1c1", listOf("d1d0", "a8b8"), moveIndex = 0)
        assertFalse(result.isCorrect)
        assertNull("No reply for wrong move", result.computerReply)
    }

    @Test
    fun `second player move at moveIndex 1 checked against index 2 of solution`() {
        // solutionMoves: [player1, reply1, player2]
        // At moveIndex=1, player move is solutionMoves[2]
        val state = engine.loadFen("7/7/7/7/7/7/7/3L3/7 r")
        val solutionMoves = listOf("d7d6", "a0b0", "d6d5")
        // moveIndex=1: expected = solutionMoves[1*2] = solutionMoves[2] = "d6d5"
        val stateAfterTwoMoves = engine.applyMove(
            engine.applyMove(
                engine.applyMove(state, "d7d6"),
                "a0b0"
            ),
            // Now it's Red to move again
            "d7d6" // won't work directly, use already-applied state
        )
        // Just test the index math: at moveIndex=1, expected = solutionMoves[2]
        val boardAt1 = engine.loadFen("7/7/7/7/7/3L3/7/7/7 r")
        val result = useCase(boardAt1, "d5d4", listOf("d7d6", "a0b0", "d5d4"), moveIndex = 1)
        assertTrue("Correct second move is accepted at moveIndex 1", result.isCorrect)
    }

    @Test
    fun `no solution moves means any move is correct`() {
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1c1", emptyList(), moveIndex = 0)
        assertTrue("Any move is correct when no solution provided", result.isCorrect)
    }

    @Test
    fun `new board state is returned after move`() {
        val state = engine.loadFen("7/3L3/7/7/7/7/7/7/7 r")
        val result = useCase(state, "d1d0", listOf("d1d0"), moveIndex = 0)
        assertNotNull("New board state should be returned", result.newBoardState)
        assertNull("d1 should be empty after move", result.newBoardState.pieceMap["d1"])
        assertNotNull("d0 should have piece after move", result.newBoardState.pieceMap["d0"])
    }
}
