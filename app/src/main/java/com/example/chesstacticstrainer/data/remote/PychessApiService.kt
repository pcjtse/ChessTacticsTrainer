package com.example.chesstacticstrainer.data.remote

import android.util.Log
import com.example.chesstacticstrainer.data.remote.dto.PychessPuzzleDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class PychessApiService(
    private val client: OkHttpClient,
    private val moshi: Moshi
) {
    private val adapter = moshi.adapter(PychessPuzzleDto::class.java)

    suspend fun fetchXiangqiPuzzle(): PychessPuzzleDto = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://www.pychess.org/puzzle/xiangqi")
            .header("User-Agent", "ChessTacticsTrainer/1.0 (Android)")
            .build()

        val html = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("pychess HTTP ${response.code}")
            response.body?.string() ?: error("Empty pychess response")
        }

        val raw = Regex("""data-puzzle="([^"]+)"""").find(html)
            ?.groupValues?.get(1)
            ?: error("data-puzzle attribute not found in pychess response")

        val json = raw
            .replace("&#34;", "\"")
            .replace("&quot;", "\"")
            .replace("&#38;", "&")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&apos;", "'")

        Log.d("CTT-XQ", "pychess puzzle json: $json")
        adapter.fromJson(json) ?: error("Failed to parse pychess puzzle JSON")
    }
}
