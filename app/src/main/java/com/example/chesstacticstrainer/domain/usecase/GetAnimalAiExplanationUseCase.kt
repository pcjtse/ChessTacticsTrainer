package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.dto.OpenAiMessage
import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest

class GetAnimalAiExplanationUseCase(
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
    ): kotlin.Result<String> {
        if (!isAvailable) return kotlin.Result.failure(IllegalStateException("OpenAI API key not configured"))

        return runCatching {
            val themeLabel = themes.map { translateTheme(it) }.joinToString("、").ifBlank { "战术组合" }

            val moveSeq = solutionMoves.mapIndexed { i, move ->
                val from = move.take(2)
                val to = move.drop(2).take(2)
                val side = if (i % 2 == 0) "你方" else "电脑"
                "$side：$from→$to"
            }.joinToString("，")

            val outcomeText = if (playerWon) "玩家成功解题。" else "玩家走了错误的着法。"

            val userPrompt = buildString {
                append("请用2-3句简体中文解释以下斗兽棋战术题的解法，适合约${rating}水平的玩家。\n")
                append("战术主题：$themeLabel\n")
                append("起始局面（FEN）：$fen\n")
                if (moveSeq.isNotBlank()) append("解法着法：$moveSeq\n")
                append(outcomeText)
                append("\n全文必须使用简体中文，解说要简洁清晰。")
            }

            Log.d("CTT-AC", "AI prompt: themes=$themeLabel moves=$solutionMoves won=$playerWon")

            val request = OpenAiRequest(
                messages = listOf(
                    OpenAiMessage(
                        "system",
                        "你是一位专业斗兽棋（Dou Shou Qi / 斗兽棋）教练。斗兽棋是中国传统棋盘游戏，棋盘7列9行，共8种动物棋子（象、狮、虎、豹、狼、狗、猫、鼠），按等级强弱捕食，首先进入对方兽穴者胜。鼠可捕象，狮虎可跳水，动物在陷阱中战力为零。请始终用简体中文回答，回答简洁，不超过3句。"
                    ),
                    OpenAiMessage("user", userPrompt)
                ),
                max_tokens = 250
            )

            val response = apiService.complete("Bearer $apiKey", request)
            val text = response.choices.firstOrNull()?.message?.content?.trim()
                ?: error("OpenAI returned empty response")
            Log.d("CTT-AC", "AI response: $text")
            text
        }
    }

    private fun translateTheme(theme: String): String = when (theme) {
        "denEntry"      -> "进入兽穴"
        "capture"       -> "捕获"
        "waterJump"     -> "跳水"
        "mouseElephant" -> "鼠胜象"
        "trap"          -> "陷阱"
        "fork"          -> "双攻"
        else            -> theme
    }
}
