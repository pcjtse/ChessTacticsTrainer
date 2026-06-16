package com.example.chesstacticstrainer.domain.engine

import com.example.chesstacticstrainer.domain.model.GoBoardState
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoStone

sealed class PlacementResult {
    data class Success(val newState: GoBoardState) : PlacementResult()
    data object Illegal : PlacementResult()
}

interface GoEngine {
    fun placeStone(state: GoBoardState, point: GoPoint, color: GoStone): PlacementResult
    fun isLegal(state: GoBoardState, point: GoPoint, color: GoStone): Boolean
}
