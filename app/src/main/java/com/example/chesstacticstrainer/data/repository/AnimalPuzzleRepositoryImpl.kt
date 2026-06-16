package com.example.chesstacticstrainer.data.repository

import com.example.chesstacticstrainer.data.local.AnimalProgressStore
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.AnimalPuzzleRepository
import kotlinx.coroutines.flow.Flow

class AnimalPuzzleRepositoryImpl(
    private val progressStore: AnimalProgressStore
) : AnimalPuzzleRepository {

    override fun observeUserProgress(): Flow<UserProgress> = progressStore.observe()

    override suspend fun updateUserProgress(progress: UserProgress): Result<Unit> = runCatching {
        progressStore.update(progress)
    }
}
