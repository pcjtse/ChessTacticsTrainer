package com.example.chesstacticstrainer.domain.repository

import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface XiangqiPuzzleRepository {
    suspend fun getNextPuzzle(): Result<Puzzle>
    suspend fun markPuzzleSolved(puzzleId: String): Result<Unit>
    suspend fun removePuzzle(puzzleId: String): Result<Unit>
    fun observeUserProgress(): Flow<UserProgress>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
}
