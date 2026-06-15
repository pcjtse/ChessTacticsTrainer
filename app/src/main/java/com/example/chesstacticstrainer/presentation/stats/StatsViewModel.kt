package com.example.chesstacticstrainer.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.model.ThemeStats
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.usecase.GetThemeStatsUseCase
import com.example.chesstacticstrainer.domain.usecase.GetUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiUserProgressUseCase
import kotlinx.coroutines.flow.Flow

class StatsViewModel(
    getChessProgress: GetUserProgressUseCase,
    getXiangqiProgress: GetXiangqiUserProgressUseCase,
    getThemeStats: GetThemeStatsUseCase
) : ViewModel() {

    val chessProgress: Flow<UserProgress>   = getChessProgress.observe()
    val xiangqiProgress: Flow<UserProgress> = getXiangqiProgress.observe()
    val themeStats: Flow<List<ThemeStats>>  = getThemeStats.observe()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                StatsViewModel(
                    app.container.getUserProgressUseCase,
                    app.container.getXiangqiUserProgressUseCase,
                    app.container.getThemeStatsUseCase
                )
            }
        }
    }
}
