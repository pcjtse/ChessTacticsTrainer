package com.example.chesstacticstrainer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.usecase.GetUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiUserProgressUseCase
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    getChessProgress: GetUserProgressUseCase,
    getXiangqiProgress: GetXiangqiUserProgressUseCase
) : ViewModel() {

    val chessProgress: Flow<UserProgress>   = getChessProgress.observe()
    val xiangqiProgress: Flow<UserProgress> = getXiangqiProgress.observe()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                HomeViewModel(
                    app.container.getUserProgressUseCase,
                    app.container.getXiangqiUserProgressUseCase
                )
            }
        }
    }
}
