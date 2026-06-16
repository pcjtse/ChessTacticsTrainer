package com.example.chesstacticstrainer.domain.model

data class AnimalChessMove(val fromSquare: String, val toSquare: String) {
    val uci: String get() = "$fromSquare$toSquare"
}
