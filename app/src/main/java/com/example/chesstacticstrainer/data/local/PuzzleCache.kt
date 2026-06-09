package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first

private val Context.puzzleDataStore by preferencesDataStore(name = "puzzle_cache")

class PuzzleCache(private val context: Context, private val moshi: Moshi) {

    private val adapter = moshi.adapter<List<CachedPuzzle>>(
        Types.newParameterizedType(List::class.java, CachedPuzzle::class.java)
    )

    private val KEY = stringPreferencesKey("puzzles")

    private suspend fun readAll(): List<CachedPuzzle> {
        val json = context.puzzleDataStore.data.first()[KEY] ?: return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    private suspend fun writeAll(puzzles: List<CachedPuzzle>) {
        context.puzzleDataStore.edit { prefs ->
            prefs[KEY] = adapter.toJson(puzzles)
        }
    }

    suspend fun countUnsolved(): Int = readAll().count { !it.isSolved }

    suspend fun getNextUnsolved(): Puzzle? =
        readAll().filter { !it.isSolved }.randomOrNull()?.toDomain()

    suspend fun insertAll(puzzles: List<Puzzle>) {
        val existing = readAll().associateBy { it.id }.toMutableMap()
        puzzles.forEach { p ->
            if (!existing.containsKey(p.id)) existing[p.id] = p.toCached()
        }
        writeAll(existing.values.toList())
    }

    suspend fun insert(puzzle: Puzzle) = insertAll(listOf(puzzle))

    suspend fun markSolved(id: String) {
        writeAll(readAll().map { if (it.id == id) it.copy(isSolved = true) else it })
    }

    suspend fun remove(id: String) {
        writeAll(readAll().filter { it.id != id })
    }
}

data class CachedPuzzle(
    val id: String,
    val fen: String,
    val solutionMoves: List<String>,
    val themes: List<String>,
    val rating: Int,
    val isSolved: Boolean = false
)

private fun CachedPuzzle.toDomain() = Puzzle(id, fen, solutionMoves, themes, rating, isSolved)
private fun Puzzle.toCached() = CachedPuzzle(id, fen, solutionMoves, themes, rating, isSolved)
