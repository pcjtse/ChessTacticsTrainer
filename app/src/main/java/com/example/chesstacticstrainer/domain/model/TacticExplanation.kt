package com.example.chesstacticstrainer.domain.model

data class TacticExplanation(
    val tacticName: String,
    val description: String,
    val highlightedSquares: List<String> = emptyList()
)
