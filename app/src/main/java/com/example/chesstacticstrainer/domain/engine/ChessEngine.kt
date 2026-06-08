package com.example.chesstacticstrainer.domain.engine

import com.example.chesstacticstrainer.domain.model.BoardState
import com.example.chesstacticstrainer.domain.model.ChessMove
import com.example.chesstacticstrainer.domain.model.Piece

interface ChessEngine {
    fun loadFen(fen: String): BoardState
    fun applyMove(state: BoardState, uciMove: String): BoardState
    fun getLegalMoves(state: BoardState): List<ChessMove>
    fun isMoveLegal(state: BoardState, uciMove: String): Boolean
    fun isInCheck(state: BoardState): Boolean
    fun isCheckmate(state: BoardState): Boolean
    fun isStalemate(state: BoardState): Boolean
    fun pieceAt(state: BoardState, square: String): Piece?
    fun getLegalMovesFromSquare(state: BoardState, square: String): List<ChessMove>
    fun sideToMove(state: BoardState): String
}
