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
import com.example.chesstacticstrainer.presentation.board.GoBoardComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoPuzzleScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoPuzzleViewModel = viewModel(factory = GoPuzzleViewModel.Factory)
) {
    val uiState           by viewModel.uiState.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("围棋死活题") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState is GoPuzzleUiState.Active &&
                        (uiState as GoPuzzleUiState.Active).result == null
                    ) {
                        IconButton(onClick = { viewModel.onHintRequested() }) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = "提示")
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
                // ── Difficulty selector ──────────────────────────────────────
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
                                "正在获取${selectedDifficulty.displayName}题目…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        is GoPuzzleUiState.Error -> {
                            Spacer(Modifier.height(80.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadNextPuzzle() }) { Text("重试") }
                        }

                        is GoPuzzleUiState.Active -> {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "难度：${"★".repeat(state.difficulty.coerceIn(1, 5))}",
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
                                    "黑提：${state.boardState.capturedByBlack}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "白提：${state.boardState.capturedByWhite}",
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
                                    // Solution is highlighted — offer retry or skip
                                    Text(
                                        "答案已显示，可落子或跳过",
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
                                            Text("再试一次")
                                        }
                                        Button(
                                            onClick  = viewModel::onNextPuzzle,
                                            modifier = Modifier.weight(1f)
                                        ) { Text("跳过此题") }
                                    }
                                } else if (state.lastMoveWasCorrect) {
                                    Text(
                                        "好棋！请继续",
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        "黑先，寻找正解",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                PuzzleResult.COMPLETE -> GoResultCard(
                                    isSuccess      = true,
                                    title          = "正解！",
                                    message        = "恭喜，棋子已被提掉。",
                                    onNext         = viewModel::onNextPuzzle,
                                    onTryAgain     = null,
                                    onShowSolution = null
                                )

                                PuzzleResult.WRONG -> GoResultCard(
                                    isSuccess      = false,
                                    title          = "走法有误",
                                    message        = "请再思考一下。",
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

// ── Difficulty selector ────────────────────────────────────────────────────────

@Composable
private fun GoDifficultySelector(
    selected: GoDifficulty,
    onSelected: (GoDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoDifficulty.entries.forEach { level ->
            FilterChip(
                selected  = level == selected,
                onClick   = { onSelected(level) },
                label     = { Text(level.displayName, style = MaterialTheme.typography.labelMedium) },
                colors    = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ── Result card ────────────────────────────────────────────────────────────────

@Composable
private fun GoResultCard(
    isSuccess: Boolean,
    title: String,
    message: String,
    onNext: () -> Unit,
    onTryAgain: (() -> Unit)?,
    onShowSolution: (() -> Unit)?
) {
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
                    Text("再试一次")
                }
                Spacer(Modifier.height(8.dp))
                if (onShowSolution != null) {
                    OutlinedButton(
                        onClick  = onShowSolution,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = onContainerColor)
                    ) { Text("显示答案") }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick  = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = onContainerColor)
                ) { Text("跳过此题") }
            } else {
                Button(
                    onClick  = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) { Text("下一题") }
            }
        }
    }
}

// ── AI coach card ──────────────────────────────────────────────────────────────

@Composable
private fun GoAiCoachCard(
    isLoading: Boolean,
    explanation: String?,
    onRequestAi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "AI围棋教练",
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
                        Text("AI解析中…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                explanation != null -> {
                    Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    Button(onClick = onRequestAi, modifier = Modifier.fillMaxWidth()) { Text("请AI解析此题") }
                }
            }
        }
    }
}
