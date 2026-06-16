package com.example.chesstacticstrainer.data.local

import com.example.chesstacticstrainer.domain.model.GoBoardState
import com.example.chesstacticstrainer.domain.model.GoPuzzle
import com.example.chesstacticstrainer.domain.model.GoPoint
import com.example.chesstacticstrainer.domain.model.GoSgfNode
import com.example.chesstacticstrainer.domain.model.GoStone

// ── Intermediate SGF representation ──────────────────────────────────────────

private data class SgfNode(val props: Map<String, List<String>>)
private data class SgfTree(val nodes: List<SgfNode>, val children: List<SgfTree>)

// ── Parser ────────────────────────────────────────────────────────────────────

object GoSgfParser {

    fun parse(sgf: String, id: String, difficulty: Int = 1, category: String = "基本"): GoPuzzle? {
        val input = sgf.trim()
        val (tree, _) = parseTree(input, 0) ?: return null

        val rootNode = tree.nodes.firstOrNull() ?: return null
        val playerColor = if (rootNode.props["PL"]?.firstOrNull()?.uppercase() == "W") GoStone.WHITE else GoStone.BLACK
        val name = rootNode.props["GN"]?.firstOrNull() ?: id
        val diff = rootNode.props["DI"]?.firstOrNull()?.toIntOrNull() ?: difficulty

        // Infer board size from SZ property; when absent, derive from the max coordinate in AB/AW
        val boardSize = rootNode.props["SZ"]?.firstOrNull()?.toIntOrNull() ?: run {
            val allCoords = (rootNode.props["AB"] ?: emptyList()) + (rootNode.props["AW"] ?: emptyList())
            val maxCoord = allCoords.maxOfOrNull { s ->
                if (s.length >= 2) maxOf(s[0] - 'a', s[1] - 'a') else 0
            } ?: 0
            when {
                maxCoord <= 8  -> 9
                maxCoord <= 12 -> 13
                else           -> 19
            }
        }

        val stones = mutableMapOf<GoPoint, GoStone>()
        rootNode.props["AB"]?.forEach { c -> sgfCoord(c, boardSize)?.let { stones[it] = GoStone.BLACK } }
        rootNode.props["AW"]?.forEach { c -> sgfCoord(c, boardSize)?.let { stones[it] = GoStone.WHITE } }

        val initialState = GoBoardState(boardSize, stones.toMap())

        // Remaining root-sequence nodes + child trees form the solution tree
        val sequenceTail = tree.nodes.drop(1)
        val solutionRoot = buildSolutionTree(
            sequenceTail = sequenceTail,
            childTrees   = tree.children,
            playerColor  = playerColor,
            isMainLine   = true,
            boardSize    = boardSize
        ) ?: GoSgfNode(move = null, color = playerColor, children = emptyList())

        return GoPuzzle(
            id           = id,
            boardSize    = boardSize,
            name         = name,
            initialState = initialState,
            solutionRoot = solutionRoot,
            playerColor  = playerColor,
            difficulty   = diff,
            category     = category
        )
    }

    // ── Tree building ─────────────────────────────────────────────────────────

    private fun buildSolutionTree(
        sequenceTail: List<SgfNode>,
        childTrees: List<SgfTree>,
        playerColor: GoStone,
        isMainLine: Boolean,
        boardSize: Int
    ): GoSgfNode? {
        // Build a chain: tail[0] -> tail[1] -> ... -> tail[n] -> (childTrees become siblings at tail[n])
        if (sequenceTail.isEmpty() && childTrees.isEmpty()) return null

        if (sequenceTail.isEmpty()) {
            // Children of the parent's last sequence node are the variations
            return buildVariationsRoot(childTrees, playerColor, boardSize)
        }

        val firstNode = sequenceTail.first()
        val (movePoint, moveColor) = extractMove(firstNode, playerColor, boardSize) ?: return null

        // Build the child recursively
        val rawChild = buildSolutionTree(
            sequenceTail = sequenceTail.drop(1),
            childTrees   = childTrees,
            playerColor  = playerColor,
            isMainLine   = true,
            boardSize    = boardSize
        )
        // If rawChild is the null-move variations wrapper produced by buildVariationsRoot,
        // attach its children directly so solutionCandidates never contains a null-move node.
        val children = when {
            rawChild == null        -> emptyList()
            rawChild.move == null   -> rawChild.children
            else                    -> listOf(rawChild)
        }
        val comment = firstNode.props["C"]?.firstOrNull() ?: ""

        return GoSgfNode(
            move       = movePoint,
            color      = moveColor,
            children   = children,
            comment    = comment,
            isMainLine = isMainLine
        )
    }

    private fun buildVariationsRoot(trees: List<SgfTree>, playerColor: GoStone, boardSize: Int): GoSgfNode? {
        if (trees.isEmpty()) return null
        // Convert each child tree to a GoSgfNode; first child = isMainLine=true
        val children = trees.mapIndexedNotNull { idx, tree ->
            val firstNode = tree.nodes.firstOrNull() ?: return@mapIndexedNotNull null
            val (movePoint, moveColor) = extractMove(firstNode, playerColor, boardSize) ?: return@mapIndexedNotNull null
            val subChild = buildSolutionTree(
                sequenceTail = tree.nodes.drop(1),
                childTrees   = tree.children,
                playerColor  = playerColor,
                isMainLine   = idx == 0,
                boardSize    = boardSize
            )
            // Apply the same null-move wrapper unwrapping as buildSolutionTree does, so that
            // a variation's subtree never contains a null-move wrapper as a direct child.
            val childrenOfNode = when {
                subChild == null      -> emptyList()
                subChild.move == null -> subChild.children  // unwrap the null-move wrapper
                else                  -> listOf(subChild)
            }
            val comment = firstNode.props["C"]?.firstOrNull() ?: ""
            GoSgfNode(
                move       = movePoint,
                color      = moveColor,
                children   = childrenOfNode,
                comment    = comment,
                isMainLine = idx == 0
            )
        }
        return GoSgfNode(move = null, color = playerColor, children = children)
    }

    private fun extractMove(node: SgfNode, playerColor: GoStone, boardSize: Int): Pair<GoPoint?, GoStone>? {
        val blackMove = node.props["B"]?.firstOrNull()
        val whiteMove = node.props["W"]?.firstOrNull()
        return when {
            blackMove != null -> Pair(sgfCoord(blackMove, boardSize), GoStone.BLACK)
            whiteMove != null -> Pair(sgfCoord(whiteMove, boardSize), GoStone.WHITE)
            else -> null
        }
    }

    // ── SGF coordinate conversion ─────────────────────────────────────────────

    private fun sgfCoord(s: String, boardSize: Int): GoPoint? {
        if (s.length < 2) return null
        if (s == "tt") return null   // pass
        val col = s[0] - 'a'
        val row = s[1] - 'a'
        if (col !in 0 until boardSize || row !in 0 until boardSize) return null
        return GoPoint(col, row)
    }

    // ── Recursive-descent SGF parser ─────────────────────────────────────────

    private fun parseTree(input: String, start: Int): Pair<SgfTree, Int>? {
        var pos = skipWs(input, start)
        if (pos >= input.length || input[pos] != '(') return null
        pos++

        val nodes = mutableListOf<SgfNode>()
        val children = mutableListOf<SgfTree>()

        pos = skipWs(input, pos)
        while (pos < input.length && input[pos] == ';') {
            val (node, next) = parseNode(input, pos)
            nodes += node
            pos = skipWs(input, next)
        }
        while (pos < input.length && input[pos] == '(') {
            val (child, next) = parseTree(input, pos) ?: break
            children += child
            pos = skipWs(input, next)
        }

        if (pos < input.length && input[pos] == ')') pos++
        return Pair(SgfTree(nodes, children), pos)
    }

    private fun parseNode(input: String, start: Int): Pair<SgfNode, Int> {
        var pos = start + 1  // skip ';'
        val props = mutableMapOf<String, MutableList<String>>()

        pos = skipWs(input, pos)
        while (pos < input.length && input[pos].isLetter() && input[pos].isUpperCase()) {
            val keyStart = pos
            while (pos < input.length && input[pos].isLetter()) pos++
            val key = input.substring(keyStart, pos)

            pos = skipWs(input, pos)
            while (pos < input.length && input[pos] == '[') {
                val (value, next) = parseValue(input, pos)
                props.getOrPut(key) { mutableListOf() } += value
                pos = skipWs(input, next)
            }
            pos = skipWs(input, pos)
        }
        return Pair(SgfNode(props), pos)
    }

    private fun parseValue(input: String, start: Int): Pair<String, Int> {
        var pos = start + 1  // skip '['
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos]
            when {
                ch == '\\' -> { pos++; if (pos < input.length) sb.append(input[pos]) }
                ch == ']'  -> return Pair(sb.toString(), pos + 1)
                else       -> sb.append(ch)
            }
            pos++
        }
        return Pair(sb.toString(), pos)
    }

    private fun skipWs(input: String, pos: Int): Int {
        var i = pos
        while (i < input.length && input[i].isWhitespace()) i++
        return i
    }
}
