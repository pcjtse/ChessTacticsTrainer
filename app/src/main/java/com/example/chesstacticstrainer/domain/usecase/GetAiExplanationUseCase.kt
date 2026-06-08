package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.dto.OpenAiMessage
import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest

class GetAiExplanationUseCase(
    private val apiService: OpenAiApiService,
    private val apiKey: String
) {
    val isAvailable: Boolean get() = apiKey.isNotBlank()

    suspend operator fun invoke(
        themes: List<String>,
        solutionMoves: List<String>,
        rating: Int,
        playerWon: Boolean
    ): Result<String> {
        if (!isAvailable) return Result.failure(IllegalStateException("OpenAI API key not configured"))

        return runCatching {
            val themeLabel = themes.firstOrNull() ?: "tactics"
            val movesLabel = solutionMoves.drop(1).joinToString(" → ") // skip opponent's first move
            val outcomeText = if (playerWon) "The player solved it correctly." else "The player made an incorrect move."

            val userPrompt = buildString {
                append("Explain this chess puzzle solution in 2-3 clear sentences for a player rated $rating.\n")
                append("Tactic theme: $themeLabel\n")
                if (movesLabel.isNotBlank()) append("Key moves: $movesLabel\n")
                append(outcomeText)
                append("\nFocus on the tactical idea — why the winning move works. Be concise.")
            }

            val request = OpenAiRequest(
                messages = listOf(
                    OpenAiMessage("system", "You are a helpful chess coach. Explain chess tactics concisely and clearly for club-level players."),
                    OpenAiMessage("user", userPrompt)
                )
            )

            val response = apiService.complete("Bearer $apiKey", request)
            response.choices.firstOrNull()?.message?.content?.trim()
                ?: error("Empty response from OpenAI")
        }
    }
}
