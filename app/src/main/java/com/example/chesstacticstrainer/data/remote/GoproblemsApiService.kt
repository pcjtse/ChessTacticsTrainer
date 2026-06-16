package com.example.chesstacticstrainer.data.remote

import android.util.Log
import com.example.chesstacticstrainer.data.remote.dto.GoproblemsPuzzleDto
import com.example.chesstacticstrainer.domain.model.GoDifficulty
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "CTT-GO"
private const val BASE = "https://www.goproblems.com"

class GoproblemsApiService(
    private val client: OkHttpClient,
    private val moshi: Moshi
) {
    private val adapter = moshi.adapter(GoproblemsPuzzleDto::class.java)

    // Non-redirecting client used to read the Location header
    private val noRedirectClient = client.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    suspend fun fetchGoPuzzle(difficulty: GoDifficulty = GoDifficulty.MEDIUM): GoproblemsPuzzleDto =
        withContext(Dispatchers.IO) {
            val problemId = fetchNextProblemId(difficulty.levelFrom, difficulty.levelTo)
            Log.d(TAG, "fetching problem id=$problemId (${difficulty.displayName})")
            fetchProblemById(problemId)
        }

    // Step 1 — GET /problems/next?levelFrom=X&levelTo=Y
    // Server returns 302 with Location: /problems/<id>?...
    // Fallback: parse meta-refresh from HTML body if Location header is absent
    private fun fetchNextProblemId(levelFrom: Int, levelTo: Int): Int {
        val request = Request.Builder()
            .url("$BASE/problems/next?levelFrom=$levelFrom&levelTo=$levelTo")
            .header("User-Agent", "ChessTacticsTrainer/1.0 (Android)")
            .build()

        val (locationHeader, body) = noRedirectClient.newCall(request).execute().use { response ->
            Pair(response.header("Location"), response.body?.string())
        }

        val rawLocation = locationHeader
            ?: body?.let { Regex("""url='(/problems/(\d+)[^']*)'""").find(it)?.groupValues?.get(1) }
            ?: error("No redirect from /problems/next?levelFrom=$levelFrom&levelTo=$levelTo")

        return Regex("""/problems/(\d+)""").find(rawLocation)?.groupValues?.get(1)?.toIntOrNull()
            ?: error("Could not extract problem ID from: $rawLocation")
    }

    // Step 2 — GET /api/v2/problems/<id>  →  JSON with sgf, problemLevel, playerColor
    private fun fetchProblemById(id: Int): GoproblemsPuzzleDto {
        val request = Request.Builder()
            .url("$BASE/api/v2/problems/$id")
            .header("Accept", "application/json")
            .header("User-Agent", "ChessTacticsTrainer/1.0 (Android)")
            .build()

        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} for problem $id")
            response.body?.string() ?: error("Empty body for problem $id")
        }

        Log.d(TAG, "problem $id raw (first 120): ${body.take(120)}")
        return adapter.fromJson(body) ?: error("JSON parse failed for problem $id")
    }
}
