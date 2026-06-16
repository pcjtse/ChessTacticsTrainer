package com.example.chesstacticstrainer.domain.model

data class GoPuzzle(
    val id: String,
    val boardSize: Int,
    val name: String,
    val initialState: GoBoardState,
    val solutionRoot: GoSgfNode,
    val playerColor: GoStone,
    val difficulty: Int = 1,
    val category: String = "基本"
)
