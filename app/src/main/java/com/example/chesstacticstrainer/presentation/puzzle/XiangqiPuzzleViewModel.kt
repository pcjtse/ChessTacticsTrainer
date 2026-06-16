package com.example.chesstacticstrainer.presentation.puzzle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.engine.XiangqiEngine
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.model.XiangqiBoardState
import com.example.chesstacticstrainer.domain.model.XiangqiColor
import com.example.chesstacticstrainer.domain.model.XiangqiPieceType
import com.example.chesstacticstrainer.domain.usecase.GetNextXiangqiPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateXiangqiStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.ValidateXiangqiMoveUseCase
import com.example.chesstacticstrainer.presentation.board.XiangqiUiBoardState
import com.example.chesstacticstrainer.presentation.board.XiangqiUiPiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class XiangqiPuzzleViewModel(
    private val engine: XiangqiEngine,
    private val getNextPuzzle: GetNextXiangqiPuzzleUseCase,
    private val validateMove: ValidateXiangqiMoveUseCase,
    private val getExplanation: GetXiangqiExplanationUseCase,
    private val updateStreak: UpdateXiangqiStreakUseCase,
    private val getAiExplanation: GetXiangqiAiExplanationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<XiangqiPuzzleUiState>(XiangqiPuzzleUiState.Loading)
    val uiState: StateFlow<XiangqiPuzzleUiState> = _uiState.asStateFlow()

    private var currentBoard: XiangqiBoardState? = null
    private var currentPuzzle: Puzzle? = null
    private var moveIndex = 0
    private var boardBeforeWrongMove: XiangqiBoardState? = null
    private var isFlipped = false

    init { loadNextPuzzle() }

    fun loadNextPuzzle() {
        boardBeforeWrongMove = null
        isFlipped = false
        _uiState.value = XiangqiPuzzleUiState.Loading
        viewModelScope.launch {
            getNextPuzzle()
                .onSuccess { (puzzle, fen) ->
                    currentPuzzle = puzzle
                    currentBoard  = engine.loadFen(fen)
                    moveIndex     = 0
                    isFlipped     = false  // Xiangqi convention: Red always at bottom
                    Log.d("CTT-XQ", "Puzzle ${puzzle.id} sideToMove=${engine.sideToMove(currentBoard!!)}")
                    _uiState.value = XiangqiPuzzleUiState.Active(
                        puzzleId   = puzzle.id,
                        rating     = puzzle.rating,
                        boardState = currentBoard!!.toUiState(),
                        themes     = puzzle.themes,
                        aiAvailable = getAiExplanation.isAvailable
                    )
                }
                .onFailure { e ->
                    _uiState.value = XiangqiPuzzleUiState.Error(e.message ?: "无法加载题目")
                }
        }
    }

    fun onSquareTapped(square: String) {
        val state  = _uiState.value as? XiangqiPuzzleUiState.Active ?: return
        if (state.result != null) return
        val board  = currentBoard ?: return
        val selected = state.boardState.selectedSquare

        when {
            selected == null -> selectIfOwn(square, state, board)
            square == selected -> deselect(state)
            state.boardState.legalTargets.contains(square) ->
                viewModelScope.launch { submitMove(selected, square, state) }
            else -> selectIfOwn(square, state, board)
        }
    }

    private fun selectIfOwn(square: String, state: XiangqiPuzzleUiState.Active, board: XiangqiBoardState) {
        val piece = board.pieceMap[square] ?: run { deselect(state); return }
        if (piece.color != engine.sideToMove(board)) { deselect(state); return }
        val targets = engine.getLegalMovesFromSquare(board, square).map { it.toSquare }
        _uiState.value = state.copy(
            hintSquare = null,
            boardState = state.boardState.copy(selectedSquare = square, legalTargets = targets)
        )
    }

    private fun deselect(state: XiangqiPuzzleUiState.Active) {
        _uiState.value = state.copy(
            hintSquare = null,
            boardState = state.boardState.copy(selectedSquare = null, legalTargets = emptyList())
        )
    }

    private suspend fun submitMove(from: String, to: String, state: XiangqiPuzzleUiState.Active) {
        val board  = currentBoard ?: return
        val puzzle = currentPuzzle ?: return
        val uci    = "$from$to"

        val result = withContext(Dispatchers.Default) {
            validateMove(board, uci, puzzle.solutionMoves, moveIndex)
        }
        currentBoard = result.newBoardState

        if (!result.isCorrect) {
            boardBeforeWrongMove = board
            runCatching { updateStreak(solved = false) }
            _uiState.value = state.copy(
                hintSquare  = null,
                boardState  = result.newBoardState.toUiState(from, to),
                result      = PuzzleResult.WRONG,
                explanation = getExplanation(puzzle.themes, isCorrect = false),
                aiAvailable = getAiExplanation.isAvailable,
                showingSolution = false,
                lastMoveWasCorrect = false
            )
            return
        }

        moveIndex++

        val reply = result.computerReply
        if (reply != null) {
            val replyLegal = withContext(Dispatchers.Default) {
                runCatching { engine.isMoveLegal(result.newBoardState, reply) }.getOrDefault(false)
            }
            if (!replyLegal) {
                Log.e("CTT-XQ", "Computer reply $reply illegal — evicting puzzle ${puzzle.id}")
                runCatching { getNextPuzzle.removeBroken(puzzle.id) }
                loadNextPuzzle()
                return
            }
            val afterReply = withContext(Dispatchers.Default) {
                engine.applyMove(result.newBoardState, reply)
            }
            if (afterReply.fen == result.newBoardState.fen) {
                Log.e("CTT-XQ", "Computer reply $reply failed to apply — evicting puzzle ${puzzle.id}")
                runCatching { getNextPuzzle.removeBroken(puzzle.id) }
                loadNextPuzzle()
                return
            }
            currentBoard = afterReply
            _uiState.value = state.copy(
                hintSquare = null,
                boardState = afterReply.toUiState(reply.take(2), reply.substring(2, 4)),
                moveIndex  = moveIndex,
                lastMoveWasCorrect = true
            )
        } else {
            runCatching { updateStreak(solved = true) }
            runCatching { getNextPuzzle.markSolved(puzzle.id) }
            _uiState.value = state.copy(
                hintSquare  = null,
                boardState  = result.newBoardState.toUiState(from, to),
                result      = PuzzleResult.COMPLETE,
                explanation = getExplanation(puzzle.themes, isCorrect = true),
                aiAvailable = getAiExplanation.isAvailable,
                lastMoveWasCorrect = false
            )
        }
    }

    fun onHintRequested() {
        val state    = _uiState.value as? XiangqiPuzzleUiState.Active ?: return
        val puzzle   = currentPuzzle ?: return
        val board    = currentBoard ?: return
        val hintFrom = puzzle.solutionMoves.getOrNull(moveIndex * 2)?.take(2) ?: return
        val targets  = engine.getLegalMovesFromSquare(board, hintFrom).map { it.toSquare }
        _uiState.value = state.copy(
            hintSquare = hintFrom,
            boardState = state.boardState.copy(selectedSquare = hintFrom, legalTargets = targets)
        )
    }

    fun onAiExplanationRequested() {
        val state  = _uiState.value as? XiangqiPuzzleUiState.Active ?: return
        if (state.result == null || state.isLoadingAi || state.aiExplanation != null) return
        val puzzle = currentPuzzle ?: return
        val requestedForId = puzzle.id

        // Reset the board to the original puzzle position so it matches the AI explanation.
        // The AI always describes moves from puzzle.fen (the starting position), but after a
        // wrong move the board shows a different state — resetting keeps them in sync.
        val originalBoard = engine.loadFen(puzzle.fen)
        currentBoard = originalBoard
        _uiState.value = state.copy(
            boardState  = originalBoard.toUiState(),
            isLoadingAi = true
        )

        viewModelScope.launch {
            getAiExplanation(
                fen           = puzzle.fen,
                themes        = puzzle.themes,
                solutionMoves = puzzle.solutionMoves,
                rating        = puzzle.rating,
                playerWon     = state.result == PuzzleResult.COMPLETE
            ).onSuccess { text ->
                val current = _uiState.value as? XiangqiPuzzleUiState.Active ?: return@onSuccess
                if (current.puzzleId != requestedForId) return@onSuccess
                _uiState.value = current.copy(aiExplanation = text, isLoadingAi = false)
            }.onFailure { e ->
                val current = _uiState.value as? XiangqiPuzzleUiState.Active ?: return@onFailure
                if (current.puzzleId != requestedForId) return@onFailure
                _uiState.value = current.copy(
                    aiExplanation = "无法加载AI解说：${e.message}",
                    isLoadingAi = false
                )
            }
        }
    }

    fun onTryAgain() {
        val board = boardBeforeWrongMove ?: return
        val state = _uiState.value as? XiangqiPuzzleUiState.Active ?: return
        currentBoard = board
        _uiState.value = state.copy(
            boardState    = board.toUiState(),
            result        = null,
            hintSquare    = null,
            showingSolution = false,
            aiExplanation = null,
            isLoadingAi   = false
        )
    }

    fun onShowSolution() {
        val board  = boardBeforeWrongMove ?: return
        val state  = _uiState.value as? XiangqiPuzzleUiState.Active ?: return
        val puzzle = currentPuzzle ?: return
        val expected = puzzle.solutionMoves.getOrNull(moveIndex * 2) ?: return
        val fromSq   = expected.take(2)
        val toSq     = expected.substring(2, 4)
        if (board.pieceMap[fromSq] == null) {
            Log.e("CTT-XQ", "showSolution: no piece at $fromSq — evicting ${puzzle.id}")
            runCatching { viewModelScope.launch { getNextPuzzle.removeBroken(puzzle.id) } }
            loadNextPuzzle()
            return
        }
        currentBoard = board
        _uiState.value = state.copy(
            boardState = board.toUiState().copy(
                selectedSquare = fromSq,
                legalTargets   = listOf(toSq)
            ),
            showingSolution = true
        )
    }

    fun onNextPuzzle() = loadNextPuzzle()

    // ── Board state conversion ────────────────────────────────────────────────

    private fun XiangqiBoardState.toUiState(lastFrom: String? = null, lastTo: String? = null): XiangqiUiBoardState {
        val side = sideToMove
        val checkedSq = if (engine.isInCheck(this)) {
            pieceMap.entries.find { (_, p) -> p.color == side && p.type == XiangqiPieceType.GENERAL }?.key
        } else null
        return XiangqiUiBoardState(
            pieceMap             = pieceMap.mapValues { (_, p) -> XiangqiUiPiece(p.type, p.color) },
            checkedGeneralSquare = checkedSq,
            lastMoveFrom         = lastFrom,
            lastMoveTo           = lastTo,
            isFlipped            = this@XiangqiPuzzleViewModel.isFlipped
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                XiangqiPuzzleViewModel(
                    engine         = app.container.xiangqiEngine,
                    getNextPuzzle  = app.container.getNextXiangqiPuzzleUseCase,
                    validateMove   = app.container.validateXiangqiMoveUseCase,
                    getExplanation = app.container.getXiangqiExplanationUseCase,
                    updateStreak   = app.container.updateXiangqiStreakUseCase,
                    getAiExplanation = app.container.getXiangqiAiExplanationUseCase
                )
            }
        }
    }
}
