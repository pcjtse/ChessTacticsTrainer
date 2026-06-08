package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.usecase.GetExplanationUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class GetExplanationUseCaseTest {

    private lateinit var useCase: GetExplanationUseCase

    @Before
    fun setup() {
        useCase = GetExplanationUseCase()
    }

    @Test
    fun `mateIn1 theme gives correct tactic name`() {
        val result = useCase(listOf("mateIn1"), isCorrect = true)
        assertEquals("Checkmate in 1", result.tacticName)
    }

    @Test
    fun `fork theme gives fork explanation`() {
        val result = useCase(listOf("fork"), isCorrect = true)
        assertEquals("Fork", result.tacticName)
        assertFalse(result.description.isEmpty())
    }

    @Test
    fun `incorrect move gives incorrect explanation`() {
        val result = useCase(listOf("fork"), isCorrect = false)
        assertEquals("Incorrect Move", result.tacticName)
    }

    @Test
    fun `mateIn1 takes priority over fork`() {
        val result = useCase(listOf("fork", "mateIn1"), isCorrect = true)
        assertEquals("Checkmate in 1", result.tacticName)
    }

    @Test
    fun `unknown theme falls back to best move`() {
        val result = useCase(listOf("someUnknownTheme"), isCorrect = true)
        assertEquals("Best Move", result.tacticName)
    }

    @Test
    fun `empty themes falls back to best move`() {
        val result = useCase(emptyList(), isCorrect = true)
        assertEquals("Best Move", result.tacticName)
    }
}
