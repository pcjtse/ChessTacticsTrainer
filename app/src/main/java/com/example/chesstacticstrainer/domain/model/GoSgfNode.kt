package com.example.chesstacticstrainer.domain.model

/**
 * A single node in the SGF solution tree.
 * [move] is null for the root (setup) node.
 * [isMainLine] true on the first child at every branch — the correct answer path.
 */
data class GoSgfNode(
    val move: GoPoint?,
    val color: GoStone,
    val children: List<GoSgfNode>,
    val comment: String = "",
    val isMainLine: Boolean = true
)
