package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first

private val Context.xiangqiPuzzleDataStore by preferencesDataStore(name = "xiangqi_puzzle_cache")

class XiangqiPuzzleCache(private val context: Context, private val moshi: Moshi) {

    private val adapter = moshi.adapter<List<CachedPuzzle>>(
        Types.newParameterizedType(List::class.java, CachedPuzzle::class.java)
    )

    private val KEY = stringPreferencesKey("xq_puzzles")

    private suspend fun readAll(): List<CachedPuzzle> {
        val json = context.xiangqiPuzzleDataStore.data.first()[KEY] ?: return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    private suspend fun writeAll(puzzles: List<CachedPuzzle>) {
        context.xiangqiPuzzleDataStore.edit { prefs ->
            prefs[KEY] = adapter.toJson(puzzles)
        }
    }

    suspend fun countUnsolved(): Int = readAll().count { !it.isSolved }

    suspend fun getNextUnsolved(): Puzzle? =
        readAll().filter { !it.isSolved }.randomOrNull()?.toDomain()

    suspend fun insert(puzzle: Puzzle) {
        val existing = readAll().associateBy { it.id }.toMutableMap()
        if (!existing.containsKey(puzzle.id)) {
            existing[puzzle.id] = puzzle.toCached()
            writeAll(existing.values.toList())
        }
    }

    suspend fun markSolved(id: String) {
        writeAll(readAll().map { if (it.id == id) it.copy(isSolved = true) else it })
    }

    suspend fun remove(id: String) {
        writeAll(readAll().filter { it.id != id })
    }
}

private fun CachedPuzzle.toDomain() = Puzzle(id, fen, solutionMoves, themes, rating, isSolved)
private fun Puzzle.toCached() = CachedPuzzle(id, fen, solutionMoves, themes, rating, isSolved)
