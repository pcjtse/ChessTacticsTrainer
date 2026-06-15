package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.XiangqiPuzzleRepository
import kotlinx.coroutines.flow.Flow

class GetXiangqiUserProgressUseCase(private val repository: XiangqiPuzzleRepository) {
    fun observe(): Flow<UserProgress> = repository.observeUserProgress()
}
