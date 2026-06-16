package com.example.chesstacticstrainer.data.local

import android.content.res.AssetManager
import com.example.chesstacticstrainer.domain.model.GoPuzzle

class GoPuzzleAssetLoader(private val assets: AssetManager) {

    fun loadAll(): List<GoPuzzle> {
        val files = assets.list("go_puzzles") ?: return emptyList()
        return files.filter { it.endsWith(".sgf") }
            .sorted()
            .mapIndexedNotNull { idx, filename ->
                try {
                    val sgf = assets.open("go_puzzles/$filename").bufferedReader().readText()
                    val (diff, cat) = metaFromFilename(filename)
                    GoSgfParser.parse(sgf, id = "go_${idx + 1}", difficulty = diff, category = cat)
                } catch (_: Exception) { null }
            }
    }

    private fun metaFromFilename(name: String): Pair<Int, String> {
        val parts = name.removeSuffix(".sgf").split("_")
        val diff = parts.firstOrNull()?.toIntOrNull() ?: 1
        val cat  = parts.drop(1).firstOrNull() ?: "基本"
        return diff to when (cat) {
            "basic"        -> "基本"
            "intermediate" -> "中级"
            "advanced"     -> "高级"
            else           -> cat
        }
    }
}
