package com.example.chesstacticstrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.goProgressDataStore by preferencesDataStore(name = "go_progress")

class GoProgressStore(private val context: Context) {

    private val PUZZLE_INDEX    = intPreferencesKey("go_puzzle_index")
    private val DIFFICULTY      = intPreferencesKey("go_difficulty")
    private val RATING          = intPreferencesKey("go_rating")
    private val STREAK          = intPreferencesKey("go_current_streak")
    private val LONGEST         = intPreferencesKey("go_longest_streak")
    private val LAST_DATE       = longPreferencesKey("go_last_epoch_day")
    private val TOTAL_SOLVED    = intPreferencesKey("go_total_solved")
    private val TOTAL_ATTEMPTED = intPreferencesKey("go_total_attempted")

    fun observe(): Flow<UserProgress> = context.goProgressDataStore.data.map { prefs ->
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
        context.goProgressDataStore.edit { prefs ->
            prefs[RATING]          = progress.rating
            prefs[STREAK]          = progress.currentStreak
            prefs[LONGEST]         = progress.longestStreak
            prefs[LAST_DATE]       = progress.lastPuzzleDate?.toEpochDay() ?: -1L
            prefs[TOTAL_SOLVED]    = progress.totalSolved
            prefs[TOTAL_ATTEMPTED] = progress.totalAttempted
        }
    }

    suspend fun getCurrentIndex(): Int =
        context.goProgressDataStore.data.first()[PUZZLE_INDEX] ?: 0

    suspend fun advanceIndex(totalPuzzles: Int) {
        context.goProgressDataStore.edit { prefs ->
            val cur = prefs[PUZZLE_INDEX] ?: 0
            prefs[PUZZLE_INDEX] = (cur + 1) % totalPuzzles
        }
    }

    suspend fun getDifficulty(): GoDifficulty =
        GoDifficulty.fromOrdinal(context.goProgressDataStore.data.first()[DIFFICULTY] ?: GoDifficulty.MEDIUM.ordinal)

    suspend fun setDifficulty(difficulty: GoDifficulty) {
        context.goProgressDataStore.edit { prefs -> prefs[DIFFICULTY] = difficulty.ordinal }
    }
}
