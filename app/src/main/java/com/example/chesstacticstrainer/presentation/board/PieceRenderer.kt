package com.example.chesstacticstrainer.presentation.board

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.caverock.androidsvg.SVG

object PieceRenderer {

    private val cache = mutableMapOf<String, ImageBitmap>()

    private val keys = listOf(
        "wK", "wQ", "wR", "wB", "wN", "wP",
        "bK", "bQ", "bR", "bB", "bN", "bP"
    )

    fun load(context: Context): Map<String, ImageBitmap> {
        if (cache.size == keys.size) return cache.toMap()
        keys.forEach { key ->
            if (!cache.containsKey(key)) {
                cache[key] = renderSvg(context, "pieces/$key.svg")
            }
        }
        return cache.toMap()
    }

    private fun renderSvg(context: Context, assetPath: String): ImageBitmap {
        return try {
            val svg = SVG.getFromAsset(context.assets, assetPath)
            val sizePx = 256
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            svg.renderToCanvas(Canvas(bitmap), RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()))
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }
}
