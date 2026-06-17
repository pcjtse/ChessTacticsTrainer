package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

class LanguageStore(private val context: Context) {
    private val IS_ENGLISH_KEY = booleanPreferencesKey("is_english")

    fun observeIsEnglish(): Flow<Boolean> =
        context.languageDataStore.data.map { prefs -> prefs[IS_ENGLISH_KEY] ?: false }

    suspend fun setIsEnglish(isEnglish: Boolean) {
        context.languageDataStore.edit { it[IS_ENGLISH_KEY] = isEnglish }
    }
}
