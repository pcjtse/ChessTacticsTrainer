package com.example.chesstacticstrainer.domain.model

data class XiangqiMove(val fromSquare: String, val toSquare: String) {
    val uci: String get() = "$fromSquare$toSquare"
}
