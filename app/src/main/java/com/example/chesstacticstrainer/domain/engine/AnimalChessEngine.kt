package com.example.chesstacticstrainer.domain.engine

import com.example.chesstacticstrainer.domain.model.AnimalChessBoardState
import com.example.chesstacticstrainer.domain.model.AnimalChessMove
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalPiece

interface AnimalChessEngine {
    fun loadFen(fen: String): AnimalChessBoardState
    fun applyMove(state: AnimalChessBoardState, uciMove: String): AnimalChessBoardState
    fun getLegalMovesFromSquare(state: AnimalChessBoardState, square: String): List<AnimalChessMove>
    fun isMoveLegal(state: AnimalChessBoardState, uciMove: String): Boolean
    fun isGameOver(state: AnimalChessBoardState): Boolean
    fun sideToMove(state: AnimalChessBoardState): AnimalColor
    fun pieceAt(state: AnimalChessBoardState, square: String): AnimalPiece?
}
