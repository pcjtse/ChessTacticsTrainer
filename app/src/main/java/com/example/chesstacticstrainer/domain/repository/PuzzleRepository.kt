package com.example.chesstacticstrainer.domain.repository

import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.model.ThemeStats
import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface PuzzleRepository {
    suspend fun getNextPuzzle(): Result<Puzzle>
    suspend fun markPuzzleSolved(puzzleId: String): Result<Unit>
    suspend fun removePuzzle(puzzleId: String): Result<Unit>
    suspend fun prefetchPuzzles(count: Int = 20): Result<Unit>
    fun observeUserProgress(): Flow<UserProgress>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
    fun observeThemeStats(): Flow<List<ThemeStats>>
    suspend fun incrementThemeStat(theme: String, solved: Boolean): Result<Unit>
}
