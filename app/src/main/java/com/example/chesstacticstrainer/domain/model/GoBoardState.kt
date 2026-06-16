package com.example.chesstacticstrainer.domain.model

/**
 * Immutable snapshot of a Go board.
 * Only BLACK/WHITE entries appear in [stones]; empty intersections are implicit.
 */
data class GoBoardState(
    val boardSize: Int,
    val stones: Map<GoPoint, GoStone> = emptyMap(),
    val koPoint: GoPoint? = null,
    val capturedByBlack: Int = 0,
    val capturedByWhite: Int = 0
)
