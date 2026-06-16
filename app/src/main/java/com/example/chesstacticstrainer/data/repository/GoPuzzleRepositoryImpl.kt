package com.example.chesstacticstrainer.data.repository

import android.util.Log
import com.example.chesstacticstrainer.data.local.GoPuzzleCache
import com.example.chesstacticstrainer.data.local.GoProgressStore
import com.example.chesstacticstrainer.data.local.GoSgfParser
import com.example.chesstacticstrainer.data.remote.GoproblemsApiService
import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.repository.GoPuzzleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "CTT-GO"

class GoPuzzleRepositoryImpl(
    private val puzzleCache: GoPuzzleCache,
    private val progressStore: GoProgressStore,
    private val apiService: GoproblemsApiService,
    private val fallbackPuzzles: List<GoPuzzle>
) : GoPuzzleRepository {

    override suspend fun getNextPuzzle(): Result<GoPuzzle> = runCatching {
        val difficulty = progressStore.getDifficulty()
        val min = difficulty.levelFrom
        val max = difficulty.levelTo

        val unsolvedCount = puzzleCache.countUnsolvedInRange(min, max)
        Log.d(TAG, "cache unsolved in ${difficulty.displayName} (level $min-$max)=$unsolvedCount")

        if (unsolvedCount in 1..3) {
            CoroutineScope(Dispatchers.IO).launch { prefetch(4, difficulty) }
        }

        puzzleCache.getNextUnsolved(min, max)
            ?: fetchAndCache(difficulty)
            ?: fallbackPuzzles.filter { it.difficulty in min..max }.randomOrNull()
            ?: fallbackPuzzles.randomOrNull()
            ?: error("No Go puzzles available")
    }

    override suspend fun markPuzzleSolved(puzzleId: String): Result<Unit> = runCatching {
        puzzleCache.markSolved(puzzleId)
    }

    override suspend fun getDifficulty(): GoDifficulty = progressStore.getDifficulty()

    override suspend fun setDifficulty(difficulty: GoDifficulty): Result<Unit> = runCatching {
        progressStore.setDifficulty(difficulty)
    }

    override fun observeUserProgress(): Flow<UserProgress> = progressStore.observe()

    override suspend fun updateUserProgress(progress: UserProgress): Result<Unit> = runCatching {
        progressStore.update(progress)
    }

    private suspend fun fetchAndCache(difficulty: GoDifficulty): GoPuzzle? = runCatching {
        val dto = apiService.fetchGoPuzzle(difficulty)
        if (dto.sgf.isBlank()) error("Empty SGF for problem ${dto.id}")

        val id = "gp_${dto.id}"
        // Use problemLevel from API as our difficulty value; fall back to levelFrom if 0
        val diffLevel = if (dto.problemLevel > 0) dto.problemLevel else difficulty.levelFrom

        val puzzle = GoSgfParser.parse(dto.sgf, id = id, difficulty = diffLevel, category = "基本")
            ?: error("SGF parse failed for problem ${dto.id}")

        puzzleCache.insert(puzzle.id, dto.sgf, diffLevel, puzzle.category)
        Log.d(TAG, "cached ${puzzle.id} '${puzzle.name}' level=$diffLevel (${difficulty.displayName})")
        puzzle
    }.onFailure { e ->
        Log.w(TAG, "API fetch failed, falling back to assets: ${e.message}")
    }.getOrNull()

    private suspend fun prefetch(count: Int, difficulty: GoDifficulty) {
        repeat(count) {
            fetchAndCache(difficulty) ?: return
            delay(2500L)
        }
    }
}
