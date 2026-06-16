package com.example.chesstacticstrainer.domain.repository

import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface GoPuzzleRepository {
    suspend fun getNextPuzzle(): Result<GoPuzzle>
    suspend fun markPuzzleSolved(puzzleId: String): Result<Unit>
    suspend fun getDifficulty(): GoDifficulty
    suspend fun setDifficulty(difficulty: GoDifficulty): Result<Unit>
    fun observeUserProgress(): Flow<UserProgress>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
}
