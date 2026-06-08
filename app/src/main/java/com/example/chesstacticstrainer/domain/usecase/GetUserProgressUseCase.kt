package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import kotlinx.coroutines.flow.Flow

class GetUserProgressUseCase(private val repository: PuzzleRepository) {
    fun observe(): Flow<UserProgress> = repository.observeUserProgress()
}
