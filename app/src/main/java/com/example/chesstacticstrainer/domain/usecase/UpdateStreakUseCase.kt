package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class UpdateStreakUseCase(private val repository: PuzzleRepository) {

    suspend operator fun invoke(solved: Boolean) {
        val current = repository.observeUserProgress().first()
        val today = LocalDate.now()
        val lastDate = current.lastPuzzleDate

        val updated = when {
            lastDate == today -> current.copy(
                totalAttempted = current.totalAttempted + 1,
                totalSolved = if (solved) current.totalSolved + 1 else current.totalSolved
            )
            lastDate == today.minusDays(1) && solved -> current.copy(
                currentStreak = current.currentStreak + 1,
                longestStreak = maxOf(current.longestStreak, current.currentStreak + 1),
                lastPuzzleDate = today,
                totalAttempted = current.totalAttempted + 1,
                totalSolved = current.totalSolved + 1
            )
            solved -> current.copy(
                currentStreak = 1,
                longestStreak = maxOf(current.longestStreak, 1),
                lastPuzzleDate = today,
                totalAttempted = current.totalAttempted + 1,
                totalSolved = current.totalSolved + 1
            )
            else -> current.copy(
                currentStreak = 0,
                totalAttempted = current.totalAttempted + 1
            )
        }

        repository.updateUserProgress(updated)
    }
}
