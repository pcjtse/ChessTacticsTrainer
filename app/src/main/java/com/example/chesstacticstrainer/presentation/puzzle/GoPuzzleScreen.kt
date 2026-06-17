package com.example.chesstacticstrainer.presentation.puzzle

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.example.chesstacticstrainer.presentation.LocalStrings
import com.example.chesstacticstrainer.presentation.board.GoBoardComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoPuzzleScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoPuzzleViewModel = viewModel(factory = GoPuzzleViewModel.Factory)
) {
    val strings            = LocalStrings.current
    val uiState           by viewModel.uiState.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.goPuzzleTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (uiState is GoPuzzleUiState.Active &&
                        (uiState as GoPuzzleUiState.Active).result == null
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoDifficultySelector(
                    selected   = selectedDifficulty,
                    onSelected = viewModel::onDifficultySelected,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = uiState) {
                        is GoPuzzleUiState.Loading -> {
                            Spacer(Modifier.height(80.dp))
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text(
                                strings.goLoadingText(strings.goDifficultyName(selectedDifficulty)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        is GoPuzzleUiState.Error -> {
                            Spacer(Modifier.height(80.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadNextPuzzle() }) { Text(strings.goRetry) }
                        }

                        is GoPuzzleUiState.Active -> {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${strings.goDifficultyPrefix}${"★".repeat(state.difficulty.coerceIn(1, 5))}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.puzzleName,
                                    style      = MaterialTheme.typography.labelLarge,
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${strings.goBlackCaptures}${state.boardState.capturedByBlack}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${strings.goWhiteCaptures}${state.boardState.capturedByWhite}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            GoBoardComponent(
                                state         = state.boardState,
                                onPointTapped = { pt ->
                                    if (state.result == null) viewModel.onPointTapped(pt)
                                }
                            )

                            Spacer(Modifier.height(16.dp))

                            when (state.result) {
                                null -> if (state.showingSolution) {
                                    Text(
                                        strings.goSolutionShown,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick  = viewModel::onTryAgain,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(6.dp))
                                            Text(strings.goTryAgain)
                                        }
                                        Button(
                                            onClick  = viewModel::onNextPuzzle,
                                            modifier = Modifier.weight(1f)
                                        ) { Text(strings.goSkip) }
                                    }
                                } else if (state.lastMoveWasCorrect) {
                                    Text(
                                        strings.goGoodMove,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        strings.goFindSolution,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                PuzzleResult.COMPLETE -> GoResultCard(
                                    isSuccess      = true,
                                    title          = strings.goCorrect,
                                    message        = strings.goCongrats,
                                    onNext         = viewModel::onNextPuzzle,
                                    onTryAgain     = null,
                                    onShowSolution = null
                                )

                                PuzzleResult.WRONG -> GoResultCard(
                                    isSuccess      = false,
                                    title          = strings.goWrongMove,
                                    message        = strings.goThinkAgain,
                                    onNext         = viewModel::onNextPuzzle,
                                    onTryAgain     = viewModel::onTryAgain,
                                    onShowSolution = viewModel::onShowSolution
                                )

                                else -> Unit
                            }

                            if (state.result != null && state.aiAvailable) {
                                Spacer(Modifier.height(12.dp))
                                GoAiCoachCard(
                                    isLoading   = state.isLoadingAi,
                                    explanation = state.aiExplanation,
                                    onRequestAi = viewModel::onAiExplanationRequested
                                )
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoDifficultySelector(
    selected: GoDifficulty,
    onSelected: (GoDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Row(
        modifier              = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        GoDifficulty.entries.forEach { level ->
            FilterChip(
                selected  = level == selected,
                onClick   = { onSelected(level) },
                label     = { Text(strings.goDifficultyName(level), style = MaterialTheme.typography.labelMedium) },
                colors    = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun GoResultCard(
    isSuccess: Boolean,
    title: String,
    message: String,
    onNext: () -> Unit,
    onTryAgain: (() -> Unit)?,
    onShowSolution: (() -> Unit)?
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
            Text(title,   style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onContainerColor)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = onContainerColor)
            Spacer(Modifier.height(12.dp))

            if (onTryAgain != null) {
                Button(
                    onClick  = onTryAgain,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(strings.goTryAgain)
                }
                Spacer(Modifier.height(8.dp))
                if (onShowSolution != null) {
                    OutlinedButton(
                        onClick  = onShowSolution,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = onContainerColor)
                    ) { Text(strings.goShowSolution) }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick  = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = onContainerColor)
                ) { Text(strings.goSkip) }
            } else {
                Button(
                    onClick  = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) { Text(strings.goNext) }
            }
        }
    }
}

@Composable
private fun GoAiCoachCard(
    isLoading: Boolean,
    explanation: String?,
    onRequestAi: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                strings.goAiCoach,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            when {
                isLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                        Text(strings.goAiAnalyzing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                explanation != null -> {
                    Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    Button(onClick = onRequestAi, modifier = Modifier.fillMaxWidth()) { Text(strings.goAskAi) }
                }
            }
        }
    }
}
