package com.example.chesstacticstrainer.engine

import com.example.chesstacticstrainer.domain.engine.AnimalChessEngine
import com.example.chesstacticstrainer.domain.model.AnimalChessBoardState
import com.example.chesstacticstrainer.domain.model.AnimalChessMove
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalColor.BLUE
import com.example.chesstacticstrainer.domain.model.AnimalColor.RED
import com.example.chesstacticstrainer.domain.model.AnimalPiece
import com.example.chesstacticstrainer.domain.model.AnimalType
import com.example.chesstacticstrainer.domain.model.AnimalType.CAT
import com.example.chesstacticstrainer.domain.model.AnimalType.DOG
import com.example.chesstacticstrainer.domain.model.AnimalType.ELEPHANT
import com.example.chesstacticstrainer.domain.model.AnimalType.LEOPARD
import com.example.chesstacticstrainer.domain.model.AnimalType.LION
import com.example.chesstacticstrainer.domain.model.AnimalType.MOUSE
import com.example.chesstacticstrainer.domain.model.AnimalType.TIGER
import com.example.chesstacticstrainer.domain.model.AnimalType.WOLF

/**
 * Pure Kotlin Animal Chess (Dou Shou Qi / 斗兽棋) engine.
 *
 * Board coordinate system:
 *   Columns a–g → indices 0–6 (left to right)
 *   Rows 0–8    → 0 = Blue back row (top), 8 = Red back row (bottom)
 *   Square strings: "a0"–"g8" (always 2 characters)
 *
 * FEN format: 9 rows separated by '/', side-to-move 'r'/'b'
 * Uppercase = Red, lowercase = Blue.
 *
 * Piece letters:
 *   E/e = Elephant (rank 8), L/l = Lion (rank 7), T/t = Tiger (rank 6)
 *   P/p = Leopard (rank 5), W/w = Wolf (rank 4), D/d = Dog (rank 3)
 *   C/c = Cat (rank 2), M/m = Mouse (rank 1)
 */
class AnimalChessEngineImpl : AnimalChessEngine {

    // ── Public API ──────────────────────────────────────────────────────────

    override fun loadFen(fen: String): AnimalChessBoardState {
        val parts = fen.trim().split("\\s+".toRegex())
        val rows = parts[0].split("/")
        val side = if (parts.getOrNull(1) == "b") BLUE else RED
        val map = mutableMapOf<String, AnimalPiece>()
        rows.forEachIndexed { rowIdx, row ->
            var colIdx = 0
            for (ch in row) {
                if (ch.isDigit()) {
                    colIdx += ch.digitToInt()
                } else {
                    charToPiece(ch)?.let { map[sq(colIdx, rowIdx)] = it }
                    colIdx++
                }
            }
        }
        return AnimalChessBoardState(fen, map.toMap(), side)
    }

    override fun applyMove(state: AnimalChessBoardState, uciMove: String): AnimalChessBoardState {
        if (uciMove.length < 4) return state
        val from = uciMove.substring(0, 2)
        val to = uciMove.substring(2, 4)
        val map = state.pieceMap.toMutableMap()
        val piece = map.remove(from) ?: return state
        map[to] = piece
        val next = state.sideToMove.opposite()
        val newFen = buildFen(map, next)
        return AnimalChessBoardState(newFen, map.toMap(), next)
    }

    override fun getLegalMovesFromSquare(state: AnimalChessBoardState, square: String): List<AnimalChessMove> {
        val piece = state.pieceMap[square] ?: return emptyList()
        if (piece.color != state.sideToMove) return emptyList()
        return generateMoves(state.pieceMap, square, piece)
            .map { uci -> AnimalChessMove(uci.substring(0, 2), uci.substring(2, 4)) }
    }

    override fun isMoveLegal(state: AnimalChessBoardState, uciMove: String): Boolean =
        getLegalMovesFromSquare(state, uciMove.take(2)).any { it.uci == uciMove }

    override fun isGameOver(state: AnimalChessBoardState): Boolean {
        // Check if any piece is on the enemy den (win condition)
        val redPieceOnBlueDen = state.pieceMap[BLUE_DEN]?.color == RED
        val bluePieceOnRedDen = state.pieceMap[RED_DEN]?.color == BLUE
        if (redPieceOnBlueDen || bluePieceOnRedDen) return true

        // Check if either side has no pieces
        val redPieces = state.pieceMap.values.count { it.color == RED }
        val bluePieces = state.pieceMap.values.count { it.color == BLUE }
        return redPieces == 0 || bluePieces == 0
    }

    override fun sideToMove(state: AnimalChessBoardState): AnimalColor = state.sideToMove

    override fun pieceAt(state: AnimalChessBoardState, square: String): AnimalPiece? =
        state.pieceMap[square]

    // ── Move generation ─────────────────────────────────────────────────────

    /**
     * Generates all legal UCI strings for the given piece at the given square.
     * Returns a list of 4-char strings "fromto".
     */
    private fun generateMoves(
        map: Map<String, AnimalPiece>,
        fromSq: String,
        piece: AnimalPiece
    ): List<String> {
        val col = col(fromSq)
        val row = row(fromSq)
        val result = mutableListOf<String>()

        // Standard orthogonal moves (one step)
        for ((dc, dr) in ORTHO) {
            val tc = col + dc
            val tr = row + dr
            if (!valid(tc, tr)) continue
            val target = sq(tc, tr)

            // No piece can enter its own den
            if (isOwnDen(target, piece.color)) continue

            // Non-mouse pieces cannot enter water
            if (WATER_SQUARES.contains(target) && piece.type != MOUSE) continue

            // Check capture legality
            val occupant = map[target]
            if (occupant != null) {
                if (occupant.color == piece.color) continue // can't capture own
                if (!canCapture(piece, occupant, target)) continue
            }

            result.add("$fromSq$target")
        }

        // Special moves for Lion and Tiger: water jumps
        if (piece.type == LION || piece.type == TIGER) {
            for ((dc, dr) in ORTHO) {
                val jumpTarget = findWaterJumpTarget(map, col, row, dc, dr) ?: continue
                val target = jumpTarget
                if (isOwnDen(target, piece.color)) continue
                val occupant = map[target]
                if (occupant != null) {
                    if (occupant.color == piece.color) continue
                    if (!canCapture(piece, occupant, target)) continue
                }
                result.add("$fromSq$target")
            }
        }

        return result
    }

    /**
     * Finds the landing square for a water jump from (col, row) in direction (dc, dr).
     * Returns null if no jump is possible (no water lane, or blocked by Mouse).
     */
    private fun findWaterJumpTarget(
        map: Map<String, AnimalPiece>,
        col: Int,
        row: Int,
        dc: Int,
        dr: Int
    ): String? {
        // The first step must land on water
        val firstC = col + dc
        val firstR = row + dr
        if (!valid(firstC, firstR)) return null
        val firstSq = sq(firstC, firstR)
        if (!WATER_SQUARES.contains(firstSq)) return null

        // Traverse all contiguous water squares in this direction
        val waterSquares = mutableListOf<String>()
        var tc = firstC
        var tr = firstR
        while (valid(tc, tr) && WATER_SQUARES.contains(sq(tc, tr))) {
            waterSquares.add(sq(tc, tr))
            tc += dc
            tr += dr
        }

        // The square after the water lane must be valid and non-water
        if (!valid(tc, tr)) return null
        val landingSq = sq(tc, tr)
        if (WATER_SQUARES.contains(landingSq)) return null

        // Jump is blocked if any Mouse is in the water squares being jumped over
        for (waterSq in waterSquares) {
            val occupant = map[waterSq]
            if (occupant != null && occupant.type == MOUSE) return null
        }

        return landingSq
    }

    /**
     * Determines if an attacker piece can capture a defender piece on targetSquare.
     *
     * Rules:
     * - Piece on enemy trap has effective rank 0
     * - Normally: attacker.rank >= defender.rank
     * - Exception: Mouse captures Elephant (rank 1 captures rank 8)
     * - Exception: Elephant CANNOT capture Mouse (despite having higher rank)
     */
    private fun canCapture(attacker: AnimalPiece, defender: AnimalPiece, targetSquare: String): Boolean {
        // Determine effective defender rank (trap reduces rank to 0)
        val defenderTrapped = isEnemyTrap(targetSquare, attacker.color)
        val effectiveDefenderRank = if (defenderTrapped) 0 else defender.type.rank

        // Mouse vs Elephant special rule
        if (attacker.type == MOUSE && defender.type == ELEPHANT) return true
        if (attacker.type == ELEPHANT && defender.type == MOUSE && !defenderTrapped) return false

        return attacker.type.rank >= effectiveDefenderRank
    }

    /**
     * Returns true if the given square is an enemy trap from the attacker's perspective.
     * Attacker = RED → enemy traps are BLUE_TRAPS.
     * Attacker = BLUE → enemy traps are RED_TRAPS.
     */
    // A defender is weakened when standing on the ATTACKER's own traps (i.e., traps on the attacker's side).
    // Red's traps weaken Blue pieces; Blue's traps weaken Red pieces.
    private fun isEnemyTrap(square: String, attackerColor: AnimalColor): Boolean =
        if (attackerColor == RED) RED_TRAPS.contains(square) else BLUE_TRAPS.contains(square)

    /**
     * Returns true if entering this square would be entering the piece's own den.
     */
    private fun isOwnDen(square: String, color: AnimalColor): Boolean =
        (color == RED && square == RED_DEN) || (color == BLUE && square == BLUE_DEN)

    // ── FEN serialization ───────────────────────────────────────────────────

    private fun buildFen(map: Map<String, AnimalPiece>, side: AnimalColor): String {
        val rows = (0..8).joinToString("/") { row ->
            val sb = StringBuilder()
            var empty = 0
            for (col in 0..6) {
                val p = map[sq(col, row)]
                if (p == null) {
                    empty++
                } else {
                    if (empty > 0) {
                        sb.append(empty)
                        empty = 0
                    }
                    sb.append(p.toFenChar())
                }
            }
            if (empty > 0) sb.append(empty)
            sb.toString()
        }
        return "$rows ${if (side == RED) "r" else "b"}"
    }

    // ── Piece letter mapping ─────────────────────────────────────────────────

    private fun charToPiece(c: Char): AnimalPiece? {
        val color = if (c.isUpperCase()) RED else BLUE
        val type = when (c.lowercaseChar()) {
            'e' -> ELEPHANT
            'l' -> LION
            't' -> TIGER
            'p' -> LEOPARD
            'w' -> WOLF
            'd' -> DOG
            'c' -> CAT
            'm' -> MOUSE
            else -> return null
        }
        return AnimalPiece(type, color)
    }

    private fun AnimalPiece.toFenChar(): Char {
        val c = when (type) {
            ELEPHANT -> 'e'
            LION     -> 'l'
            TIGER    -> 't'
            LEOPARD  -> 'p'
            WOLF     -> 'w'
            DOG      -> 'd'
            CAT      -> 'c'
            MOUSE    -> 'm'
        }
        return if (color == RED) c.uppercaseChar() else c
    }

    // ── Coordinate helpers ───────────────────────────────────────────────────

    private fun sq(col: Int, row: Int): String = "${'a' + col}$row"
    private fun col(sq: String): Int = sq[0] - 'a'
    private fun row(sq: String): Int = sq[1].digitToInt()
    private fun valid(c: Int, r: Int): Boolean = c in 0..6 && r in 0..8

    // ── Constants ────────────────────────────────────────────────────────────

    companion object {
        private val ORTHO = listOf(0 to -1, 0 to 1, -1 to 0, 1 to 0)

        val WATER_SQUARES: Set<String> = setOf(
            "b3", "b4", "b5", "c3", "c4", "c5",
            "e3", "e4", "e5", "f3", "f4", "f5"
        )

        const val BLUE_DEN = "d0"
        const val RED_DEN = "d8"

        val BLUE_TRAPS: Set<String> = setOf("c0", "e0", "d1")
        val RED_TRAPS: Set<String> = setOf("c8", "e8", "d7")

        const val STARTING_FEN = "l5t/1d3c1/m1p1w1e/7/7/7/E1W1P1M/1C3D1/T5L r"
    }
}
