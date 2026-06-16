package com.example.chesstacticstrainer.presentation.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.engine.AnimalChessEngine
import com.example.chesstacticstrainer.domain.model.AnimalChessBoardState
import com.example.chesstacticstrainer.domain.model.AnimalColor
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.engine.AnimalChessAI
import com.example.chesstacticstrainer.engine.AnimalChessEngineImpl
import com.example.chesstacticstrainer.presentation.board.AnimalUiBoardState
import com.example.chesstacticstrainer.presentation.board.AnimalUiPiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimalGameViewModel(
    private val engine: AnimalChessEngine,
    private val ai: AnimalChessAI,
    private val difficulty: AnimalDifficulty
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimalGameUiState>(AnimalGameUiState.Loading)
    val uiState: StateFlow<AnimalGameUiState> = _uiState.asStateFlow()

    private var board: AnimalChessBoardState = engine.loadFen(AnimalChessEngineImpl.STARTING_FEN)
    private val playerColor = AnimalColor.RED   // player always Red
    private val capturedByPlayer = mutableListOf<String>()
    private val capturedByAi = mutableListOf<String>()

    init { startGame() }

    private fun startGame() {
        board = engine.loadFen(AnimalChessEngineImpl.STARTING_FEN)
        capturedByPlayer.clear()
        capturedByAi.clear()
        _uiState.value = AnimalGameUiState.Playing(
            boardState   = board.toUiState(),
            playerColor  = playerColor,
            currentTurn  = AnimalColor.RED,
            difficulty   = difficulty
        )
    }

    fun onSquareTapped(square: String) {
        val state = _uiState.value as? AnimalGameUiState.Playing ?: return
        if (state.isAiThinking || state.currentTurn != playerColor) return

        val selected = state.boardState.selectedSquare
        when {
            selected == null -> trySelect(square, state)
            square == selected -> deselect(state)
            state.boardState.legalTargets.contains(square) -> applyPlayerMove(selected, square, state)
            else -> trySelect(square, state)
        }
    }

    private fun trySelect(square: String, state: AnimalGameUiState.Playing) {
        val piece = board.pieceMap[square] ?: run { deselect(state); return }
        if (piece.color != playerColor) { deselect(state); return }
        val targets = engine.getLegalMovesFromSquare(board, square).map { it.toSquare }
        _uiState.value = state.copy(
            boardState = state.boardState.copy(selectedSquare = square, legalTargets = targets)
        )
    }

    private fun deselect(state: AnimalGameUiState.Playing) {
        _uiState.value = state.copy(
            boardState = state.boardState.copy(selectedSquare = null, legalTargets = emptyList())
        )
    }

    private fun applyPlayerMove(from: String, to: String, state: AnimalGameUiState.Playing) {
        val captured = board.pieceMap[to]
        board = engine.applyMove(board, "$from$to")
        captured?.let { capturedByPlayer.add(AnimalUiPiece(it.type, it.color).emoji) }

        if (engine.isGameOver(board)) {
            _uiState.value = AnimalGameUiState.GameOver(
                boardState = board.toUiState(from, to),
                playerWon  = true,
                difficulty = difficulty
            )
            return
        }

        _uiState.value = state.copy(
            boardState       = board.toUiState(from, to),
            currentTurn      = AnimalColor.BLUE,
            isAiThinking     = true,
            capturedByPlayer = capturedByPlayer.toList(),
            capturedByAi     = capturedByAi.toList()
        )
        scheduleAiMove()
    }

    private fun scheduleAiMove() {
        viewModelScope.launch {
            delay(400)  // brief pause so AI doesn't feel instant
            val move = withContext(Dispatchers.Default) {
                ai.bestMove(board, difficulty.depth)
            } ?: run {
                // AI has no moves — player wins
                (_uiState.value as? AnimalGameUiState.Playing)?.let {
                    _uiState.value = AnimalGameUiState.GameOver(
                        boardState = board.toUiState(),
                        playerWon  = true,
                        difficulty = difficulty
                    )
                }
                return@launch
            }

            val from     = move.take(2)
            val to       = move.substring(2, 4)
            val captured = board.pieceMap[to]
            board = engine.applyMove(board, move)
            captured?.let { capturedByAi.add(AnimalUiPiece(it.type, it.color).emoji) }

            val playingState = _uiState.value as? AnimalGameUiState.Playing

            if (engine.isGameOver(board)) {
                _uiState.value = AnimalGameUiState.GameOver(
                    boardState = board.toUiState(from, to),
                    playerWon  = false,
                    difficulty = difficulty
                )
                return@launch
            }

            _uiState.value = (playingState ?: AnimalGameUiState.Playing(
                boardState   = board.toUiState(from, to),
                playerColor  = playerColor,
                currentTurn  = AnimalColor.RED,
                difficulty   = difficulty
            )).copy(
                boardState       = board.toUiState(from, to),
                currentTurn      = AnimalColor.RED,
                isAiThinking     = false,
                capturedByPlayer = capturedByPlayer.toList(),
                capturedByAi     = capturedByAi.toList()
            )
        }
    }

    fun onNewGame() = startGame()

    private fun AnimalChessBoardState.toUiState(lastFrom: String? = null, lastTo: String? = null) =
        AnimalUiBoardState(
            pieceMap     = pieceMap.mapValues { (_, p) -> AnimalUiPiece(p.type, p.color) },
            lastMoveFrom = lastFrom,
            lastMoveTo   = lastTo
        )

    companion object {
        fun factory(difficulty: AnimalDifficulty): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                val engine = app.container.animalEngine
                AnimalGameViewModel(
                    engine     = engine,
                    ai         = AnimalChessAI(engine),
                    difficulty = difficulty
                )
            }
        }
    }
}
