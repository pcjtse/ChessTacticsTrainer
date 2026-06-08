package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.ThemeStats
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_stats")

class ThemeStatsStore(private val context: Context, private val moshi: Moshi) {

    private val adapter = moshi.adapter<List<ThemeStatEntry>>(
        Types.newParameterizedType(List::class.java, ThemeStatEntry::class.java)
    )

    private val KEY = stringPreferencesKey("theme_stats")

    fun observeAll(): Flow<List<ThemeStats>> = context.themeDataStore.data.map { prefs ->
        val json = prefs[KEY] ?: return@map emptyList()
        runCatching { adapter.fromJson(json) ?: emptyList() }
            .getOrDefault(emptyList())
            .sortedByDescending { it.attempted }
            .map { ThemeStats(it.theme, it.solved, it.attempted) }
    }

    suspend fun increment(theme: String, solved: Boolean) {
        val current = context.themeDataStore.data.first()
        val json = current[KEY]
        val entries = runCatching { json?.let { adapter.fromJson(it) } ?: emptyList() }
            .getOrDefault(emptyList())
            .toMutableList()

        val idx = entries.indexOfFirst { it.theme == theme }
        if (idx >= 0) {
            entries[idx] = entries[idx].copy(
                solved = if (solved) entries[idx].solved + 1 else entries[idx].solved,
                attempted = entries[idx].attempted + 1
            )
        } else {
            entries.add(ThemeStatEntry(theme, if (solved) 1 else 0, 1))
        }

        context.themeDataStore.edit { prefs -> prefs[KEY] = adapter.toJson(entries) }
    }
}

private data class ThemeStatEntry(val theme: String, val solved: Int, val attempted: Int)
