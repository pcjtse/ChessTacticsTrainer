package com.example.chesstacticstrainer.engine

import android.util.Log
import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.model.BoardState
import com.example.chesstacticstrainer.domain.model.ChessMove
import com.example.chesstacticstrainer.domain.model.Piece
import com.example.chesstacticstrainer.domain.model.PieceColor
import com.example.chesstacticstrainer.domain.model.PieceType
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square as LibSquare
import com.github.bhlangonijr.chesslib.move.Move as LibMove

class ChessLibEngine : ChessEngine {

    override fun loadFen(fen: String): BoardState = board(fen).toDomain(fen)

    override fun applyMove(state: BoardState, uciMove: String): BoardState {
        return try {
            val b = board(state.fen)
            b.doMove(parseMove(uciMove, b))
            b.toDomain(b.fen)
        } catch (e: Exception) {
            // chesslib throws ArrayIndexOutOfBoundsException (pieces[64]) for certain
            // positions (e.g. checkmate) inside doMove or its internal legalMoves call.
            // Return the pre-move board; isCorrect is already computed independently.
            Log.e("CTT", "applyMove failed for uci=$uciMove fen=${state.fen}: ${e.message}")
            state
        }
    }

    override fun getLegalMoves(state: BoardState): List<ChessMove> =
        board(state.fen).legalMoves().map { it.toDomain() }

    override fun isMoveLegal(state: BoardState, uciMove: String): Boolean {
        val b = board(state.fen)
        val move = parseMove(uciMove, b)
        return b.legalMoves().any { it == move }
    }

    override fun isInCheck(state: BoardState): Boolean = board(state.fen).isKingAttacked

    override fun isCheckmate(state: BoardState): Boolean = board(state.fen).isMated

    override fun isStalemate(state: BoardState): Boolean {
        val b = board(state.fen)
        return !b.isKingAttacked && b.legalMoves().isEmpty()
    }

    override fun pieceAt(state: BoardState, square: String): Piece? {
        val p = board(state.fen).getPiece(LibSquare.valueOf(square.uppercase()))
        return if (p == LibPiece.NONE) null else p.toDomain()
    }

    override fun getLegalMovesFromSquare(state: BoardState, square: String): List<ChessMove> {
        val sq = LibSquare.valueOf(square.uppercase())
        return board(state.fen).legalMoves().filter { it.from == sq }.map { it.toDomain() }
    }

    override fun sideToMove(state: BoardState): String =
        if (board(state.fen).sideToMove == Side.WHITE) "white" else "black"

    private fun board(fen: String) = Board().also { it.loadFromFen(fen) }

    private fun parseMove(uci: String, b: Board): LibMove {
        val from = LibSquare.valueOf(uci.substring(0, 2).uppercase())
        val to = LibSquare.valueOf(uci.substring(2, 4).uppercase())
        val promotion = if (uci.length == 5) {
            val isWhite = b.sideToMove == Side.WHITE
            when (uci[4]) {
                'q' -> if (isWhite) LibPiece.WHITE_QUEEN else LibPiece.BLACK_QUEEN
                'r' -> if (isWhite) LibPiece.WHITE_ROOK else LibPiece.BLACK_ROOK
                'b' -> if (isWhite) LibPiece.WHITE_BISHOP else LibPiece.BLACK_BISHOP
                'n' -> if (isWhite) LibPiece.WHITE_KNIGHT else LibPiece.BLACK_KNIGHT
                else -> LibPiece.NONE
            }
        } else LibPiece.NONE
        return LibMove(from, to, promotion)
    }

    private fun Board.toDomain(fen: String): BoardState {
        val pieces = mutableMapOf<String, Piece>()
        for (sq in LibSquare.values()) {
            if (sq == LibSquare.NONE) continue
            val p = getPiece(sq)
            if (p != LibPiece.NONE) pieces[sq.name.lowercase()] = p.toDomain()
        }
        val isCheck = try { isKingAttacked } catch (e: Exception) { false }
        return BoardState(
            fen = fen,
            pieceMap = pieces,
            isCheck = isCheck
        )
    }

    private fun LibMove.toDomain(): ChessMove {
        val f = from.name.lowercase()
        val t = to.name.lowercase()
        val promo = when (promotion) {
            LibPiece.WHITE_QUEEN, LibPiece.BLACK_QUEEN -> 'q'
            LibPiece.WHITE_ROOK, LibPiece.BLACK_ROOK -> 'r'
            LibPiece.WHITE_BISHOP, LibPiece.BLACK_BISHOP -> 'b'
            LibPiece.WHITE_KNIGHT, LibPiece.BLACK_KNIGHT -> 'n'
            else -> null
        }
        return ChessMove(f, t, f + t + (promo ?: ""), promo)
    }

    private fun LibPiece.toDomain(): Piece {
        val type = when (pieceType) {
            LibPieceType.KING -> PieceType.KING
            LibPieceType.QUEEN -> PieceType.QUEEN
            LibPieceType.ROOK -> PieceType.ROOK
            LibPieceType.BISHOP -> PieceType.BISHOP
            LibPieceType.KNIGHT -> PieceType.KNIGHT
            LibPieceType.PAWN -> PieceType.PAWN
            else -> PieceType.PAWN
        }
        val color = if (pieceSide == Side.WHITE) PieceColor.WHITE else PieceColor.BLACK
        return Piece(type, color)
    }
}
