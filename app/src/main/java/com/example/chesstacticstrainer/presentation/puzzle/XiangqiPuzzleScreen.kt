package com.example.chesstacticstrainer.presentation.puzzle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chesstacticstrainer.presentation.LocalStrings
import com.example.chesstacticstrainer.presentation.board.XiangqiBoardComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiangqiPuzzleScreen(
    onNavigateBack: () -> Unit,
    viewModel: XiangqiPuzzleViewModel = viewModel(factory = XiangqiPuzzleViewModel.Factory)
) {
    val strings  = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.xiangqiPuzzleTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (uiState is XiangqiPuzzleUiState.Active &&
                        (uiState as XiangqiPuzzleUiState.Active).result == null
                    ) {
                        IconButton(onClick = { viewModel.onHintRequested() }) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = strings.hint)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color    = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = uiState) {
                    is XiangqiPuzzleUiState.Loading -> {
                        Spacer(Modifier.weight(1f))
                        CircularProgressIndicator()
                        Spacer(Modifier.weight(1f))
                    }
                    is XiangqiPuzzleUiState.Error -> {
                        Spacer(Modifier.weight(1f))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNextPuzzle() }) { Text(strings.retry) }
                        Spacer(Modifier.weight(1f))
                    }
                    is XiangqiPuzzleUiState.Active -> {
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${strings.ratingPrefix}${state.rating}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                state.themes.firstOrNull()
                                    ?.let { strings.xiangqiThemeDisplayName(it) }
                                    ?: strings.findBestMove,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        XiangqiBoardComponent(
                            state = state.boardState,
                            onSquareTapped = { sq ->
                                if (state.result == null) viewModel.onSquareTapped(sq)
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        when (state.result) {
                            null -> if (state.lastMoveWasCorrect) {
                                Text(
                                    strings.goodMoveFindNext,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    strings.findWinningMove,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            PuzzleResult.COMPLETE -> XiangqiResultCard(
                                isSuccess   = true,
                                title       = state.explanation?.theme
                                    ?.let { strings.xiangqiThemeDisplayName(it) }
                                    ?: strings.excellent,
                                description = state.explanation?.theme
                                    ?.let { strings.xiangqiTacticDescription(it) }
                                    ?: strings.puzzleComplete,
                                onNext      = viewModel::onNextPuzzle
                            )
                            PuzzleResult.WRONG -> XiangqiWrongMoveCard(
                                title           = strings.incorrect,
                                description     = strings.notRightMove,
                                showingSolution = state.showingSolution,
                                onTryAgain      = viewModel::onTryAgain,
                                onShowSolution  = viewModel::onShowSolution,
                                onNext          = viewModel::onNextPuzzle
                            )
                            else -> Unit
                        }

                        if (state.result != null) {
                            Spacer(Modifier.height(12.dp))
                            XiangqiAiSection(
                                state = state,
                                onRequestAi = { viewModel.onAiExplanationRequested(strings.isEnglish) }
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun XiangqiResultCard(
    isSuccess: Boolean, title: String, description: String, onNext: () -> Unit
) {
    val strings = LocalStrings.current
    val containerColor = if (isSuccess)
        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val onContainerColor = if (isSuccess)
        MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onContainerColor)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = onContainerColor)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) { Text(strings.nextXiangqi) }
        }
    }
}

@Composable
private fun XiangqiWrongMoveCard(
    title: String, description: String, showingSolution: Boolean,
    onTryAgain: () -> Unit, onShowSolution: () -> Unit, onNext: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onTryAgain, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(strings.tryAgain)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onShowSolution, modifier = Modifier.fillMaxWidth(),
                enabled = !showingSolution,
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Icon(Icons.Filled.Visibility, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(if (showingSolution) strings.solutionShownOnBoard else strings.showSolution)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onNext, modifier = Modifier.fillMaxWidth(),
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            ) { Text(strings.skipXiangqi) }
        }
    }
}

@Composable
private fun XiangqiAiSection(state: XiangqiPuzzleUiState.Active, onRequestAi: () -> Unit) {
    val strings = LocalStrings.current
    when {
        state.aiExplanation != null -> {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.medium,
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AutoAwesome, null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text(strings.aiCoach, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())) {
                            Text(state.aiExplanation, style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
        state.isLoadingAi -> {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.secondary)
                        Text(strings.aiCoachThinking, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        state.aiAvailable -> {
            OutlinedButton(
                onClick = onRequestAi, modifier = Modifier.fillMaxWidth(),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(strings.explainWithAi)
            }
        }
    }
}
