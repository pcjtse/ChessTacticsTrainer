package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.usecase.GetAnimalExplanationUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAnimalExplanationUseCaseTest {

    private lateinit var useCase: GetAnimalExplanationUseCase

    @Before
    fun setup() {
        useCase = GetAnimalExplanationUseCase()
    }

    @Test
    fun `wrong move returns error explanation`() {
        val result = useCase(listOf("denEntry"), isCorrect = false)
        assertEquals("走法有误", result.tacticName)
        assertTrue("Description should mention finding a move",
            result.description.contains("着法"))
    }

    @Test
    fun `denEntry theme returns correct Chinese text`() {
        val result = useCase(listOf("denEntry"), isCorrect = true)
        assertEquals("进入兽穴！", result.tacticName)
        assertTrue("Description should mention den", result.description.contains("兽穴"))
    }

    @Test
    fun `mouseElephant theme returns correct Chinese text`() {
        val result = useCase(listOf("mouseElephant"), isCorrect = true)
        assertEquals("鼠胜象！", result.tacticName)
        assertTrue("Description should mention mouse and elephant",
            result.description.contains("鼠") || result.description.contains("象"))
    }

    @Test
    fun `waterJump theme returns correct Chinese text`() {
        val result = useCase(listOf("waterJump"), isCorrect = true)
        assertEquals("跳水！", result.tacticName)
        assertTrue("Description should mention water or jump",
            result.description.contains("水") || result.description.contains("跳"))
    }

    @Test
    fun `trap theme returns correct Chinese text`() {
        val result = useCase(listOf("trap"), isCorrect = true)
        assertEquals("陷阱！", result.tacticName)
        assertTrue("Description should mention trap", result.description.contains("陷阱"))
    }

    @Test
    fun `fork theme returns correct Chinese text`() {
        val result = useCase(listOf("fork"), isCorrect = true)
        assertEquals("双攻！", result.tacticName)
    }

    @Test
    fun `capture theme returns correct Chinese text`() {
        val result = useCase(listOf("capture"), isCorrect = true)
        assertEquals("捕获！", result.tacticName)
    }

    @Test
    fun `unknown theme returns default explanation`() {
        val result = useCase(listOf("unknownTheme"), isCorrect = true)
        assertEquals("最佳着法", result.tacticName)
    }

    @Test
    fun `empty themes list returns default explanation`() {
        val result = useCase(emptyList(), isCorrect = true)
        assertEquals("最佳着法", result.tacticName)
    }

    @Test
    fun `denEntry has higher priority than capture`() {
        // When both themes present, denEntry should win (it's first in THEME_PRIORITY)
        val result = useCase(listOf("capture", "denEntry"), isCorrect = true)
        assertEquals("进入兽穴！", result.tacticName)
    }

    @Test
    fun `wrong move explanation does not depend on theme`() {
        val result1 = useCase(listOf("denEntry"), isCorrect = false)
        val result2 = useCase(listOf("waterJump"), isCorrect = false)
        val result3 = useCase(emptyList(), isCorrect = false)
        assertEquals(result1.tacticName, result2.tacticName)
        assertEquals(result1.tacticName, result3.tacticName)
        assertEquals("走法有误", result1.tacticName)
    }
}
