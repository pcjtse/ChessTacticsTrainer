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
    val firstMove = puzzle.solution.firstOrNull()
    // Try offsets -1, 0, +1 around initialPly and pick the FEN where solution[0] is legal.
    var chosenFen = reconstructFen(game.pgn, puzzle.initialPly - 1)
    var chosenOffset = -1
    if (firstMove != null) {
        for (offset in listOf(-1, 0, 1)) {
            val candidate = reconstructFen(game.pgn, puzzle.initialPly + offset)
            if (isMoveLegalInFen(candidate, firstMove)) {
                chosenFen = candidate
                chosenOffset = offset
                break
            }
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
