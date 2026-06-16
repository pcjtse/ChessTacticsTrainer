package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.example.chesstacticstrainer.domain.repository.GoPuzzleRepository

class GetNextGoPuzzleUseCase(private val repository: GoPuzzleRepository) {
    suspend operator fun invoke(): Result<GoPuzzle> = repository.getNextPuzzle()
    suspend fun markSolved(puzzleId: String) = repository.markPuzzleSolved(puzzleId)
    suspend fun getDifficulty(): GoDifficulty = repository.getDifficulty()
    suspend fun setDifficulty(difficulty: GoDifficulty) = repository.setDifficulty(difficulty)
}
