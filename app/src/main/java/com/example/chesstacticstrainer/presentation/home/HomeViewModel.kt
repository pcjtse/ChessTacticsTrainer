package com.example.chesstacticstrainer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.data.local.AnimalSettingsStore
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.domain.usecase.GetAnimalUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetGoUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiUserProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel(
    getChessProgress: GetUserProgressUseCase,
    getXiangqiProgress: GetXiangqiUserProgressUseCase,
    getAnimalProgress: GetAnimalUserProgressUseCase,
    getGoProgress: GetGoUserProgressUseCase,
    private val animalSettingsStore: AnimalSettingsStore
) : ViewModel() {

    val chessProgress: Flow<UserProgress>        = getChessProgress.observe()
    val xiangqiProgress: Flow<UserProgress>      = getXiangqiProgress.observe()
    val animalProgress: Flow<UserProgress>       = getAnimalProgress.observe()
    val goProgress: Flow<UserProgress>           = getGoProgress.observe()
    val animalDifficulty: Flow<AnimalDifficulty> = animalSettingsStore.observeDifficulty()

    fun setDifficulty(d: AnimalDifficulty) {
        viewModelScope.launch { animalSettingsStore.setDifficulty(d) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                HomeViewModel(
                    app.container.getUserProgressUseCase,
                    app.container.getXiangqiUserProgressUseCase,
                    app.container.getAnimalUserProgressUseCase,
                    app.container.getGoUserProgressUseCase,
                    app.container.animalSettingsStore
                )
            }
        }
    }
}
