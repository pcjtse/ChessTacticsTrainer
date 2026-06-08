package com.example.chesstacticstrainer.domain.model

data class ChessMove(
    val fromSquare: String,
    val toSquare: String,
    val uci: String,
    val promotion: Char? = null
)
