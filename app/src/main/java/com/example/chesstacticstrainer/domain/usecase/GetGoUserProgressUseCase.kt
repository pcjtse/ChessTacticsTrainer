package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.GoPuzzleRepository
import kotlinx.coroutines.flow.Flow

class GetGoUserProgressUseCase(private val repository: GoPuzzleRepository) {
    fun observe(): Flow<UserProgress> = repository.observeUserProgress()
}
