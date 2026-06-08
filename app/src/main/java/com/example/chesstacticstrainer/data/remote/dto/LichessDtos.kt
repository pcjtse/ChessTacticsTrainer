package com.example.chesstacticstrainer.data.remote.dto

data class PuzzleResponseDto(
    val game: GameDto,
    val puzzle: PuzzleDto
)

data class GameDto(
    val id: String,
    val pgn: String
)

data class PuzzleDto(
    val id: String,
    val rating: Int,
    val plays: Int = 0,
    val solution: List<String>,
    val themes: List<String>,
    val initialPly: Int
)
