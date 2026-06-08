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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val progress by viewModel.userProgress.collectAsState(initial = null)
    val themes by viewModel.themeStats.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistics") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatRow("Rating", "${progress?.rating ?: 1200}")
                        StatRow("Current Streak", "${progress?.currentStreak ?: 0} days")
                        StatRow("Longest Streak", "${progress?.longestStreak ?: 0} days")
                        StatRow("Puzzles Solved", "${progress?.totalSolved ?: 0}")
                        val accuracy = progress?.let {
                            if (it.totalAttempted > 0) (it.totalSolved * 100 / it.totalAttempted) else 0
                        } ?: 0
                        StatRow("Accuracy", "$accuracy%")
                    }
                }
            }

            if (themes.isNotEmpty()) {
                item {
                    Text(
                        "Tactics Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(themes) { stat ->
                    ThemeStatCard(stat.theme, stat.solved, stat.attempted)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThemeStatCard(theme: String, solved: Int, attempted: Int) {
    val accuracy = if (attempted > 0) solved.toFloat() / attempted else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(theme.toDisplayName(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("$solved/$attempted", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { accuracy },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )
        }
    }
}

private fun String.toDisplayName(): String = when (this) {
    "mateIn1" -> "Checkmate in 1"
    "mateIn2" -> "Checkmate in 2"
    "mateIn3" -> "Checkmate in 3"
    "fork" -> "Fork"
    "pin" -> "Pin"
    "skewer" -> "Skewer"
    "discoveredAttack" -> "Discovered Attack"
    "hangingPiece" -> "Hanging Piece"
    "sacrifice" -> "Sacrifice"
    "deflection" -> "Deflection"
    else -> replaceFirstChar { it.uppercase() }
}
