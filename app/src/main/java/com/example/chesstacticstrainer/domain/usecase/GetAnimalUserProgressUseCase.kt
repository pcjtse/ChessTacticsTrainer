package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.AnimalPuzzleRepository
import kotlinx.coroutines.flow.Flow

class GetAnimalUserProgressUseCase(private val repository: AnimalPuzzleRepository) {
    fun observe(): Flow<UserProgress> = repository.observeUserProgress()
}
