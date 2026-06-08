package com.example.chesstacticstrainer.data.remote

import com.example.chesstacticstrainer.data.remote.dto.PuzzleResponseDto
import retrofit2.http.GET

interface LichessApiService {
    @GET("api/puzzle/next")
    suspend fun getNextPuzzle(): PuzzleResponseDto

    @GET("api/puzzle/daily")
    suspend fun getDailyPuzzle(): PuzzleResponseDto
}
