package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first

private val Context.goPuzzleDataStore by preferencesDataStore(name = "go_puzzle_cache")

class GoPuzzleCache(private val context: Context, private val moshi: Moshi) {

    private val adapter = moshi.adapter<List<CachedGoPuzzle>>(
        Types.newParameterizedType(List::class.java, CachedGoPuzzle::class.java)
    )

    private val KEY = stringPreferencesKey("go_puzzles")

    private suspend fun readAll(): List<CachedGoPuzzle> {
        val json = context.goPuzzleDataStore.data.first()[KEY] ?: return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    private suspend fun writeAll(puzzles: List<CachedGoPuzzle>) {
        context.goPuzzleDataStore.edit { prefs ->
            prefs[KEY] = adapter.toJson(puzzles)
        }
    }

    suspend fun countUnsolved(): Int = readAll().count { !it.isSolved }

    suspend fun countUnsolvedInRange(min: Int, max: Int): Int =
        readAll().count { !it.isSolved && it.difficulty in min..max }

    suspend fun getNextUnsolved(min: Int = 1, max: Int = Int.MAX_VALUE): GoPuzzle? =
        readAll().filter { !it.isSolved && it.difficulty in min..max }.randomOrNull()?.toDomain()

    suspend fun insert(id: String, sgf: String, difficulty: Int, category: String) {
        val existing = readAll().associateBy { it.id }.toMutableMap()
        if (!existing.containsKey(id)) {
            existing[id] = CachedGoPuzzle(id = id, sgf = sgf, difficulty = difficulty, category = category)
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

private data class CachedGoPuzzle(
    @Json(name = "id")         val id: String,
    @Json(name = "sgf")        val sgf: String,
    @Json(name = "difficulty") val difficulty: Int = 1,
    @Json(name = "category")   val category: String = "基本",
    @Json(name = "isSolved")   val isSolved: Boolean = false
)

private fun CachedGoPuzzle.toDomain(): GoPuzzle? =
    GoSgfParser.parse(sgf, id = id, difficulty = difficulty, category = category)
