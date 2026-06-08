package com.example.chesstacticstrainer.data.remote

import com.example.chesstacticstrainer.data.remote.dto.OpenAiRequest
import com.example.chesstacticstrainer.data.remote.dto.OpenAiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun complete(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}
