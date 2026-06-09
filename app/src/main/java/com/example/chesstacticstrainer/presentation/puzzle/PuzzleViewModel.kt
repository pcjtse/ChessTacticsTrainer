package com.example.chesstacticstrainer.presentation.puzzle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.model.BoardState
import com.example.chesstacticstrainer.domain.model.Piece
import com.example.chesstacticstrainer.domain.model.PieceColor
import com.example.chesstacticstrainer.domain.model.PieceType
import com.example.chesstacticstrainer.domain.model.Puzzle
import com.example.chesstacticstrainer.domain.usecase.GetAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.ValidateMoveUseCase
import com.example.chesstacticstrainer.presentation.board.UiBoardState
import com.example.chesstacticstrainer.presentation.board.UiPiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PuzzleViewModel(
    private val engine: ChessEngine,
    private val getNextPuzzle: GetNextPuzzleUseCase,
    private val validateMove: ValidateMoveUseCase,
    private val getExplanation: GetExplanationUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val getAiExplanation: GetAiExplanationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PuzzleUiState>(PuzzleUiState.Loading)
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    private var currentBoard: BoardState? = null
    private var currentPuzzle: Puzzle? = null
    private var moveIndex = 0
    private var boardBeforeWrongMove: BoardState? = null
    private var isFlipped = false

    init {
        loadNextPuzzle()
    }

    fun loadNextPuzzle() {
        boardBeforeWrongMove = null
        isFlipped = false
        _uiState.value = PuzzleUiState.Loading
        viewModelScope.launch {
            getNextPuzzle()
                .onSuccess { (puzzle, startFen) ->
                    currentPuzzle = puzzle
                    currentBoard = engine.loadFen(startFen)
                    moveIndex = 0
                    // Flip board so the player's pieces are always at the bottom
                    isFlipped = engine.sideToMove(currentBoard!!) == "black"
                    Log.d("CTT", "Puzzle loaded: id=${puzzle.id} playerSide=${if (isFlipped) "black" else "white"} moveIndex=$moveIndex")
                    _uiState.value = PuzzleUiState.Active(
                        puzzleId = puzzle.id,
                        rating = puzzle.rating,
                        boardState = currentBoard!!.toUiState(),
                        themes = puzzle.themes,
                        aiAvailable = getAiExplanation.isAvailable
                    )
                }
                .onFailure { e ->
                    _uiState.value = PuzzleUiState.Error(e.message ?: "Failed to load puzzle")
                }
        }
    }

    fun onSquareTapped(square: String) {
        val state = _uiState.value as? PuzzleUiState.Active ?: return
        if (state.result != null) return
        val board = currentBoard ?: return
        val selected = state.boardState.selectedSquare

        when {
            selected == null -> selectPieceIfOwn(square, state, board)
            square == selected -> deselect(state)
            state.boardState.legalTargets.contains(square) -> viewModelScope.launch { submitMove(selected, square, state) }
            else -> selectPieceIfOwn(square, state, board)
        }
    }

    private fun selectPieceIfOwn(square: String, state: PuzzleUiState.Active, board: BoardState) {
        val piece = board.pieceMap[square] ?: run { deselect(state); return }
        if (!piece.matchesSide(engine.sideToMove(board))) { deselect(state); return }
        val targets = engine.getLegalMovesFromSquare(board, square).map { it.toSquare }
        _uiState.value = state.copy(
            boardState = state.boardState.copy(selectedSquare = square, legalTargets = targets)
        )
    }

    private fun deselect(state: PuzzleUiState.Active) {
        _uiState.value = state.copy(
            boardState = state.boardState.copy(selectedSquare = null, legalTargets = emptyList())
        )
    }

    private suspend fun submitMove(from: String, to: String, state: PuzzleUiState.Active) {
        val board = currentBoard ?: return
        val puzzle = currentPuzzle ?: return
        val uci = "$from$to"
        val expected = puzzle.solutionMoves.getOrNull(moveIndex * 2)
        Log.d("CTT", "submitMove: uci=$uci expected=$expected moveIndex=$moveIndex allMoves=${puzzle.solutionMoves}")

        val result = withContext(Dispatchers.Default) {
            validateMove(board, uci, puzzle.solutionMoves, moveIndex)
        }

        currentBoard = result.newBoardState

        if (!result.isCorrect) {
            boardBeforeWrongMove = board
            runCatching { updateStreak(solved = false) }
            _uiState.value = state.copy(
                boardState = result.newBoardState.toUiState(from, to),
                result = PuzzleResult.WRONG,
                explanation = getExplanation(puzzle.themes, isCorrect = false),
                aiAvailable = getAiExplanation.isAvailable,
                showingSolution = false,
                lastMoveWasCorrect = false
            )
            return
        }

        moveIndex++

        val computerReply = result.computerReply
        if (computerReply != null) {
            val afterReply = withContext(Dispatchers.Default) {
                engine.applyMove(result.newBoardState, computerReply)
            }
            currentBoard = afterReply
            _uiState.value = state.copy(
                boardState = afterReply.toUiState(
                    computerReply.substring(0, 2),
                    computerReply.substring(2, 4)
                ),
                moveIndex = moveIndex,
                lastMoveWasCorrect = true
            )
        } else {
            runCatching { updateStreak(solved = true) }
            runCatching { getNextPuzzle.markSolved(puzzle.id) }
            _uiState.value = state.copy(
                boardState = result.newBoardState.toUiState(from, to),
                result = PuzzleResult.COMPLETE,
                explanation = getExplanation(puzzle.themes, isCorrect = true),
                aiAvailable = getAiExplanation.isAvailable,
                lastMoveWasCorrect = false
            )
        }
    }

    fun onHintRequested() {
        val state = _uiState.value as? PuzzleUiState.Active ?: return
        val puzzle = currentPuzzle ?: return
        val hintFrom = puzzle.solutionMoves.getOrNull(moveIndex * 2)?.take(2) ?: return
        _uiState.value = state.copy(hintSquare = hintFrom)
    }

    fun onAiExplanationRequested() {
        val state = _uiState.value as? PuzzleUiState.Active ?: return
        if (state.result == null || state.isLoadingAi || state.aiExplanation != null) return
        val puzzle = currentPuzzle ?: return

        _uiState.value = state.copy(isLoadingAi = true)
        viewModelScope.launch {
            getAiExplanation(
                themes = puzzle.themes,
                solutionMoves = puzzle.solutionMoves,
                rating = puzzle.rating,
                playerWon = state.result == PuzzleResult.COMPLETE
            )
                .onSuccess { text ->
                    (_uiState.value as? PuzzleUiState.Active)?.let {
                        _uiState.value = it.copy(aiExplanation = text, isLoadingAi = false)
                    }
                }
                .onFailure { e ->
                    (_uiState.value as? PuzzleUiState.Active)?.let {
                        _uiState.value = it.copy(
                            aiExplanation = "Could not load AI explanation: ${e.message}",
                            isLoadingAi = false
                        )
                    }
                }
        }
    }

    fun onTryAgain() {
        val board = boardBeforeWrongMove ?: return
        val state = _uiState.value as? PuzzleUiState.Active ?: return
        currentBoard = board
        _uiState.value = state.copy(
            boardState = board.toUiState(),
            result = null,
            hintSquare = null,
            showingSolution = false,
            aiExplanation = null,
            isLoadingAi = false
        )
    }

    fun onShowSolution() {
        val board = boardBeforeWrongMove ?: return
        val state = _uiState.value as? PuzzleUiState.Active ?: return
        val puzzle = currentPuzzle ?: return
        val expectedMove = puzzle.solutionMoves.getOrNull(moveIndex * 2) ?: return
        val fromSq = expectedMove.take(2)
        val toSq = expectedMove.substring(2, 4)
        val pieceAtFrom = board.pieceMap[fromSq]
        Log.d("CTT", "showSolution: moveIndex=$moveIndex expectedMove=$expectedMove fromSq=$fromSq toSq=$toSq")
        Log.d("CTT", "  pieceAtFrom=$pieceAtFrom boardFen=${board.fen}")
        Log.d("CTT", "  allSolutionMoves=${puzzle.solutionMoves}")
        currentBoard = board
        _uiState.value = state.copy(
            boardState = board.toUiState().copy(
                selectedSquare = fromSq,
                legalTargets = listOf(toSq)
            ),
            showingSolution = true
        )
    }

    fun onNextPuzzle() = loadNextPuzzle()

    private fun BoardState.toUiState(lastFrom: String? = null, lastTo: String? = null) = UiBoardState(
        pieceMap = pieceMap.mapValues { (_, p) -> p.toUi() },
        checkedKingSquare = if (isCheck) findKingSquare() else null,
        lastMoveFrom = lastFrom,
        lastMoveTo = lastTo,
        isFlipped = this@PuzzleViewModel.isFlipped
    )

    private fun BoardState.findKingSquare(): String? {
        val side = engine.sideToMove(this)
        val kingColor = if (side == "white") PieceColor.WHITE else PieceColor.BLACK
        return pieceMap.entries.find { (_, p) -> p.color == kingColor && p.type == PieceType.KING }?.key
    }

    private fun Piece.toUi() = UiPiece(type, color)

    private fun Piece.matchesSide(side: String) =
        (side == "white" && color == PieceColor.WHITE) || (side == "black" && color == PieceColor.BLACK)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                PuzzleViewModel(
                    engine = app.container.engine,
                    getNextPuzzle = app.container.getNextPuzzleUseCase,
                    validateMove = app.container.validateMoveUseCase,
                    getExplanation = app.container.getExplanationUseCase,
                    updateStreak = app.container.updateStreakUseCase,
                    getAiExplanation = app.container.getAiExplanationUseCase
                )
            }
        }
    }
}
