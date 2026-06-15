package com.example.chesstacticstrainer.domain.engine

import com.example.chesstacticstrainer.domain.model.XiangqiBoardState
import com.example.chesstacticstrainer.domain.model.XiangqiColor
import com.example.chesstacticstrainer.domain.model.XiangqiMove
import com.example.chesstacticstrainer.domain.model.XiangqiPiece

interface XiangqiEngine {
    fun loadFen(fen: String): XiangqiBoardState
    fun applyMove(state: XiangqiBoardState, ucciMove: String): XiangqiBoardState
    fun getLegalMovesFromSquare(state: XiangqiBoardState, square: String): List<XiangqiMove>
    fun isMoveLegal(state: XiangqiBoardState, ucciMove: String): Boolean
    fun isInCheck(state: XiangqiBoardState): Boolean
    fun isCheckmate(state: XiangqiBoardState): Boolean
    fun isStalemate(state: XiangqiBoardState): Boolean
    fun sideToMove(state: XiangqiBoardState): XiangqiColor
    fun pieceAt(state: XiangqiBoardState, square: String): XiangqiPiece?
}
