package com.example.chesstacticstrainer.domain.usecase

import android.util.Log
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.dto.OpenAiMessage
import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest

class GetXiangqiAiExplanationUseCase(
    private val apiService: OpenAiApiService,
    private val apiKey: String
) {
    val isAvailable: Boolean get() = apiKey.isNotBlank()

    suspend operator fun invoke(
        fen: String,
        themes: List<String>,
        solutionMoves: List<String>,
        rating: Int,
        playerWon: Boolean,
        isEnglish: Boolean = false
    ): kotlin.Result<String> {
        if (!isAvailable) return kotlin.Result.failure(IllegalStateException("OpenAI API key not configured"))

        return runCatching {
            if (isEnglish) {
                val themeLabel = themes.joinToString(", ").ifBlank { "tactics" }
                val moveSequence = solutionMoves.mapIndexed { i, uci ->
                    if (i % 2 == 0) "You: $uci" else "Engine: $uci"
                }.joinToString(" | ")
                val outcomeText = if (playerWon) "The player solved it correctly." else "The player made an incorrect move."

                val userPrompt = buildString {
                    append("Explain this Xiangqi (Chinese chess) puzzle solution in 2-3 clear sentences for a player rated $rating.\n")
                    append("Starting position (FEN): $fen\n")
                    append("Tactic theme(s): $themeLabel\n")
                    if (moveSequence.isNotBlank()) append("Solution moves (UCCI notation): $moveSequence\n")
                    append(outcomeText)
                    append("\nFocus on the tactical idea — why the winning move works. Describe pieces by their Xiangqi names (Chariot, Horse, Cannon, Advisor, etc.).")
                }

                Log.d("CTT-XQ", "AI prompt (EN): themes=$themeLabel moves=$solutionMoves won=$playerWon")

                val request = OpenAiRequest(
                    messages = listOf(
                        OpenAiMessage("system", "You are a helpful Xiangqi (Chinese chess) coach. Explain tactics concisely in English for players of all levels. When given a FEN position and UCCI moves, describe the tactical idea clearly using Xiangqi piece names."),
                        OpenAiMessage("user", userPrompt)
                    ),
                    max_tokens = 250
                )
                val response = apiService.complete("Bearer $apiKey", request)
                val text = response.choices.firstOrNull()?.message?.content?.trim()
                    ?: error("OpenAI returned empty response")
                Log.d("CTT-XQ", "AI response (EN): $text")
                text
            } else {
                val themeLabel = themes.map { translateTheme(it) }.joinToString("、").ifBlank { "战术组合" }

                // Convert UCCI moves to standard Chinese chess notation, tracking board state across moves
                val chineseMoves = convertMovesToChinese(fen, solutionMoves)
                val moveSeq = chineseMoves.mapIndexed { i, move ->
                    if (i % 2 == 0) "你方：$move" else "电脑：$move"
                }.joinToString("，")

                val outcomeText = if (playerWon) "玩家成功解题。" else "玩家走了错误的着法。"

                val userPrompt = buildString {
                    append("请用2-3句简体中文解释以下象棋战术题的解法，适合约${rating}水平的玩家。\n")
                    append("战术主题：$themeLabel\n")
                    append("起始局面（FEN）：$fen\n")
                    if (moveSeq.isNotBlank()) append("解法（中文记谱）：$moveSeq\n")
                    append(outcomeText)
                    append("\n解说时请用标准中文象棋记谱格式（如：炮二平五、马八进七、车三进二）指代着法，全文必须使用简体中文。")
                }

                Log.d("CTT-XQ", "AI prompt (ZH): themes=$themeLabel moves=$chineseMoves won=$playerWon")

                val request = OpenAiRequest(
                    messages = listOf(
                        OpenAiMessage(
                            "system",
                            "你是一位专业象棋（中国象棋）教练。请始终用简体中文回答，使用标准中文记谱格式（红方用汉字路数如炮五平六，黑方用阿拉伯数字如炮5平6）和传统棋子名称（帅/仕/相/马/车/炮/兵，将/士/象/马/车/炮/卒）。回答简洁，不超过3句。"
                        ),
                        OpenAiMessage("user", userPrompt)
                    ),
                    max_tokens = 250
                )
                val response = apiService.complete("Bearer $apiKey", request)
                val text = response.choices.firstOrNull()?.message?.content?.trim()
                    ?: error("OpenAI returned empty response")
                Log.d("CTT-XQ", "AI response (ZH): $text")
                text
            }
        }
    }

    // ── Move conversion ───────────────────────────────────────────────────────

    /**
     * Converts a sequence of UCCI moves to Chinese chess notation,
     * tracking piece positions across moves so later moves identify pieces correctly.
     */
    private fun convertMovesToChinese(fen: String, moves: List<String>): List<String> {
        val board = buildRawFenMap(fen).toMutableMap()
        return moves.map { uci ->
            val from = uci.take(2)
            val to   = uci.drop(2).take(2)
            val pieceChar = board[from]
            val result = if (pieceChar != null) ucciToChineseNotation(pieceChar, from, to) else uci
            // Apply move so subsequent moves see the updated board
            val moved = board.remove(from)
            if (moved != null) board[to] = moved
            result
        }
    }

    /**
     * Converts one UCCI move to Chinese notation.
     *
     * Format: [棋子名][起始路][进/退/平][终点路 or 步数]
     *
     * File numbering (right-to-left from each player's perspective):
     *   Red  file = 9 - col  (col 8 = file i = Red's 1st file)
     *   Black file = col + 1  (col 0 = file a = Black's 1st file)
     * Red uses Chinese numerals 一~九; Black uses Arabic 1–9.
     *
     * 4th character:
     *   平 (horizontal)        → destination file
     *   Diagonal pieces        → destination file  (Horse, Elephant, Advisor)
     *   Straight vertical move → number of ranks moved  (Chariot, Cannon, General, Soldier)
     */
    private fun ucciToChineseNotation(pieceChar: Char, from: String, to: String): String {
        val fromCol  = from[0] - 'a'
        val fromRank = from[1].digitToInt()
        val toCol    = to[0] - 'a'
        val toRank   = to[1].digitToInt()
        val isRed    = pieceChar.isUpperCase()

        val pieceName = chinesePieceName(pieceChar) ?: return "$from$to"

        // File number from each side's perspective
        val fromFileNum = if (isRed) 9 - fromCol else fromCol + 1
        val toFileNum   = if (isRed) 9 - toCol   else toCol   + 1

        // Direction
        val direction = when {
            fromRank == toRank                      -> "平"
            isRed  && toRank < fromRank             -> "进"   // Red advances toward rank 0
            isRed                                   -> "退"
            toRank > fromRank                       -> "进"   // Black advances toward rank 9
            else                                    -> "退"
        }

        // 4th character: destination file or step count
        val isDiagonalPiece = pieceChar.lowercaseChar() in setOf('h', 'n', 'e', 'b', 'a')
        val fourthChar = when {
            direction == "平" || isDiagonalPiece -> {
                if (isRed) chineseNumeral(toFileNum) else toFileNum.toString()
            }
            else -> {
                val steps = if (toRank > fromRank) toRank - fromRank else fromRank - toRank
                if (isRed) chineseNumeral(steps) else steps.toString()
            }
        }

        val fromFileStr = if (isRed) chineseNumeral(fromFileNum) else fromFileNum.toString()
        return "$pieceName$fromFileStr$direction$fourthChar"
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildRawFenMap(fen: String): Map<String, Char> {
        val map  = mutableMapOf<String, Char>()
        val rows = fen.split(" ").first().split("/")
        rows.forEachIndexed { rankIdx, row ->
            var fileIdx = 0
            for (ch in row) {
                if (ch.isDigit()) fileIdx += ch.digitToInt()
                else { map["${'a' + fileIdx}$rankIdx"] = ch; fileIdx++ }
            }
        }
        return map
    }

    private fun chinesePieceName(c: Char): String? {
        val isRed = c.isUpperCase()
        return when (c.lowercaseChar()) {
            'r'      -> "车"
            'h', 'n' -> "马"
            'e', 'b' -> if (isRed) "相" else "象"
            'a'      -> if (isRed) "仕" else "士"
            'k'      -> if (isRed) "帅" else "将"
            'c'      -> "炮"
            'p'      -> if (isRed) "兵" else "卒"
            else     -> null
        }
    }

    private fun chineseNumeral(n: Int): String = when (n) {
        1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"
        6 -> "六"; 7 -> "七"; 8 -> "八"; 9 -> "九"
        else -> n.toString()
    }

    private fun translateTheme(theme: String): String = when (theme) {
        "mateIn1"                       -> "一步将杀"
        "mateIn2"                       -> "两步将杀"
        "mateIn3"                       -> "三步将杀"
        "mateIn4"                       -> "四步将杀"
        "mateIn5"                       -> "五步将杀"
        "tactics"                       -> "战术组合"
        "pin"                           -> "牵制"
        "fork"                          -> "双将"
        "skewer"                        -> "串将"
        "discovery", "discoveredAttack" -> "闪将"
        "sacrifice"                     -> "弃子"
        "check"                         -> "将军"
        else                            -> theme
    }
}
