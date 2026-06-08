package com.example.chesstacticstrainer.domain.model

data class Puzzle(
    val id: String,
    val fen: String,
    val solutionMoves: List<String>,
    val themes: List<String>,
    val rating: Int,
    val isSolved: Boolean = false
)
