package com.example.chesstacticstrainer.presentation.puzzle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chesstacticstrainer.ChessTacticsApp
import com.example.chesstacticstrainer.domain.engine.GoEngine
import com.example.chesstacticstrainer.domain.engine.PlacementResult
import com.example.chesstacticstrainer.domain.model.GoBoardState
import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoSgfNode
import com.example.chesstacticstrainer.domain.model.GoStone
import com.example.chesstacticstrainer.domain.usecase.GetGoAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextGoPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateGoStreakUseCase
import com.example.chesstacticstrainer.presentation.board.GoUiBoardState
import com.example.chesstacticstrainer.presentation.board.GoViewport
import com.example.chesstacticstrainer.presentation.board.computeViewport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CTT-GO"

class GoPuzzleViewModel(
    private val engine: GoEngine,
    private val getNextPuzzle: GetNextGoPuzzleUseCase,
    private val updateStreak: UpdateGoStreakUseCase,
    private val getGoAiExplanation: GetGoAiExplanationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoPuzzleUiState>(GoPuzzleUiState.Loading)
    val uiState: StateFlow<GoPuzzleUiState> = _uiState.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow(GoDifficulty.MEDIUM)
    val selectedDifficulty: StateFlow<GoDifficulty> = _selectedDifficulty.asStateFlow()

    private var currentState: GoBoardState? = null
    private var currentPuzzle: GoPuzzle? = null
    private var solutionCandidates: List<GoSgfNode> = emptyList()
    private var stateBeforeWrong: GoBoardState? = null
    private var candidatesBeforeWrong: List<GoSgfNode> = emptyList()
    private var currentViewport: GoViewport? = null

    init {
        viewModelScope.launch {
            _selectedDifficulty.value = getNextPuzzle.getDifficulty()
            loadNextPuzzle()
        }
    }

    fun loadNextPuzzle() {
        _uiState.value = GoPuzzleUiState.Loading
        viewModelScope.launch {
            getNextPuzzle()
                .onSuccess { puzzle ->
                    currentPuzzle      = puzzle
                    currentState       = puzzle.initialState
                    solutionCandidates = puzzle.solutionRoot.children
                    stateBeforeWrong   = null
                    candidatesBeforeWrong = emptyList()
                    val solutionMoves  = puzzle.solutionRoot.children.mapNotNull { it.move }
                    currentViewport    = computeViewport(puzzle.initialState.stones, puzzle.boardSize, solutionMoves)
                    Log.d(TAG, "Loaded puzzle ${puzzle.id} '${puzzle.name}' size=${puzzle.boardSize} diff=${puzzle.difficulty} stones=${puzzle.initialState.stones.size} candidates=${solutionCandidates.size} viewport=${currentViewport}")
                    _uiState.value = GoPuzzleUiState.Active(
                        puzzleId    = puzzle.id,
                        puzzleName  = puzzle.name,
                        difficulty  = puzzle.difficulty,
                        boardState  = puzzle.initialState.toUi(),
                        aiAvailable = getGoAiExplanation.isAvailable
                    )
                }
                .onFailure { e ->
                    _uiState.value = GoPuzzleUiState.Error(e.message ?: "无法加载题目")
                }
        }
    }

    fun onDifficultySelected(difficulty: GoDifficulty) {
        if (_selectedDifficulty.value == difficulty) return
        _selectedDifficulty.value = difficulty
        viewModelScope.launch {
            getNextPuzzle.setDifficulty(difficulty)
            loadNextPuzzle()
        }
    }

    fun onPointTapped(point: GoPoint) {
        val state  = _uiState.value as? GoPuzzleUiState.Active ?: return
        if (state.result != null) return
        val puzzle = currentPuzzle ?: return
        val board  = currentState ?: return

        val match = solutionCandidates.firstOrNull { it.move == point }

        if (match == null || !match.isMainLine) {
            Log.d(TAG, "onPointTapped: WRONG at $point — candidates=${solutionCandidates.size}, mainLine=${solutionCandidates.firstOrNull { it.isMainLine }?.move}")
            viewModelScope.launch { runCatching { updateStreak(solved = false) } }
            stateBeforeWrong      = board
            candidatesBeforeWrong = solutionCandidates
            _uiState.value = state.copy(
                boardState         = state.boardState.copy(hintPoint = null),
                hintPoint          = null,
                result             = PuzzleResult.WRONG,
                lastMoveWasCorrect = false,
                showingSolution    = false,
                aiExplanation      = null
            )
            return
        }

        viewModelScope.launch {
            val placement = withContext(Dispatchers.Default) {
                engine.placeStone(board, point, puzzle.playerColor)
            }
            if (placement !is PlacementResult.Success) {
                Log.w(TAG, "Solution move $point illegal in engine — skipping puzzle")
                loadNextPuzzle()
                return@launch
            }
            currentState = placement.newState

            if (match.children.isEmpty()) {
                runCatching { updateStreak(solved = true) }
                runCatching { getNextPuzzle.markSolved(puzzle.id) }
                _uiState.value = state.copy(
                    hintPoint  = null,
                    boardState = placement.newState.toUi(lastMove = point),
                    result     = PuzzleResult.COMPLETE
                )
                return@launch
            }

            val firstChild = match.children.first()
            if (firstChild.color == puzzle.playerColor.opposite() && firstChild.move != null) {
                val afterPlayer = placement.newState
                _uiState.value = state.copy(
                    hintPoint          = null,
                    boardState         = afterPlayer.toUi(lastMove = point),
                    lastMoveWasCorrect = true
                )

                delay(450L)

                val oppPlacement = withContext(Dispatchers.Default) {
                    engine.placeStone(afterPlayer, firstChild.move, firstChild.color)
                }
                if (oppPlacement is PlacementResult.Success) {
                    currentState = oppPlacement.newState
                }
                solutionCandidates = firstChild.children

                val afterOpp = (oppPlacement as? PlacementResult.Success)?.newState ?: afterPlayer
                if (firstChild.children.isEmpty()) {
                    runCatching { updateStreak(solved = true) }
                    runCatching { getNextPuzzle.markSolved(puzzle.id) }
                    _uiState.value = (_uiState.value as? GoPuzzleUiState.Active)?.copy(
                        boardState = afterOpp.toUi(lastMove = firstChild.move),
                        result     = PuzzleResult.COMPLETE
                    ) ?: _uiState.value
                } else {
                    _uiState.value = (_uiState.value as? GoPuzzleUiState.Active)?.copy(
                        boardState         = afterOpp.toUi(lastMove = firstChild.move),
                        lastMoveWasCorrect = true
                    ) ?: _uiState.value
                }
            } else {
                solutionCandidates = match.children
                _uiState.value = state.copy(
                    hintPoint          = null,
                    boardState         = placement.newState.toUi(lastMove = point),
                    lastMoveWasCorrect = true
                )
            }
        }
    }

    fun onHintRequested() {
        val state = _uiState.value as? GoPuzzleUiState.Active ?: return
        val hint = solutionCandidates.firstOrNull { it.isMainLine }?.move ?: return
        _uiState.value = state.copy(hintPoint = hint, boardState = state.boardState.copy(hintPoint = hint))
    }

    fun onShowSolution() {
        val state = _uiState.value as? GoPuzzleUiState.Active
            ?: run { Log.w(TAG, "onShowSolution: no Active state"); return }
        if (state.result != PuzzleResult.WRONG) {
            Log.w(TAG, "onShowSolution: result=${state.result}, expected WRONG — aborting"); return
        }
        val board = stateBeforeWrong ?: currentPuzzle?.initialState
            ?: run { Log.w(TAG, "onShowSolution: no board state"); return }
        val mainCandidate = candidatesBeforeWrong.firstOrNull { it.isMainLine }
        Log.d(TAG, "onShowSolution: ${candidatesBeforeWrong.size} candidates, mainCandidate=$mainCandidate")
        val correctMove = mainCandidate?.move
            ?: run { Log.w(TAG, "onShowSolution: correctMove is null (mainCandidate.move=null or no mainLine candidate)"); return }
        Log.d(TAG, "onShowSolution: showing hint at $correctMove, viewport=$currentViewport")
        currentState = board
        solutionCandidates = candidatesBeforeWrong.ifEmpty { currentPuzzle?.solutionRoot?.children ?: emptyList() }
        _uiState.value = state.copy(
            boardState         = board.toUi().copy(hintPoint = correctMove),
            hintPoint          = correctMove,
            result             = null,
            showingSolution    = true,
            lastMoveWasCorrect = false
        )
    }

    fun onTryAgain() {
        val state = _uiState.value as? GoPuzzleUiState.Active ?: return
        val board = stateBeforeWrong ?: currentPuzzle?.initialState ?: return
        currentState = board
        solutionCandidates = candidatesBeforeWrong.ifEmpty { currentPuzzle?.solutionRoot?.children ?: emptyList() }
        _uiState.value = state.copy(
            boardState         = board.toUi(),
            result             = null,
            hintPoint          = null,
            showingSolution    = false,
            lastMoveWasCorrect = false
        )
    }

    // Advance cache index when skipping from WRONG or after solution was shown
    fun onNextPuzzle() {
        val state = _uiState.value as? GoPuzzleUiState.Active
        viewModelScope.launch {
            if (state?.result == PuzzleResult.WRONG || state?.showingSolution == true) {
                currentPuzzle?.let { runCatching { getNextPuzzle.markSolved(it.id) } }
            }
            loadNextPuzzle()
        }
    }

    fun onAiExplanationRequested() {
        val state = _uiState.value as? GoPuzzleUiState.Active ?: return
        if (state.isLoadingAi || state.aiExplanation != null) return
        val puzzle = currentPuzzle ?: return
        val requestedForId = puzzle.id
        _uiState.value = state.copy(isLoadingAi = true)
        viewModelScope.launch {
            getGoAiExplanation(
                puzzleName   = puzzle.name,
                boardSize    = puzzle.boardSize,
                playerColor  = puzzle.playerColor,
                solutionRoot = puzzle.solutionRoot,
                playerWon    = state.result == PuzzleResult.COMPLETE
            )
                .onSuccess { explanation ->
                    val current = _uiState.value as? GoPuzzleUiState.Active ?: return@onSuccess
                    if (current.puzzleId != requestedForId) return@onSuccess
                    _uiState.value = current.copy(aiExplanation = explanation, isLoadingAi = false)
                }
                .onFailure { e ->
                    Log.w(TAG, "AI explanation failed: ${e.message}")
                    val current = _uiState.value as? GoPuzzleUiState.Active ?: return@onFailure
                    if (current.puzzleId != requestedForId) return@onFailure
                    _uiState.value = current.copy(aiExplanation = "AI解析暂时不可用", isLoadingAi = false)
                }
        }
    }

    private fun GoBoardState.toUi(lastMove: GoPoint? = null): GoUiBoardState = GoUiBoardState(
        boardSize       = boardSize,
        stones          = stones,
        lastMove        = lastMove,
        hintPoint       = null,
        capturedByBlack = capturedByBlack,
        capturedByWhite = capturedByWhite,
        viewport        = currentViewport
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChessTacticsApp
                GoPuzzleViewModel(
                    engine             = app.container.goEngine,
                    getNextPuzzle      = app.container.getNextGoPuzzleUseCase,
                    updateStreak       = app.container.updateGoStreakUseCase,
                    getGoAiExplanation = app.container.getGoAiExplanationUseCase
                )
            }
        }
    }
}
