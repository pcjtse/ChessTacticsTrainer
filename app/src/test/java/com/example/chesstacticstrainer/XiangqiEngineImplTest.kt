package com.example.chesstacticstrainer

import com.example.chesstacticstrainer.domain.model.XiangqiColor
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType
import com.example.chesstacticstrainer.domain.usecase.ValidateXiangqiMoveUseCase
import com.example.chesstacticstrainer.engine.XiangqiEngineImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class XiangqiEngineImplTest {

    private lateinit var engine: XiangqiEngineImpl

    @Before
    fun setup() {
        engine = XiangqiEngineImpl()
    }

    // ── FEN Parsing ──────────────────────────────────────────────────────────

    @Test
    fun `starting position has 32 pieces`() {
        val state = engine.loadFen(STARTING_FEN)
        assertEquals(32, state.pieceMap.size)
    }

    @Test
    fun `starting position side to move is Red`() {
        assertEquals(XiangqiColor.RED, engine.loadFen(STARTING_FEN).sideToMove)
    }

    @Test
    fun `loadFen places generals correctly`() {
        val state = engine.loadFen("4k4/9/9/9/9/9/9/9/9/4K4 w - - 0 1")
        val bk = state.pieceMap["e0"]
        val rk = state.pieceMap["e9"]
        assertNotNull(bk); assertEquals(XiangqiPieceType.GENERAL, bk!!.type); assertEquals(XiangqiColor.BLACK, bk.color)
        assertNotNull(rk); assertEquals(XiangqiPieceType.GENERAL, rk!!.type); assertEquals(XiangqiColor.RED,   rk.color)
    }

    @Test
    fun `loadFen black-to-move FEN sets sideToMove BLACK`() {
        assertEquals(XiangqiColor.BLACK, engine.loadFen("4k4/9/9/9/9/9/9/9/9/4K4 b - - 0 1").sideToMove)
    }

    // ── Chariot ──────────────────────────────────────────────────────────────

    @Test
    fun `chariot slides along rank and file from open position`() {
        // Red chariot at b5; generals off the chariot's file and rank
        val state = engine.loadFen("k8/9/9/9/9/1R7/9/9/9/4K4 w - - 0 1")
        val moves = engine.getLegalMovesFromSquare(state, "b5")
        assertTrue("Chariot in open position should have many moves", moves.size >= 16)
    }

    @Test
    fun `chariot stops before own piece and cannot capture it`() {
        // Red chariot at a9, Red general at e9 blocks rightward after d9
        val state = engine.loadFen("k8/9/9/9/9/9/9/9/9/R3K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "a9").map { it.toSquare }
        assertFalse("Chariot must not capture own general at e9", "e9" in targets)
        assertTrue("Chariot should reach d9 (one before own general)", "d9" in targets)
    }

    @Test
    fun `chariot can capture enemy piece`() {
        // Red chariot at a9, Black chariot at a0; generals on different files
        val state = engine.loadFen("r4k3/9/9/9/9/9/9/9/9/R3K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "a9").map { it.toSquare }
        assertTrue("Chariot should capture enemy at a0", "a0" in targets)
    }

    // ── Cannon ───────────────────────────────────────────────────────────────

    @Test
    fun `cannon slides to empty squares`() {
        // Red cannon at e5; Black general at a0 (off cannon's file), Red general at e9
        val state = engine.loadFen("k8/9/9/9/9/4C4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        assertTrue("Cannon should reach e4 (slide up)", "e4" in targets)
        assertTrue("Cannon should reach e6 (slide down)", "e6" in targets)
    }

    @Test
    fun `cannon cannot capture without platform`() {
        // Red cannon at e5, Black chariot at e0, no piece between them
        val state = engine.loadFen("k3r4/9/9/9/9/4C4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        assertFalse("Cannon must not capture e0 without platform", "e0" in targets)
        assertTrue("Cannon can still slide to e4", "e4" in targets)
    }

    @Test
    fun `cannon captures over exactly one platform`() {
        // Red cannon at e5, Red soldier (platform) at e3, Black chariot at e0
        val state = engine.loadFen("k3r4/9/9/4P4/9/4C4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        assertTrue("Cannon should capture e0 over platform at e3", "e0" in targets)
    }

    // ── Horse ────────────────────────────────────────────────────────────────

    @Test
    fun `horse has eight moves from open center`() {
        // Red horse at e5; Black general at a0 (off e-file), Red general at e9
        val state = engine.loadFen("k8/9/9/9/9/4H4/9/9/9/4K4 w - - 0 1")
        assertEquals("Horse in open center has 8 moves", 8, engine.getLegalMovesFromSquare(state, "e5").size)
    }

    @Test
    fun `horse leg blocked by own piece`() {
        // Red soldier at e4 blocks the upward leg from e5, preventing d3 and f3
        val state = engine.loadFen("k8/9/9/9/4P4/4H4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        assertFalse("d3 blocked by leg piece at e4", "d3" in targets)
        assertFalse("f3 blocked by leg piece at e4", "f3" in targets)
        assertTrue("Other horse moves still exist", targets.size >= 4)
    }

    // ── Elephant ─────────────────────────────────────────────────────────────

    @Test
    fun `red elephant cannot cross river to Black side`() {
        // Red elephant at e5; Black general at a0, Red general at e9
        val state = engine.loadFen("k8/9/9/9/9/4E4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        assertFalse("Red elephant cannot go to c3 (Black side)", "c3" in targets)
        assertFalse("Red elephant cannot go to g3 (Black side)", "g3" in targets)
        // Legal targets (both on Red side): c7, g7
        assertTrue("Red elephant can go to c7", "c7" in targets)
        assertTrue("Red elephant can go to g7", "g7" in targets)
    }

    @Test
    fun `elephant blocked by midpoint piece`() {
        // Red elephant at c7 trying to reach e5; midpoint d6 is occupied by Red soldier
        val state = engine.loadFen("k8/9/9/9/9/9/3P5/2E6/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "c7").map { it.toSquare }
        assertFalse("Elephant to e5 blocked by piece at d6", "e5" in targets)
    }

    // ── Advisor ──────────────────────────────────────────────────────────────

    @Test
    fun `advisor only moves diagonally within palace`() {
        // Red advisor at e9, Red general at d9, Black general at a0
        val state = engine.loadFen("k8/9/9/9/9/9/9/9/9/3KA4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e9").map { it.toSquare }
        assertTrue("Advisor should reach d8", "d8" in targets)
        assertTrue("Advisor should reach f8", "f8" in targets)
        assertFalse("Advisor must not leave palace", targets.any { sq ->
            sq[0] - 'a' !in 3..5 || sq[1].digitToInt() !in 7..9
        })
    }

    // ── General ──────────────────────────────────────────────────────────────

    @Test
    fun `general has four orthogonal moves from palace center`() {
        // Red general at e8 (center of palace); Black general at a0 (off e-file)
        val state = engine.loadFen("k8/9/9/9/9/9/9/9/4K4/9 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e8").map { it.toSquare }
        assertTrue("General reaches d8", "d8" in targets)
        assertTrue("General reaches f8", "f8" in targets)
        assertTrue("General reaches e7", "e7" in targets)
        assertTrue("General reaches e9", "e9" in targets)
    }

    @Test
    fun `general cannot leave palace`() {
        val state = engine.loadFen("k8/9/9/9/9/9/9/9/4K4/9 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e8").map { it.toSquare }
        assertFalse("General must not leave palace", targets.any { sq ->
            sq[0] - 'a' !in 3..5 || sq[1].digitToInt() !in 7..9
        })
    }

    // ── Soldier ──────────────────────────────────────────────────────────────

    @Test
    fun `red soldier before river can only move forward`() {
        // Red soldier at e6 (Red's own side, rank 6 >= 5 → not crossed)
        val state = engine.loadFen("k8/9/9/9/9/9/4P4/9/9/4K4 w - - 0 1")
        val moves = engine.getLegalMovesFromSquare(state, "e6")
        assertEquals("Pre-river soldier has exactly 1 move", 1, moves.size)
        assertEquals("Pre-river soldier moves forward to e5", "e5", moves[0].toSquare)
    }

    @Test
    fun `red soldier after crossing river moves forward and sideways`() {
        // Red soldier at e4 (Black's side, rank 4 < 5 → crossed); generals on different files
        val state = engine.loadFen("k8/9/9/9/4P4/9/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e4").map { it.toSquare }
        assertTrue("Crossed soldier moves forward to e3", "e3" in targets)
        assertTrue("Crossed soldier moves left to d4", "d4" in targets)
        assertTrue("Crossed soldier moves right to f4", "f4" in targets)
        assertEquals("Crossed soldier has exactly 3 moves", 3, targets.size)
    }

    // ── Flying General ───────────────────────────────────────────────────────

    @Test
    fun `flying general detected when generals face each other`() {
        // Both generals on e-file with nothing between them
        val state = engine.loadFen("4k4/9/9/9/9/9/9/9/9/4K4 b - - 0 1")
        assertTrue("Flying general: side to move is in check", engine.isInCheck(state))
    }

    @Test
    fun `move that exposes flying general is illegal`() {
        // Red chariot at e5 is the only blocker between both generals on file e
        val state = engine.loadFen("4k4/9/9/9/9/4R4/9/9/9/4K4 w - - 0 1")
        val targets = engine.getLegalMovesFromSquare(state, "e5").map { it.toSquare }
        // Moving off file e would expose flying general → all legal moves stay on file e
        assertFalse("Chariot must not move off e-file (creates flying general)",
            targets.any { it[0] != 'e' })
    }

    // ── isInCheck ────────────────────────────────────────────────────────────

    @Test
    fun `not in check in quiet position`() {
        val state = engine.loadFen("3k5/9/9/9/9/9/9/9/9/4K4 w - - 0 1")
        assertFalse("Red should not be in check in quiet position", engine.isInCheck(state))
    }

    @Test
    fun `chariot gives check on same file`() {
        // Black chariot at e5, Red general at e9, Black general at d0 (different file)
        val state = engine.loadFen("3k5/9/9/9/9/4r4/9/9/9/4K4 w - - 0 1")
        assertTrue("Red general should be in check from Black chariot at e5", engine.isInCheck(state))
    }

    // ── applyMove ────────────────────────────────────────────────────────────

    @Test
    fun `applyMove moves piece and clears origin`() {
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val next = engine.applyMove(state, "a6a5")
        assertNull("Origin a6 should be empty after move", next.pieceMap["a6"])
        assertNotNull("Destination a5 should hold the piece", next.pieceMap["a5"])
    }

    @Test
    fun `applyMove switches side to move`() {
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        assertEquals(XiangqiColor.BLACK, engine.applyMove(state, "a6a5").sideToMove)
    }

    @Test
    fun `applyMove preserves piece type`() {
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val piece = engine.applyMove(state, "a6a5").pieceMap["a5"]
        assertNotNull(piece)
        assertEquals(XiangqiPieceType.SOLDIER, piece!!.type)
        assertEquals(XiangqiColor.RED, piece.color)
    }

    // ── ValidateXiangqiMoveUseCase ───────────────────────────────────────────

    @Test
    fun `validateMove accepts correct solution move`() {
        val uc = ValidateXiangqiMoveUseCase(engine)
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val result = uc(state, "a6a5", listOf("a6a5"), moveIndex = 0)
        assertTrue(result.isCorrect)
    }

    @Test
    fun `validateMove rejects wrong move`() {
        val uc = ValidateXiangqiMoveUseCase(engine)
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val result = uc(state, "b6b5", listOf("a6a5"), moveIndex = 0)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `validateMove returns computer reply after correct move`() {
        val uc = ValidateXiangqiMoveUseCase(engine)
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val result = uc(state, "a6a5", listOf("a6a5", "e0d0"), moveIndex = 0)
        assertEquals("e0d0", result.computerReply)
    }

    @Test
    fun `validateMove returns no reply after wrong move`() {
        val uc = ValidateXiangqiMoveUseCase(engine)
        val state = engine.loadFen("4k4/9/9/9/9/9/P8/9/9/4K4 w - - 0 1")
        val result = uc(state, "b6b5", listOf("a6a5", "e0d0"), moveIndex = 0)
        assertNull(result.computerReply)
    }

    // ── pychess standard UCCI notation (n=horse, b=elephant) ────────────────

    @Test
    fun `pychess starting FEN with n and b notation has 32 pieces`() {
        // pychess uses standard UCCI: n=horse, b=elephant (NOT h/e)
        val state = engine.loadFen(PYCHESS_STARTING_FEN)
        assertEquals(32, state.pieceMap.size)
    }

    @Test
    fun `pychess FEN uppercase N parsed as Red Horse`() {
        val state = engine.loadFen(PYCHESS_STARTING_FEN)
        val redHorseB9 = state.pieceMap["b9"]
        assertNotNull(redHorseB9)
        assertEquals(XiangqiPieceType.HORSE, redHorseB9!!.type)
        assertEquals(XiangqiColor.RED, redHorseB9.color)
    }

    @Test
    fun `pychess FEN uppercase B parsed as Red Elephant`() {
        val state = engine.loadFen(PYCHESS_STARTING_FEN)
        val redElephantC9 = state.pieceMap["c9"]
        assertNotNull(redElephantC9)
        assertEquals(XiangqiPieceType.ELEPHANT, redElephantC9!!.type)
        assertEquals(XiangqiColor.RED, redElephantC9.color)
    }

    @Test
    fun `pychess FEN lowercase n parsed as Black Horse`() {
        val state = engine.loadFen(PYCHESS_STARTING_FEN)
        val blackHorseB0 = state.pieceMap["b0"]
        assertNotNull(blackHorseB0)
        assertEquals(XiangqiPieceType.HORSE, blackHorseB0!!.type)
        assertEquals(XiangqiColor.BLACK, blackHorseB0.color)
    }

    @Test
    fun `pychess FEN lowercase b parsed as Black Elephant`() {
        val state = engine.loadFen(PYCHESS_STARTING_FEN)
        val blackElephantC0 = state.pieceMap["c0"]
        assertNotNull(blackElephantC0)
        assertEquals(XiangqiPieceType.ELEPHANT, blackElephantC0!!.type)
        assertEquals(XiangqiColor.BLACK, blackElephantC0.color)
    }

    @Test
    fun `first move is legal in real pychess puzzle UaELz`() {
        // Real pychess puzzle — would show firstMoveLegal=false before the FEN parsing fix
        val state = engine.loadFen("2baka1RC/7R1/6r2/p3p3p/2pn2b2/5p3/P1P1c3P/2r1N4/4A4/2BAK1B2 w - - 0 1")
        assertTrue("h1f1 must be legal in puzzle UaELz", engine.isMoveLegal(state, "h1f1"))
    }

    companion object {
        // Internal FEN notation (h=horse, e=elephant)
        private const val STARTING_FEN =
            "rheakaehr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RHEAKAEHR w - - 0 1"

        // pychess/standard UCCI notation (n=horse, b=elephant)
        private const val PYCHESS_STARTING_FEN =
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1"
    }
}
