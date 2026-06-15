package com.example.chesstacticstrainer.engine

import com.example.chesstacticstrainer.domain.engine.XiangqiEngine
import com.example.chesstacticstrainer.domain.model.XiangqiBoardState
import com.example.chesstacticstrainer.domain.model.XiangqiColor
import com.example.chesstacticstrainer.domain.model.XiangqiColor.BLACK
import com.example.chesstacticstrainer.domain.model.XiangqiColor.RED
import com.example.chesstacticstrainer.domain.model.XiangqiMove
import com.example.chesstacticstrainer.domain.model.XiangqiPiece
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.ADVISOR
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.CANNON
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.CHARIOT
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.ELEPHANT
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.GENERAL
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.HORSE
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType.SOLDIER

/**
 * Pure Kotlin Xiangqi (Chinese Chess) engine.
 *
 * Board coordinate system (UCCI):
 *   Files a–i  → indices 0–8 (left to right from Red's perspective)
 *   Ranks 0–9  → 0 = Black's back row (top), 9 = Red's back row (bottom)
 *   Square strings: "a0"–"i9"  (always 2 characters)
 *
 * FEN format: rows top-to-bottom separated by '/', side-to-move 'w'/'b'.
 * Piece letters: r/R=chariot  h/H=horse  e/E=elephant  a/A=advisor
 *                k/K=general  c/C=cannon  p/P=soldier
 * Uppercase = Red, lowercase = Black.
 */
class XiangqiEngineImpl : XiangqiEngine {

    // ── Public API ──────────────────────────────────────────────────────────

    override fun loadFen(fen: String): XiangqiBoardState {
        val parts = fen.trim().split("\\s+".toRegex())
        val rows = parts[0].split("/")
        val side = if (parts.getOrNull(1) == "b") BLACK else RED
        val map = mutableMapOf<String, XiangqiPiece>()
        rows.forEachIndexed { rankIdx, row ->
            var fileIdx = 0
            for (ch in row) {
                if (ch.isDigit()) { fileIdx += ch.digitToInt() }
                else { charToPiece(ch)?.let { map[sq(fileIdx, rankIdx)] = it }; fileIdx++ }
            }
        }
        return XiangqiBoardState(fen, map.toMap(), side)
    }

    override fun applyMove(state: XiangqiBoardState, ucciMove: String): XiangqiBoardState {
        if (ucciMove.length < 4) return state
        val from = ucciMove.substring(0, 2)
        val to   = ucciMove.substring(2, 4)
        val map  = state.pieceMap.toMutableMap()
        val piece = map.remove(from) ?: return state
        map[to] = piece
        val next = state.sideToMove.opposite()
        val newFen = buildFen(map, next)
        return XiangqiBoardState(newFen, map.toMap(), next)
    }

    override fun getLegalMovesFromSquare(state: XiangqiBoardState, square: String): List<XiangqiMove> {
        val piece = state.pieceMap[square] ?: return emptyList()
        if (piece.color != state.sideToMove) return emptyList()
        val mover = state.sideToMove
        return pseudoLegal(state.pieceMap, square, piece).filter { uci ->
            val after = applyMove(state, uci)
            !inCheckForColor(after.pieceMap, mover)
        }.map { uci -> XiangqiMove(uci.substring(0, 2), uci.substring(2, 4)) }
    }

    override fun isMoveLegal(state: XiangqiBoardState, ucciMove: String): Boolean =
        getLegalMovesFromSquare(state, ucciMove.take(2)).any { it.uci == ucciMove }

    override fun isInCheck(state: XiangqiBoardState): Boolean =
        inCheckForColor(state.pieceMap, state.sideToMove)

    override fun isCheckmate(state: XiangqiBoardState): Boolean =
        isInCheck(state) && hasNoLegalMoves(state)

    override fun isStalemate(state: XiangqiBoardState): Boolean =
        !isInCheck(state) && hasNoLegalMoves(state)

    override fun sideToMove(state: XiangqiBoardState): XiangqiColor = state.sideToMove

    override fun pieceAt(state: XiangqiBoardState, square: String): XiangqiPiece? =
        state.pieceMap[square]

    // ── Internal check helpers ───────────────────────────────────────────────

    private fun inCheckForColor(map: Map<String, XiangqiPiece>, color: XiangqiColor): Boolean {
        val genSq = map.entries.find { it.value.type == GENERAL && it.value.color == color }?.key
            ?: return false
        if (flyingGeneral(map)) return true
        val opp = color.opposite()
        return map.entries.any { (sq, p) ->
            p.color == opp && pseudoLegal(map, sq, p).any { it.substring(2) == genSq }
        }
    }

    private fun flyingGeneral(map: Map<String, XiangqiPiece>): Boolean {
        val redSq = map.entries.find { it.value.type == GENERAL && it.value.color == RED }?.key ?: return false
        val blkSq = map.entries.find { it.value.type == GENERAL && it.value.color == BLACK }?.key ?: return false
        if (file(redSq) != file(blkSq)) return false
        val f = file(redSq)
        val rMin = minOf(rank(redSq), rank(blkSq)) + 1
        val rMax = maxOf(rank(redSq), rank(blkSq)) - 1
        return (rMin..rMax).none { r -> map.containsKey(sq(f, r)) }
    }

    private fun hasNoLegalMoves(state: XiangqiBoardState): Boolean =
        state.pieceMap.entries
            .filter { it.value.color == state.sideToMove }
            .all { (sq, _) -> getLegalMovesFromSquare(state, sq).isEmpty() }

    // ── Pseudo-legal move generators ────────────────────────────────────────

    /** Returns a list of UCI strings (4-char "fromto") for all pseudo-legal moves. */
    private fun pseudoLegal(map: Map<String, XiangqiPiece>, fromSq: String, piece: XiangqiPiece): List<String> {
        val f = file(fromSq); val r = rank(fromSq); val c = piece.color
        val targets: List<Pair<Int, Int>> = when (piece.type) {
            CHARIOT  -> chariotTargets(map, f, r, c)
            CANNON   -> cannonTargets(map, f, r, c)
            HORSE    -> horseTargets(map, f, r, c)
            ELEPHANT -> elephantTargets(map, f, r, c)
            ADVISOR  -> advisorTargets(map, f, r, c)
            GENERAL  -> generalTargets(map, f, r, c)
            SOLDIER  -> soldierTargets(map, f, r, c)
        }
        return targets.map { (tf, tr) -> "$fromSq${sq(tf, tr)}" }
    }

    private fun chariotTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((df, dr) in ORTHO) {
            var tf = f + df; var tr = r + dr
            while (valid(tf, tr)) {
                val t = map[sq(tf, tr)]
                if (t == null) result.add(tf to tr)
                else { if (t.color != c) result.add(tf to tr); break }
                tf += df; tr += dr
            }
        }
        return result
    }

    private fun cannonTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((df, dr) in ORTHO) {
            var tf = f + df; var tr = r + dr
            var platform = false
            while (valid(tf, tr)) {
                val t = map[sq(tf, tr)]
                if (!platform) {
                    if (t == null) result.add(tf to tr)
                    else platform = true
                } else {
                    if (t != null) { if (t.color != c) result.add(tf to tr); break }
                }
                tf += df; tr += dr
            }
        }
        return result
    }

    private fun horseTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((legDf, legDr, tOffsets) in HORSE_LEGS) {
            val legF = f + legDf; val legR = r + legDr
            if (!valid(legF, legR) || map.containsKey(sq(legF, legR))) continue
            for ((df, dr) in tOffsets) {
                val tf = f + df; val tr = r + dr
                if (!valid(tf, tr)) continue
                val t = map[sq(tf, tr)]
                if (t == null || t.color != c) result.add(tf to tr)
            }
        }
        return result
    }

    private fun elephantTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((df, dr) in DIAG_2) {
            val midF = f + df / 2; val midR = r + dr / 2
            val tf = f + df; val tr = r + dr
            if (!valid(tf, tr)) continue
            if (map.containsKey(sq(midF, midR))) continue
            val ownHalf = if (c == RED) tr in 5..9 else tr in 0..4
            if (!ownHalf) continue
            val t = map[sq(tf, tr)]
            if (t == null || t.color != c) result.add(tf to tr)
        }
        return result
    }

    private fun advisorTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((df, dr) in DIAG_1) {
            val tf = f + df; val tr = r + dr
            if (!valid(tf, tr) || tf !in 3..5) continue
            if (c == RED && tr !in 7..9) continue
            if (c == BLACK && tr !in 0..2) continue
            val t = map[sq(tf, tr)]
            if (t == null || t.color != c) result.add(tf to tr)
        }
        return result
    }

    private fun generalTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for ((df, dr) in ORTHO) {
            val tf = f + df; val tr = r + dr
            if (!valid(tf, tr) || tf !in 3..5) continue
            if (c == RED && tr !in 7..9) continue
            if (c == BLACK && tr !in 0..2) continue
            val t = map[sq(tf, tr)]
            if (t == null || t.color != c) result.add(tf to tr)
        }
        return result
    }

    private fun soldierTargets(map: Map<String, XiangqiPiece>, f: Int, r: Int, c: XiangqiColor): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val fwd = if (c == RED) -1 else 1
        val crossed = if (c == RED) r < 5 else r > 4
        // Forward
        val fwdR = r + fwd
        if (valid(f, fwdR)) {
            val t = map[sq(f, fwdR)]
            if (t == null || t.color != c) result.add(f to fwdR)
        }
        // Sideways (only after crossing the river)
        if (crossed) {
            for (df in listOf(-1, 1)) {
                val tf = f + df
                if (valid(tf, r)) {
                    val t = map[sq(tf, r)]
                    if (t == null || t.color != c) result.add(tf to r)
                }
            }
        }
        return result
    }

    // ── FEN serialisation ───────────────────────────────────────────────────

    private fun buildFen(map: Map<String, XiangqiPiece>, side: XiangqiColor): String {
        val rows = (0..9).joinToString("/") { rank ->
            val sb = StringBuilder(); var empty = 0
            for (file in 0..8) {
                val p = map[sq(file, rank)]
                if (p == null) empty++
                else { if (empty > 0) { sb.append(empty); empty = 0 }; sb.append(p.toFenChar()) }
            }
            if (empty > 0) sb.append(empty)
            sb.toString()
        }
        return "$rows ${if (side == RED) "w" else "b"} - - 0 1"
    }

    // ── Piece letter mapping ─────────────────────────────────────────────────

    private fun charToPiece(c: Char): XiangqiPiece? {
        val color = if (c.isUpperCase()) RED else BLACK
        val type = when (c.lowercaseChar()) {
            'r' -> CHARIOT
            'h', 'n' -> HORSE      // 'n' = standard UCCI (pychess); 'h' = internal
            'e', 'b' -> ELEPHANT   // 'b' = standard UCCI (pychess); 'e' = internal
            'a' -> ADVISOR; 'k' -> GENERAL; 'c' -> CANNON; 'p' -> SOLDIER
            else -> return null
        }
        return XiangqiPiece(type, color)
    }

    private fun XiangqiPiece.toFenChar(): Char {
        val c = when (type) {
            CHARIOT -> 'r'; HORSE -> 'h'; ELEPHANT -> 'e'
            ADVISOR -> 'a'; GENERAL -> 'k'; CANNON -> 'c'; SOLDIER -> 'p'
        }
        return if (color == RED) c.uppercaseChar() else c
    }

    // ── Coordinate helpers ───────────────────────────────────────────────────

    private fun sq(file: Int, rank: Int): String = "${'a' + file}$rank"
    private fun file(sq: String): Int = sq[0] - 'a'
    private fun rank(sq: String): Int = sq[1].digitToInt()
    private fun valid(f: Int, r: Int): Boolean = f in 0..8 && r in 0..9

    // ── Direction tables ─────────────────────────────────────────────────────

    companion object {
        private val ORTHO  = listOf(0 to -1, 0 to 1, -1 to 0, 1 to 0)
        private val DIAG_1 = listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)
        private val DIAG_2 = listOf(-2 to -2, 2 to -2, -2 to 2, 2 to 2)

        // Each entry: (legFileDelta, legRankDelta, list-of-target-offsets-from-origin)
        private data class HorseLeg(val df: Int, val dr: Int, val targets: List<Pair<Int, Int>>)
        private val HORSE_LEGS = listOf(
            HorseLeg(0, -1, listOf(-1 to -2,  1 to -2)),
            HorseLeg(0,  1, listOf(-1 to  2,  1 to  2)),
            HorseLeg(-1, 0, listOf(-2 to -1, -2 to  1)),
            HorseLeg(1,  0, listOf( 2 to -1,  2 to  1))
        )
    }
}
