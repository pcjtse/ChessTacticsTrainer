package com.example.chesstacticstrainer.data.repository

import com.example.chesstacticstrainer.data.local.PuzzleCache
import com.example.chesstacticstrainer.data.local.ThemeStatsStore
import com.example.chesstacticstrainer.data.local.UserProgressStore
import com.example.chesstacticstrainer.data.mapper.toDomain
import com.example.chesstacticstrainer.data.remote.LichessApiService
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.model.ThemeStats
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PuzzleRepositoryImpl(
    private val puzzleCache: PuzzleCache,
    private val progressStore: UserProgressStore,
    private val themeStatsStore: ThemeStatsStore,
    private val apiService: LichessApiService
) : PuzzleRepository {

    override suspend fun getNextPuzzle(): Result<Puzzle> = runCatching {
        val unsolvedCount = puzzleCache.countUnsolved()
        if (unsolvedCount < 10) {
            CoroutineScope(Dispatchers.IO).launch { prefetchPuzzles(20) }
        }
        puzzleCache.getNextUnsolved()
            ?: apiService.getNextPuzzle().toDomain().also { puzzleCache.insert(it) }
    }

    override suspend fun markPuzzleSolved(puzzleId: String): Result<Unit> = runCatching {
        puzzleCache.markSolved(puzzleId)
    }

    override suspend fun prefetchPuzzles(count: Int): Result<Unit> = runCatching {
        repeat(count) {
            runCatching {
                val puzzle = apiService.getNextPuzzle().toDomain()
                puzzleCache.insert(puzzle)
            }
            delay(1100L)
        }
    }

    override fun observeUserProgress(): Flow<UserProgress> = progressStore.observe()

    override suspend fun updateUserProgress(progress: UserProgress): Result<Unit> = runCatching {
        progressStore.update(progress)
    }

    override fun observeThemeStats(): Flow<List<ThemeStats>> = themeStatsStore.observeAll()

    override suspend fun incrementThemeStat(theme: String, solved: Boolean): Result<Unit> = runCatching {
        themeStatsStore.increment(theme, solved)
    }
}
