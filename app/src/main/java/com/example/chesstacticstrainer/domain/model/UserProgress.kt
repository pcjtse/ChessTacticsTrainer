package com.example.chesstacticstrainer.domain.model

import java.time.LocalDate

data class UserProgress(
    val id: Int = 1,
    val rating: Int = 1200,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPuzzleDate: LocalDate? = null,
    val totalSolved: Int = 0,
    val totalAttempted: Int = 0
)
