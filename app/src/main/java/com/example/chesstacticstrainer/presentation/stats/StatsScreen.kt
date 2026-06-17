package com.example.chesstacticstrainer.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chesstacticstrainer.domain.model.UserProgress
import com.example.chesstacticstrainer.presentation.LocalStrings

private enum class StatsMode { CHESS, XIANGQI }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val strings         = LocalStrings.current
    val chessProgress   by viewModel.chessProgress.collectAsState(initial = null)
    val xiangqiProgress by viewModel.xiangqiProgress.collectAsState(initial = null)
    val themes          by viewModel.themeStats.collectAsState(initial = emptyList())
    var mode            by rememberSaveable { mutableStateOf(StatsMode.CHESS) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(strings.statsTitle) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == StatsMode.CHESS,
                        onClick  = { mode = StatsMode.CHESS },
                        label    = { Text(strings.statsChessTab) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = mode == StatsMode.XIANGQI,
                        onClick  = { mode = StatsMode.XIANGQI },
                        label    = { Text(strings.statsXiangqiTab) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            val progress = if (mode == StatsMode.CHESS) chessProgress else xiangqiProgress

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.large,
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (mode == StatsMode.CHESS) strings.statsChessOverview else strings.statsXiangqiOverview,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                        )
                        StatRow(strings.statsRating, "${progress?.rating ?: 1200}")
                        StatRow(strings.statsCurrentStreak, "${progress?.currentStreak ?: 0}${strings.statsDaysUnit}")
                        StatRow(strings.statsBestStreak, "${progress?.longestStreak ?: 0}${strings.statsDaysUnit}")
                        StatRow(strings.statsPuzzlesSolved, "${progress?.totalSolved ?: 0}")
                        StatRow(strings.statsAccuracy, "${accuracyOf(progress)}%")
                    }
                }
            }

            if (mode == StatsMode.CHESS && themes.isNotEmpty()) {
                item {
                    Text(
                        strings.statsTacticBreakdown,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(themes) { stat ->
                    ThemeStatCard(strings.chessThemeDisplayName(stat.theme), stat.solved, stat.attempted)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThemeStatCard(theme: String, solved: Int, attempted: Int) {
    val accuracy = if (attempted > 0) solved.toFloat() / attempted else 0f
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(theme, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("$solved/$attempted", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { accuracy },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )
        }
    }
}

private fun accuracyOf(p: UserProgress?): Int =
    p?.let { if (it.totalAttempted > 0) it.totalSolved * 100 / it.totalAttempted else 0 } ?: 0
