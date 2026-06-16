package com.example.chesstacticstrainer.presentation.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chesstacticstrainer.domain.model.AnimalDifficulty
import com.example.chesstacticstrainer.presentation.board.AnimalBoardComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalGameScreen(
    difficulty: AnimalDifficulty,
    onNavigateBack: () -> Unit,
    viewModel: AnimalGameViewModel = viewModel(factory = AnimalGameViewModel.factory(difficulty))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("斗兽棋 · ${difficulty.label}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onNewGame() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "新游戏")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = uiState) {
                    is AnimalGameUiState.Loading -> {
                        Spacer(Modifier.weight(1f))
                        CircularProgressIndicator()
                        Spacer(Modifier.weight(1f))
                    }

                    is AnimalGameUiState.Playing -> {
                        Spacer(Modifier.height(8.dp))

                        // AI captured pieces row (Blue's captures shown at top = AI side)
                        CapturedRow(
                            label  = "AI捕获",
                            pieces = state.capturedByAi,
                            color  = Color(0xFF1565C0)
                        )

                        Spacer(Modifier.height(6.dp))

                        // Turn indicator
                        val turnText = if (state.isAiThinking) "AI思考中…"
                                       else if (state.currentTurn == state.playerColor) "你的回合"
                                       else "等待AI…"
                        val turnColor = if (state.isAiThinking || state.currentTurn != state.playerColor)
                            MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        Text(
                            text       = turnText,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = turnColor
                        )

                        Spacer(Modifier.height(6.dp))

                        AnimalBoardComponent(
                            state          = state.boardState,
                            onSquareTapped = { sq -> viewModel.onSquareTapped(sq) }
                        )

                        Spacer(Modifier.height(6.dp))

                        // Player captured pieces row (Red's captures at bottom = player side)
                        CapturedRow(
                            label  = "你捕获",
                            pieces = state.capturedByPlayer,
                            color  = Color(0xFFC62828)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            LegendDot(Color(0xFFC62828), "你 (红方)")
                            Spacer(Modifier.width(16.dp))
                            LegendDot(Color(0xFF1565C0), "AI (蓝方)")
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    is AnimalGameUiState.GameOver -> {
                        Spacer(Modifier.height(8.dp))

                        AnimalBoardComponent(
                            state          = state.boardState,
                            onSquareTapped = {}
                        )

                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = MaterialTheme.shapes.large,
                            colors   = CardDefaults.cardColors(
                                containerColor = if (state.playerWon)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier            = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text       = if (state.playerWon) "🎉 你赢了！" else "😢 AI获胜",
                                    fontSize   = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (state.playerWon)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text  = if (state.playerWon) "恭喜击败${state.difficulty.label}难度的AI！"
                                            else "再接再厉，挑战AI！",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (state.playerWon)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick  = { viewModel.onNewGame() },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("再来一局") }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick  = onNavigateBack,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("返回主菜单") }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CapturedRow(label: String, pieces: List<String>, color: Color) {
    if (pieces.isEmpty()) return
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text       = "$label：",
            style      = MaterialTheme.typography.labelSmall,
            color      = color,
            fontWeight = FontWeight.Bold
        )
        pieces.forEach { emoji ->
            Text(text = emoji, fontSize = 18.sp)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
