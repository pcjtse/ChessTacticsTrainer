package com.example.chesstacticstrainer.di

import android.content.Context
import com.example.chesstacticstrainer.data.local.PuzzleCache
import com.example.chesstacticstrainer.data.local.ThemeStatsStore
import com.example.chesstacticstrainer.data.local.UserProgressStore
import com.example.chesstacticstrainer.BuildConfig
import com.example.chesstacticstrainer.data.remote.LichessApiService
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.repository.PuzzleRepositoryImpl
import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import com.example.chesstacticstrainer.domain.usecase.GetAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.GetThemeStatsUseCase
import com.example.chesstacticstrainer.domain.usecase.GetUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.ValidateMoveUseCase
import com.example.chesstacticstrainer.engine.ChessLibEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://lichess.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: LichessApiService = retrofit.create(LichessApiService::class.java)

    private val openAiRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val openAiApiService: OpenAiApiService = openAiRetrofit.create(OpenAiApiService::class.java)

    private val puzzleCache = PuzzleCache(context, moshi)
    private val progressStore = UserProgressStore(context)
    private val themeStatsStore = ThemeStatsStore(context, moshi)

    val engine: ChessEngine = ChessLibEngine()

    val repository: PuzzleRepository = PuzzleRepositoryImpl(
        puzzleCache = puzzleCache,
        progressStore = progressStore,
        themeStatsStore = themeStatsStore,
        apiService = apiService
    )

    val getNextPuzzleUseCase = GetNextPuzzleUseCase(repository, engine)
    val validateMoveUseCase = ValidateMoveUseCase(engine)
    val getExplanationUseCase = GetExplanationUseCase()
    val updateStreakUseCase = UpdateStreakUseCase(repository)
    val getUserProgressUseCase = GetUserProgressUseCase(repository)
    val getThemeStatsUseCase = GetThemeStatsUseCase(repository)
    val getAiExplanationUseCase = GetAiExplanationUseCase(openAiApiService, BuildConfig.OPENAI_API_KEY)
}
