package com.example.chesstacticstrainer.presentation.home

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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.chesstacticstrainer.domain.model.UserProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartChess: () -> Unit,
    onStartXiangqi: () -> Unit,
    onStartAnimal: (AnimalDifficulty) -> Unit,
    onStartGo: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val chess              by viewModel.chessProgress.collectAsState(initial = null)
    val xiangqi            by viewModel.xiangqiProgress.collectAsState(initial = null)
    val animal             by viewModel.animalProgress.collectAsState(initial = null)
    val go                 by viewModel.goProgress.collectAsState(initial = null)
    val selectedDifficulty by viewModel.animalDifficulty.collectAsState(initial = AnimalDifficulty.MEDIUM)

    Scaffold(
        topBar = { TopAppBar(title = { Text("战术训练") }) }
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(24.dp))

                Text("♟", fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "选择训练模式",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "通过战术题提升你的棋艺",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                // Chess card
                ModeCard(
                    emoji       = "♟",
                    title       = "国际象棋",
                    subtitle    = "International Chess",
                    progress    = chess,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick     = onStartChess
                )

                Spacer(Modifier.height(16.dp))

                // Xiangqi card
                ModeCard(
                    emoji       = "象",
                    title       = "中国象棋",
                    subtitle    = "Chinese Chess",
                    progress    = xiangqi,
                    accentColor = Color(0xFFC62828),
                    onClick     = onStartXiangqi
                )

                Spacer(Modifier.height(16.dp))

                // Animal Chess card with difficulty selector
                AnimalModeCard(
                    progress           = animal,
                    selectedDifficulty = selectedDifficulty,
                    onDifficultyChange = { viewModel.setDifficulty(it) },
                    onClick            = { onStartAnimal(selectedDifficulty) }
                )

                Spacer(Modifier.height(16.dp))

                // Go / Weiqi card
                ModeCard(
                    emoji       = "⚫",
                    title       = "围棋",
                    subtitle    = "Go · Tsumego Puzzles",
                    progress    = go,
                    accentColor = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                    onClick     = onStartGo
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalModeCard(
    progress: UserProgress?,
    selectedDifficulty: AnimalDifficulty,
    onDifficultyChange: (AnimalDifficulty) -> Unit,
    onClick: () -> Unit
) {
    val accentColor = Color(0xFF2E7D32)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.spacedBy(12.dp)
            ) {
                Text("🦁", fontSize = 36.sp)
                Column {
                    Text(
                        "斗兽棋",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Animal Chess",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats chips
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatChip(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(Icons.Filled.LocalFireDepartment, null,
                            tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    label = "连胜",
                    value = "${progress?.currentStreak ?: 0}天"
                )
                MiniStatChip(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(Icons.Filled.Star, null,
                            tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    label = "评分",
                    value = "${progress?.rating ?: 1200}"
                )
            }

            if ((progress?.totalAttempted ?: 0) > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "已玩：${progress?.totalSolved ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val acc = progress?.let {
                        if (it.totalAttempted > 0) it.totalSolved * 100 / it.totalAttempted else 0
                    } ?: 0
                    Text(
                        "胜率：$acc%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Difficulty selector
            Text(
                "难度",
                style      = MaterialTheme.typography.labelMedium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimalDifficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = diff == selectedDifficulty,
                        onClick  = { onDifficultyChange(diff) },
                        label    = { Text(diff.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick  = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("开始游戏", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun ModeCard(
    emoji: String,
    title: String,
    subtitle: String,
    progress: UserProgress?,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.spacedBy(12.dp)
            ) {
                Text(emoji, fontSize = 36.sp)
                Column {
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatChip(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(Icons.Filled.LocalFireDepartment, null,
                            tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    label = "连胜",
                    value = "${progress?.currentStreak ?: 0}天"
                )
                MiniStatChip(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(Icons.Filled.Star, null,
                            tint = accentColor, modifier = Modifier.size(20.dp))
                    },
                    label = "评分",
                    value = "${progress?.rating ?: 1200}"
                )
            }

            if ((progress?.totalAttempted ?: 0) > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "已解：${progress?.totalSolved ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val acc = progress?.let {
                        if (it.totalAttempted > 0) it.totalSolved * 100 / it.totalAttempted else 0
                    } ?: 0
                    Text(
                        "正确率：$acc%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick  = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("开始练习", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun MiniStatChip(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    value,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
