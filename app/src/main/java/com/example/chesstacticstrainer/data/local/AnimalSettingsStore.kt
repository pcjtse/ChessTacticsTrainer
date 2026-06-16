package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.animalSettingsDataStore by preferencesDataStore(name = "animal_settings")

class AnimalSettingsStore(private val context: Context) {
    private val DIFFICULTY_KEY = stringPreferencesKey("difficulty")

    fun observeDifficulty(): Flow<AnimalDifficulty> =
        context.animalSettingsDataStore.data.map { prefs ->
            AnimalDifficulty.entries.find { it.name == prefs[DIFFICULTY_KEY] } ?: AnimalDifficulty.MEDIUM
        }

    suspend fun setDifficulty(difficulty: AnimalDifficulty) {
        context.animalSettingsDataStore.edit { it[DIFFICULTY_KEY] = difficulty.name }
    }
}
