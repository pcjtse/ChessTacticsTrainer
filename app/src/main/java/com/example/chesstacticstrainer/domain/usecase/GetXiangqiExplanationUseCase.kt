package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.TacticExplanation

class GetXiangqiExplanationUseCase {

    operator fun invoke(themes: List<String>, isCorrect: Boolean): TacticExplanation {
        if (!isCorrect) {
            return TacticExplanation(
                tacticName = "走法有误",
                description = "这不是最佳着法。请寻找能立即制造威胁的着法。",
                highlightedSquares = emptyList()
            )
        }
        val primary = THEME_PRIORITY.firstOrNull { it in themes } ?: themes.firstOrNull()
        return explanationFor(primary)
    }

    private fun explanationFor(theme: String?): TacticExplanation = when (theme) {
        "mateIn1"  -> TacticExplanation("一步将杀", "此着法直接将死对方，将军无路可逃。")
        "mateIn2"  -> TacticExplanation("两步将杀", "两步强制将杀，对方将军无法解脱。")
        "mateIn3"  -> TacticExplanation("三步将杀", "三步连续将杀，形成无法破解的必杀棋。")
        "mateIn4"  -> TacticExplanation("四步将杀", "四步强制将杀序列。")
        "mateIn5"  -> TacticExplanation("五步将杀", "五步强制将杀序列。")
        "tactics"  -> TacticExplanation("战术妙手", "此着法是当前局面的最强着法。")
        else       -> TacticExplanation("最佳着法", "此着法是当前局面的最强选择。")
    }

    companion object {
        private val THEME_PRIORITY = listOf(
            "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5", "tactics"
        )
    }
}
