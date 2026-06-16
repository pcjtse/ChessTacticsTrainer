package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.animalProgressDataStore by preferencesDataStore(name = "animal_progress")

class AnimalProgressStore(private val context: Context) {

    private val RATING          = intPreferencesKey("ac_rating")
    private val STREAK          = intPreferencesKey("ac_current_streak")
    private val LONGEST         = intPreferencesKey("ac_longest_streak")
    private val LAST_DATE       = longPreferencesKey("ac_last_puzzle_epoch_day")
    private val TOTAL_SOLVED    = intPreferencesKey("ac_total_solved")
    private val TOTAL_ATTEMPTED = intPreferencesKey("ac_total_attempted")

    fun observe(): Flow<UserProgress> = context.animalProgressDataStore.data.map { prefs ->
        val epochDay = prefs[LAST_DATE] ?: -1L
        UserProgress(
            rating         = prefs[RATING] ?: 1200,
            currentStreak  = prefs[STREAK] ?: 0,
            longestStreak  = prefs[LONGEST] ?: 0,
            lastPuzzleDate = if (epochDay >= 0) LocalDate.ofEpochDay(epochDay) else null,
            totalSolved    = prefs[TOTAL_SOLVED] ?: 0,
            totalAttempted = prefs[TOTAL_ATTEMPTED] ?: 0
        )
    }

    suspend fun update(progress: UserProgress) {
        context.animalProgressDataStore.edit { prefs ->
            prefs[RATING]          = progress.rating
            prefs[STREAK]          = progress.currentStreak
            prefs[LONGEST]         = progress.longestStreak
            prefs[LAST_DATE]       = progress.lastPuzzleDate?.toEpochDay() ?: -1L
            prefs[TOTAL_SOLVED]    = progress.totalSolved
            prefs[TOTAL_ATTEMPTED] = progress.totalAttempted
        }
    }
}
