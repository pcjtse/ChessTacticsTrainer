package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.dto.OpenAiMessage
import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest

class GetAiExplanationUseCase(
    private val apiService: OpenAiApiService,
    private val apiKey: String
) {
    val isAvailable: Boolean get() = apiKey.isNotBlank()

    suspend operator fun invoke(
        fen: String,
        themes: List<String>,
        solutionMoves: List<String>,
        rating: Int,
        playerWon: Boolean
    ): Result<String> {
        if (!isAvailable) return Result.failure(IllegalStateException("OpenAI API key not configured"))

        return runCatching {
            val themeLabel = themes.joinToString(", ").ifBlank { "tactics" }
            // solutionMoves[0,2,4,...] = player moves; solutionMoves[1,3,5,...] = computer replies
            val moveSequence = solutionMoves.mapIndexed { i, uci ->
                if (i % 2 == 0) "You: $uci" else "Engine: $uci"
            }.joinToString(" | ")
            val outcomeText = if (playerWon) "The player solved it correctly." else "The player made an incorrect move."

            val userPrompt = buildString {
                append("Explain this chess puzzle solution in 2-3 clear sentences for a player rated $rating.\n")
                append("Starting position (FEN): $fen\n")
                append("Tactic theme(s): $themeLabel\n")
                if (moveSequence.isNotBlank()) append("Solution moves (UCI notation): $moveSequence\n")
                append(outcomeText)
                append("\nFocus on the tactical idea — why the winning move works. Be specific to this position and mention the pieces and squares involved.")
            }

            Log.d("CTT", "AI prompt: fen=$fen themes=$themeLabel moves=${solutionMoves} playerWon=$playerWon")

            val request = OpenAiRequest(
                messages = listOf(
                    OpenAiMessage("system", "You are a helpful chess coach. Explain chess tactics concisely for club-level players. When given a FEN position and UCI moves, always reference the specific pieces and squares involved."),
                    OpenAiMessage("user", userPrompt)
                )
            )

            val response = apiService.complete("Bearer $apiKey", request)
            val text = response.choices.firstOrNull()?.message?.content?.trim()
                ?: error("Empty response from OpenAI")
            Log.d("CTT", "AI response: $text")
            text
        }
    }
}
