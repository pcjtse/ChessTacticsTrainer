package com.example.chesstacticstrainer.data.repository

import android.util.Log
import com.example.chesstacticstrainer.data.local.XiangqiProgressStore
import com.example.chesstacticstrainer.data.local.XiangqiPuzzleCache
import com.example.chesstacticstrainer.data.mapper.toDomain
import com.example.chesstacticstrainer.data.remote.PychessApiService
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.XiangqiPuzzleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class XiangqiPuzzleRepositoryImpl(
    private val puzzleCache: XiangqiPuzzleCache,
    private val progressStore: XiangqiProgressStore,
    private val apiService: PychessApiService
) : XiangqiPuzzleRepository {

    override suspend fun getNextPuzzle(): Result<Puzzle> = runCatching {
        val unsolvedCount = puzzleCache.countUnsolved()
        if (unsolvedCount in 1..3) {
            CoroutineScope(Dispatchers.IO).launch { prefetch(4) }
        }
        puzzleCache.getNextUnsolved()
            ?: apiService.fetchXiangqiPuzzle().toDomain().also { puzzleCache.insert(it) }
    }

    override suspend fun markPuzzleSolved(puzzleId: String): Result<Unit> = runCatching {
        puzzleCache.markSolved(puzzleId)
    }

    override suspend fun removePuzzle(puzzleId: String): Result<Unit> = runCatching {
        puzzleCache.remove(puzzleId)
    }

    override fun observeUserProgress(): Flow<UserProgress> = progressStore.observe()

    override suspend fun updateUserProgress(progress: UserProgress): Result<Unit> = runCatching {
        progressStore.update(progress)
    }

    private suspend fun prefetch(count: Int) {
        repeat(count) {
            runCatching {
                val puzzle = apiService.fetchXiangqiPuzzle().toDomain()
                puzzleCache.insert(puzzle)
            }.onFailure { e ->
                Log.w("CTT-XQ", "prefetch failed: ${e.message}")
                return
            }
            delay(2500L)
        }
    }
}
