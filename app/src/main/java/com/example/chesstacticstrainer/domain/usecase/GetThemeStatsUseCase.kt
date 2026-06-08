package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.ThemeStats
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import kotlinx.coroutines.flow.Flow

class GetThemeStatsUseCase(private val repository: PuzzleRepository) {
    fun observe(): Flow<List<ThemeStats>> = repository.observeThemeStats()
}
