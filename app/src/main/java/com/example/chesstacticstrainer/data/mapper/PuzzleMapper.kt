package com.example.chesstacticstrainer.data.mapper

import android.util.Log
import com.example.chesstacticstrainer.data.remote.dto.PuzzleResponseDto
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square as LibSquare
import com.github.bhlangonijr.chesslib.move.Move as LibMove
import com.github.bhlangonijr.chesslib.move.MoveList

fun PuzzleResponseDto.toDomain(): Puzzle {
    // Default to initialPly - 1 in case nothing validates.
    var chosenFen = reconstructFen(game.pgn, puzzle.initialPly - 1)
    var chosenOffset = -1

    if (puzzle.solution.isNotEmpty()) {
        var bestFen: String? = null
        var bestOffset: Int? = null
        var bestScore = 0

        // Try offsets in order of proximity to initialPly. For each candidate FEN,
        // walk the full solution chain and count how many consecutive moves are legal.
        // Accept the first offset where every move in the chain is legal (full score).
        // If none passes fully, keep the offset with the highest partial score as fallback.
        for (offset in listOf(-1, 0, 1, -2, 2)) {
            val candidate = reconstructFen(game.pgn, puzzle.initialPly + offset)
            val score = countValidMoveChain(candidate, puzzle.solution)
            if (score == puzzle.solution.size) {
                chosenFen = candidate
                chosenOffset = offset
                bestFen = null   // signal: no fallback needed
                break
            }
            if (score > bestScore) {
                bestScore = score
                bestFen = candidate
                bestOffset = offset
            }
        }

        if (bestFen != null) {
            chosenFen = bestFen
            chosenOffset = bestOffset!!
            Log.w("CTT", "toDomain: id=${puzzle.id} no offset fully validates — best offset $chosenOffset validates $bestScore/${puzzle.solution.size} moves")
        }
    }

    Log.d("CTT", "toDomain: id=${puzzle.id} initialPly=${puzzle.initialPly} offset=$chosenOffset")
    Log.d("CTT", "  fen=$chosenFen solution=${puzzle.solution}")
    return Puzzle(
        id = puzzle.id,
        fen = chosenFen,
        solutionMoves = puzzle.solution,
        themes = puzzle.themes,
        rating = puzzle.rating
    )
}

/** Applies each move in [moves] in sequence from [startFen]; returns the count of
 *  consecutive moves that are legal (stops at the first illegal or failing move). */
private fun countValidMoveChain(startFen: String, moves: List<String>): Int {
    var fen = startFen
    for ((index, move) in moves.withIndex()) {
        fen = applyMoveToFen(fen, move) ?: return index
    }
    return moves.size
}

private fun applyMoveToFen(fen: String, uci: String): String? {
    return try {
        val board = Board()
        board.loadFromFen(fen)
        val from = LibSquare.valueOf(uci.substring(0, 2).uppercase())
        val to = LibSquare.valueOf(uci.substring(2, 4).uppercase())
        val promo = if (uci.length == 5) {
            val isWhite = board.sideToMove == Side.WHITE
            when (uci[4]) {
                'q' -> if (isWhite) LibPiece.WHITE_QUEEN else LibPiece.BLACK_QUEEN
                'r' -> if (isWhite) LibPiece.WHITE_ROOK else LibPiece.BLACK_ROOK
                'b' -> if (isWhite) LibPiece.WHITE_BISHOP else LibPiece.BLACK_BISHOP
                'n' -> if (isWhite) LibPiece.WHITE_KNIGHT else LibPiece.BLACK_KNIGHT
                else -> LibPiece.NONE
            }
        } else LibPiece.NONE
        val move = LibMove(from, to, promo)
        if (!board.legalMoves().any { it == move }) return null
        board.doMove(move)
        board.fen
    } catch (e: Exception) {
        null
    }
}

private fun isMoveLegalInFen(fen: String, uci: String): Boolean {
    return try {
        val board = Board()
        board.loadFromFen(fen)
        val from = LibSquare.valueOf(uci.substring(0, 2).uppercase())
        val to = LibSquare.valueOf(uci.substring(2, 4).uppercase())
        val promo = if (uci.length == 5) {
            val isWhite = board.sideToMove == Side.WHITE
            when (uci[4]) {
                'q' -> if (isWhite) LibPiece.WHITE_QUEEN else LibPiece.BLACK_QUEEN
                'r' -> if (isWhite) LibPiece.WHITE_ROOK else LibPiece.BLACK_ROOK
                'b' -> if (isWhite) LibPiece.WHITE_BISHOP else LibPiece.BLACK_BISHOP
                'n' -> if (isWhite) LibPiece.WHITE_KNIGHT else LibPiece.BLACK_KNIGHT
                else -> LibPiece.NONE
            }
        } else LibPiece.NONE
        val move = LibMove(from, to, promo)
        board.legalMoves().any { it == move }
    } catch (e: Exception) {
        false
    }
}

private fun reconstructFen(pgn: String, targetPly: Int): String {
    if (targetPly < 0) return Board().fen
    return try {
        var cleaned = pgn.replace(Regex("\\{[^}]*\\}"), "")
        var prev = ""
        while (prev != cleaned) {
            prev = cleaned
            cleaned = cleaned.replace(Regex("\\([^()]*\\)"), "")
        }
        cleaned = cleaned
            .replace(Regex("[!?]+"), "")
            .replace(Regex("\\$\\d+"), "")
            .replace(Regex("[+#]"), "")
            .replace(Regex("\\d+\\.+\\s*"), " ")
            .replace(Regex("(1-0|0-1|1/2-1/2|\\*)"), "")
            .trim()

        val tokens = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        val sanText = tokens.take(targetPly).joinToString(" ")

        val moveList = MoveList()
        moveList.loadFromSan(sanText)

        if (moveList.size < targetPly) {
            Log.w("CTT", "reconstructFen: parsed ${moveList.size}/${targetPly} moves for ply $targetPly")
        }

        val board = Board()
        for (i in 0 until minOf(targetPly, moveList.size)) {
            board.doMove(moveList[i])
        }
        board.fen
    } catch (e: Exception) {
        Log.e("CTT", "reconstructFen failed at ply $targetPly: ${e.message}")
        Board().fen
    }
}
