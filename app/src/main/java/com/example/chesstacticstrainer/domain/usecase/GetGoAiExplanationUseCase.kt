package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.dto.OpenAiMessage
import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoSgfNode
import com.example.chesstacticstrainer.domain.model.GoStone

class GetGoAiExplanationUseCase(
    private val apiService: OpenAiApiService,
    private val apiKey: String
) {
    val isAvailable: Boolean get() = apiKey.isNotBlank()

    suspend operator fun invoke(
        puzzleName: String,
        boardSize: Int,
        playerColor: GoStone,
        solutionRoot: GoSgfNode,
        playerWon: Boolean
    ): Result<String> {
        if (!isAvailable) return Result.failure(IllegalStateException("OpenAI API key not configured"))

        return runCatching {
            val colorStr    = if (playerColor == GoStone.BLACK) "黑方" else "白方"
            val solutionStr = formatMainLine(solutionRoot, boardSize)
            val outcomeStr  = if (playerWon) "玩家成功解题。" else "玩家走了错误的着法。"

            val prompt = buildString {
                append("这是一道${boardSize}路围棋死活题，题目名称：「$puzzleName」，执子方为$colorStr。\n")
                if (solutionStr.isNotBlank()) append("正确解法：$solutionStr\n")
                append(outcomeStr)
                append("\n请用2-3句简体中文解释这道题的关键着法和原理（如气、打吃、提子、眼位等概念）。")
            }

            Log.d("CTT-GO", "AI prompt: $prompt")

            val request = OpenAiRequest(
                messages = listOf(
                    OpenAiMessage(
                        "system",
                        "你是一位围棋老师，擅长用简洁的语言向初学者解释死活题的原理。请始终使用简体中文，回答不超过3句。"
                    ),
                    OpenAiMessage("user", prompt)
                ),
                max_tokens = 200
            )

            val response = apiService.complete("Bearer $apiKey", request)
            val text = response.choices.firstOrNull()?.message?.content?.trim()
                ?: error("OpenAI returned empty response")
            Log.d("CTT-GO", "AI response: $text")
            text
        }
    }

    private fun formatMainLine(root: GoSgfNode, boardSize: Int): String {
        val moves = mutableListOf<String>()
        var node: GoSgfNode? = root.children.firstOrNull { it.isMainLine }
        while (node != null) {
            val m = node.move
            if (m != null) {
                val colorLabel = if (node.color == GoStone.BLACK) "黑" else "白"
                moves.add("$colorLabel${pointToLabel(m, boardSize)}")
            }
            node = node.children.firstOrNull { it.isMainLine }
        }
        return moves.joinToString(" → ")
    }

    // Converts GoPoint to standard Go notation (A1–T19, skipping I column)
    private fun pointToLabel(p: GoPoint, boardSize: Int): String {
        val colChar = if (p.col < 8) ('A' + p.col) else ('A' + p.col + 1)   // skip 'I'
        val rowNum  = boardSize - p.row                                         // row 0 = top = highest number
        return "$colChar$rowNum"
    }
}
