package com.example.chesstacticstrainer.engine

import com.example.chesstacticstrainer.domain.engine.GoEngine
import com.example.chesstacticstrainer.domain.engine.PlacementResult
import com.example.chesstacticstrainer.domain.model.GoBoardState
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoStone

class GoEngineImpl : GoEngine {

    override fun placeStone(state: GoBoardState, point: GoPoint, color: GoStone): PlacementResult {
        if (state.stones.containsKey(point)) return PlacementResult.Illegal
        if (state.koPoint == point) return PlacementResult.Illegal

        val mutableStones = state.stones.toMutableMap()
        mutableStones[point] = color

        val opponent = color.opposite()
        var capturedCount = 0
        val capturedPoints = mutableSetOf<GoPoint>()

        for (nb in neighbors(point, state.boardSize)) {
            if (mutableStones[nb] == opponent) {
                val group = getGroup(mutableStones, nb, state.boardSize)
                if (getLiberties(mutableStones, group, state.boardSize).isEmpty()) {
                    capturedCount += group.size
                    capturedPoints += group
                    group.forEach { mutableStones.remove(it) }
                }
            }
        }

        val ownGroup = getGroup(mutableStones, point, state.boardSize)
        if (getLiberties(mutableStones, ownGroup, state.boardSize).isEmpty()) {
            return PlacementResult.Illegal
        }

        val ownLiberties = getLiberties(mutableStones, ownGroup, state.boardSize)
        val newKoPoint = if (capturedCount == 1 && ownGroup.size == 1 && ownLiberties.size == 1) {
            capturedPoints.first()
        } else null

        val newState = state.copy(
            stones          = mutableStones.toMap(),
            koPoint         = newKoPoint,
            capturedByBlack = if (color == GoStone.BLACK) state.capturedByBlack + capturedCount else state.capturedByBlack,
            capturedByWhite = if (color == GoStone.WHITE) state.capturedByWhite + capturedCount else state.capturedByWhite
        )
        return PlacementResult.Success(newState)
    }

    override fun isLegal(state: GoBoardState, point: GoPoint, color: GoStone): Boolean =
        placeStone(state, point, color) is PlacementResult.Success

    // ── Group and liberty helpers ─────────────────────────────────────────────

    private fun getGroup(stones: Map<GoPoint, GoStone>, start: GoPoint, boardSize: Int): Set<GoPoint> {
        val color = stones[start] ?: return emptySet()
        val group = mutableSetOf(start)
        val queue = ArrayDeque<GoPoint>().also { it.add(start) }
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            for (nb in neighbors(cur, boardSize)) {
                if (nb !in group && stones[nb] == color) {
                    group += nb; queue += nb
                }
            }
        }
        return group
    }

    private fun getLiberties(
        stones: Map<GoPoint, GoStone>, group: Set<GoPoint>, boardSize: Int
    ): Set<GoPoint> = group.flatMap { p ->
        neighbors(p, boardSize).filter { it !in stones }
    }.toSet()

    private fun neighbors(p: GoPoint, boardSize: Int): List<GoPoint> = listOf(
        GoPoint(p.col - 1, p.row), GoPoint(p.col + 1, p.row),
        GoPoint(p.col, p.row - 1), GoPoint(p.col, p.row + 1)
    ).filter { it.col in 0 until boardSize && it.row in 0 until boardSize }
}
