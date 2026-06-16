package com.example.chesstacticstrainer.domain.repository

import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface AnimalPuzzleRepository {
    fun observeUserProgress(): Flow<UserProgress>
    suspend fun updateUserProgress(progress: UserProgress): Result<Unit>
}
