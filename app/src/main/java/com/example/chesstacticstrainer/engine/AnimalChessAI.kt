package com.example.chesstacticstrainer.engine

import com.example.chesstacticstrainer.domain.engine.AnimalChessEngine
import com.example.chesstacticstrainer.domain.model.AnimalChessBoardState
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalType

class AnimalChessAI(private val engine: AnimalChessEngine) {

    /**
     * Returns the best UCI move string for the current side to move, searching to [depth] plies.
     * Returns null if no moves available (game over).
     */
    fun bestMove(state: AnimalChessBoardState, depth: Int): String? {
        val moves = allLegalMoves(state)
        if (moves.isEmpty()) return null
        if (moves.size == 1) return moves.first()

        val maximizing = state.sideToMove
        var bestScore = Int.MIN_VALUE
        var bestMove: String? = null
        var alpha = Int.MIN_VALUE
        val beta = Int.MAX_VALUE

        for (move in moves.shuffled()) {          // shuffle for variety at equal scores
            val next = engine.applyMove(state, move)
            val score = minimax(next, depth - 1, alpha, beta, false, maximizing)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
            alpha = maxOf(alpha, bestScore)
        }
        return bestMove
    }

    private fun minimax(
        state: AnimalChessBoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        maximizingColor: AnimalColor
    ): Int {
        if (depth == 0 || engine.isGameOver(state)) {
            return evaluate(state, maximizingColor)
        }

        val moves = allLegalMoves(state)
        if (moves.isEmpty()) return evaluate(state, maximizingColor)

        return if (isMaximizing) {
            var value = Int.MIN_VALUE
            var a = alpha
            for (move in moves) {
                val next = engine.applyMove(state, move)
                value = maxOf(value, minimax(next, depth - 1, a, beta, false, maximizingColor))
                a = maxOf(a, value)
                if (value >= beta) break
            }
            value
        } else {
            var value = Int.MAX_VALUE
            var b = beta
            for (move in moves) {
                val next = engine.applyMove(state, move)
                value = minOf(value, minimax(next, depth - 1, alpha, b, true, maximizingColor))
                b = minOf(b, value)
                if (value <= alpha) break
            }
            value
        }
    }

    private fun evaluate(state: AnimalChessBoardState, maximizingColor: AnimalColor): Int {
        // Terminal states
        if (engine.isGameOver(state)) {
            val redOnBlueDen  = state.pieceMap[AnimalChessEngineImpl.BLUE_DEN]?.color == AnimalColor.RED
            val blueOnRedDen  = state.pieceMap[AnimalChessEngineImpl.RED_DEN]?.color == AnimalColor.BLUE
            val redPieces     = state.pieceMap.values.count { it.color == AnimalColor.RED }
            val bluePieces    = state.pieceMap.values.count { it.color == AnimalColor.BLUE }

            return when {
                redOnBlueDen  && maximizingColor == AnimalColor.RED  -> 1_000_000
                redOnBlueDen  && maximizingColor == AnimalColor.BLUE -> -1_000_000
                blueOnRedDen  && maximizingColor == AnimalColor.BLUE -> 1_000_000
                blueOnRedDen  && maximizingColor == AnimalColor.RED  -> -1_000_000
                bluePieces == 0 && maximizingColor == AnimalColor.RED  -> 1_000_000
                bluePieces == 0 && maximizingColor == AnimalColor.BLUE -> -1_000_000
                redPieces  == 0 && maximizingColor == AnimalColor.BLUE -> 1_000_000
                redPieces  == 0 && maximizingColor == AnimalColor.RED  -> -1_000_000
                else -> 0
            }
        }

        var score = 0
        for ((sq, piece) in state.pieceMap) {
            val base = PIECE_VALUE[piece.type] ?: 0
            // Distance bonus: reward pieces closer to enemy den
            val denRow = if (piece.color == AnimalColor.RED) 0 else 8
            val col    = sq[0] - 'a'
            val row    = sq[1].digitToInt()
            val distToDen = Math.abs(row - denRow) + Math.abs(col - 3)
            val advance   = (12 - distToDen) * 3     // max bonus ~36 for pieces near den

            val contribution = base + advance
            score += if (piece.color == maximizingColor) contribution else -contribution
        }
        return score
    }

    private fun allLegalMoves(state: AnimalChessBoardState): List<String> {
        val result = mutableListOf<String>()
        for ((sq, piece) in state.pieceMap) {
            if (piece.color != state.sideToMove) continue
            engine.getLegalMovesFromSquare(state, sq).forEach { result.add(it.uci) }
        }
        return result
    }

    companion object {
        private val PIECE_VALUE = mapOf(
            AnimalType.ELEPHANT to 800,
            AnimalType.LION     to 700,
            AnimalType.TIGER    to 600,
            AnimalType.LEOPARD  to 500,
            AnimalType.WOLF     to 400,
            AnimalType.DOG      to 300,
            AnimalType.CAT      to 200,
            AnimalType.MOUSE    to 100
        )
    }
}
