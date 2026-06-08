package com.example.chesstacticstrainer.domain.usecase

import com.example.chesstacticstrainer.domain.model.TacticExplanation

class GetExplanationUseCase {

    operator fun invoke(themes: List<String>, isCorrect: Boolean): TacticExplanation {
        if (!isCorrect) {
            return TacticExplanation(
                tacticName = "Incorrect Move",
                description = "That wasn't the best move. Look for a move that creates an immediate threat.",
                highlightedSquares = emptyList()
            )
        }
        val primaryTheme = THEME_PRIORITY.firstOrNull { it in themes } ?: themes.firstOrNull()
        return explanationFor(primaryTheme)
    }

    private fun explanationFor(theme: String?): TacticExplanation = when (theme) {
        "mateIn1" -> TacticExplanation("Checkmate in 1", "The king has no escape — this move delivers immediate checkmate.")
        "mateIn2" -> TacticExplanation("Checkmate in 2", "A forced two-move sequence that traps the king with no escape.")
        "mateIn3" -> TacticExplanation("Checkmate in 3", "A three-move combination leading to an inescapable checkmate.")
        "mateIn4" -> TacticExplanation("Checkmate in 4", "A four-move sequence that forces checkmate.")
        "mateIn5" -> TacticExplanation("Checkmate in 5", "A five-move forced sequence ending in checkmate.")
        "fork" -> TacticExplanation("Fork", "One piece attacks two enemy pieces simultaneously, guaranteeing material gain.")
        "pin" -> TacticExplanation("Pin", "A piece is pinned — moving it would expose a more valuable piece behind it.")
        "skewer" -> TacticExplanation("Skewer", "Like a reverse pin: the more valuable piece is attacked first, and a lesser piece hides behind it.")
        "discoveredAttack" -> TacticExplanation("Discovered Attack", "Moving one piece uncovers an attack from another piece behind it.")
        "discoveredCheck" -> TacticExplanation("Discovered Check", "Moving a piece reveals a check from another piece — the king must respond.")
        "doubleCheck" -> TacticExplanation("Double Check", "Two pieces give check simultaneously — the king must move.")
        "hangingPiece" -> TacticExplanation("Hanging Piece", "A piece is left undefended — capture it to win material.")
        "sacrifice" -> TacticExplanation("Sacrifice", "Giving up material to gain a decisive positional or tactical advantage.")
        "deflection" -> TacticExplanation("Deflection", "Forcing an overloaded defending piece away from its duty.")
        "interference" -> TacticExplanation("Interference", "A piece is placed between two enemy pieces to cut off their coordination.")
        "trappedPiece" -> TacticExplanation("Trapped Piece", "An enemy piece has no safe square to move to — win it with tempo.")
        "zugzwang" -> TacticExplanation("Zugzwang", "Any move the opponent makes worsens their position.")
        else -> TacticExplanation("Best Move", "This is the strongest available move in the position.")
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
