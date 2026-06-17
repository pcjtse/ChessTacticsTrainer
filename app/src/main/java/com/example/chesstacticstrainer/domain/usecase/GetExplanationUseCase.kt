package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.TacticExplanation

class GetExplanationUseCase {

    operator fun invoke(themes: List<String>, isCorrect: Boolean): TacticExplanation {
        if (!isCorrect) {
            return TacticExplanation(
                tacticName = "Incorrect Move",
                description = "That wasn't the best move. Look for a move that creates an immediate threat.",
                highlightedSquares = emptyList(),
                theme = null
            )
        }
        val primaryTheme = THEME_PRIORITY.firstOrNull { it in themes } ?: themes.firstOrNull()
        return explanationFor(primaryTheme)
    }

    private fun explanationFor(theme: String?): TacticExplanation = when (theme) {
        "mateIn1" -> TacticExplanation("Checkmate in 1", "The king has no escape — this move delivers immediate checkmate.", theme = "mateIn1")
        "mateIn2" -> TacticExplanation("Checkmate in 2", "A forced two-move sequence that traps the king with no escape.", theme = "mateIn2")
        "mateIn3" -> TacticExplanation("Checkmate in 3", "A three-move combination leading to an inescapable checkmate.", theme = "mateIn3")
        "mateIn4" -> TacticExplanation("Checkmate in 4", "A four-move sequence that forces checkmate.", theme = "mateIn4")
        "mateIn5" -> TacticExplanation("Checkmate in 5", "A five-move forced sequence ending in checkmate.", theme = "mateIn5")
        "fork" -> TacticExplanation("Fork", "One piece attacks two enemy pieces simultaneously, guaranteeing material gain.", theme = "fork")
        "pin" -> TacticExplanation("Pin", "A piece is pinned — moving it would expose a more valuable piece behind it.", theme = "pin")
        "skewer" -> TacticExplanation("Skewer", "Like a reverse pin: the more valuable piece is attacked first, and a lesser piece hides behind it.", theme = "skewer")
        "discoveredAttack" -> TacticExplanation("Discovered Attack", "Moving one piece uncovers an attack from another piece behind it.", theme = "discoveredAttack")
        "discoveredCheck" -> TacticExplanation("Discovered Check", "Moving a piece reveals a check from another piece — the king must respond.", theme = "discoveredCheck")
        "doubleCheck" -> TacticExplanation("Double Check", "Two pieces give check simultaneously — the king must move.", theme = "doubleCheck")
        "hangingPiece" -> TacticExplanation("Hanging Piece", "A piece is left undefended — capture it to win material.", theme = "hangingPiece")
        "sacrifice" -> TacticExplanation("Sacrifice", "Giving up material to gain a decisive positional or tactical advantage.", theme = "sacrifice")
        "deflection" -> TacticExplanation("Deflection", "Forcing an overloaded defending piece away from its duty.", theme = "deflection")
        "interference" -> TacticExplanation("Interference", "A piece is placed between two enemy pieces to cut off their coordination.", theme = "interference")
        "trappedPiece" -> TacticExplanation("Trapped Piece", "An enemy piece has no safe square to move to — win it with tempo.", theme = "trappedPiece")
        "zugzwang" -> TacticExplanation("Zugzwang", "Any move the opponent makes worsens their position.", theme = "zugzwang")
        else -> TacticExplanation("Best Move", "This is the strongest available move in the position.", theme = theme)
    }

    companion object {
        private val THEME_PRIORITY = listOf(
            "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5",
            "fork", "pin", "skewer", "discoveredAttack", "discoveredCheck",
            "doubleCheck", "hangingPiece", "sacrifice", "deflection",
            "interference", "trappedPiece", "zugzwang"
        )
    }
}
