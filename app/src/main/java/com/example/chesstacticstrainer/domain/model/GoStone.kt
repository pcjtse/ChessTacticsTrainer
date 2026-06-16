package com.example.chesstacticstrainer.domain.model

enum class GoStone { BLACK, WHITE;
    fun opposite(): GoStone = if (this == BLACK) WHITE else BLACK
}
