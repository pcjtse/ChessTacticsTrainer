package com.example.chesstacticstrainer.di

import android.content.Context
import com.example.chesstacticstrainer.BuildConfig
import com.example.chesstacticstrainer.data.local.AnimalProgressStore
import com.example.chesstacticstrainer.data.local.AnimalSettingsStore
import com.example.chesstacticstrainer.data.local.GoProgressStore
import com.example.chesstacticstrainer.data.local.GoPuzzleAssetLoader
import com.example.chesstacticstrainer.data.local.GoPuzzleCache
import com.example.chesstacticstrainer.data.remote.GoproblemsApiService
import com.example.chesstacticstrainer.data.local.PuzzleCache
import com.example.chesstacticstrainer.data.local.ThemeStatsStore
import com.example.chesstacticstrainer.data.local.UserProgressStore
import com.example.chesstacticstrainer.data.local.XiangqiProgressStore
import com.example.chesstacticstrainer.data.local.XiangqiPuzzleCache
import com.example.chesstacticstrainer.data.remote.LichessApiService
import com.example.chesstacticstrainer.data.remote.OpenAiApiService
import com.example.chesstacticstrainer.data.remote.PychessApiService
import com.example.chesstacticstrainer.data.repository.AnimalPuzzleRepositoryImpl
import com.example.chesstacticstrainer.data.repository.GoPuzzleRepositoryImpl
import com.example.chesstacticstrainer.data.repository.PuzzleRepositoryImpl
import com.example.chesstacticstrainer.data.repository.XiangqiPuzzleRepositoryImpl
import com.example.chesstacticstrainer.domain.engine.AnimalChessEngine
import com.example.chesstacticstrainer.domain.engine.ChessEngine
import com.example.chesstacticstrainer.domain.engine.GoEngine
import com.example.chesstacticstrainer.domain.engine.XiangqiEngine
import com.example.chesstacticstrainer.domain.repository.AnimalPuzzleRepository
import com.example.chesstacticstrainer.domain.repository.GoPuzzleRepository
import com.example.chesstacticstrainer.domain.repository.PuzzleRepository
import com.example.chesstacticstrainer.domain.repository.XiangqiPuzzleRepository
import com.example.chesstacticstrainer.domain.usecase.GetAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetAnimalAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetAnimalUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetGoAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetGoUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextGoPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.GetNextXiangqiPuzzleUseCase
import com.example.chesstacticstrainer.domain.usecase.GetThemeStatsUseCase
import com.example.chesstacticstrainer.domain.usecase.GetUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiAiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiExplanationUseCase
import com.example.chesstacticstrainer.domain.usecase.GetXiangqiUserProgressUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateAnimalStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateGoStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.UpdateXiangqiStreakUseCase
import com.example.chesstacticstrainer.domain.usecase.ValidateMoveUseCase
import com.example.chesstacticstrainer.domain.usecase.ValidateXiangqiMoveUseCase
import com.example.chesstacticstrainer.engine.AnimalChessEngineImpl
import com.example.chesstacticstrainer.engine.ChessLibEngine
import com.example.chesstacticstrainer.engine.GoEngineImpl
import com.example.chesstacticstrainer.engine.XiangqiEngineImpl
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

    // Chess
    private val puzzleCache     = PuzzleCache(context, moshi)
    private val progressStore   = UserProgressStore(context)
    private val themeStatsStore = ThemeStatsStore(context, moshi)

    val engine: ChessEngine = ChessLibEngine()

    val repository: PuzzleRepository = PuzzleRepositoryImpl(
        puzzleCache     = puzzleCache,
        progressStore   = progressStore,
        themeStatsStore = themeStatsStore,
        apiService      = apiService
    )

    val getNextPuzzleUseCase   = GetNextPuzzleUseCase(repository, engine)
    val validateMoveUseCase    = ValidateMoveUseCase(engine)
    val getExplanationUseCase  = GetExplanationUseCase()
    val updateStreakUseCase     = UpdateStreakUseCase(repository)
    val getUserProgressUseCase  = GetUserProgressUseCase(repository)
    val getThemeStatsUseCase    = GetThemeStatsUseCase(repository)
    val getAiExplanationUseCase = GetAiExplanationUseCase(openAiApiService, BuildConfig.OPENAI_API_KEY)

    // Xiangqi
    private val xiangqiPuzzleCache   = XiangqiPuzzleCache(context, moshi)
    private val xiangqiProgressStore  = XiangqiProgressStore(context)
    private val pychessApiService     = PychessApiService(okHttpClient, moshi)

    val xiangqiEngine: XiangqiEngine = XiangqiEngineImpl()

    val xiangqiRepository: XiangqiPuzzleRepository = XiangqiPuzzleRepositoryImpl(
        puzzleCache   = xiangqiPuzzleCache,
        progressStore = xiangqiProgressStore,
        apiService    = pychessApiService
    )

    val getNextXiangqiPuzzleUseCase   = GetNextXiangqiPuzzleUseCase(xiangqiRepository, xiangqiEngine)
    val validateXiangqiMoveUseCase    = ValidateXiangqiMoveUseCase(xiangqiEngine)
    val getXiangqiExplanationUseCase  = GetXiangqiExplanationUseCase()
    val updateXiangqiStreakUseCase     = UpdateXiangqiStreakUseCase(xiangqiRepository)
    val getXiangqiUserProgressUseCase  = GetXiangqiUserProgressUseCase(xiangqiRepository)
    val getXiangqiAiExplanationUseCase = GetXiangqiAiExplanationUseCase(openAiApiService, BuildConfig.OPENAI_API_KEY)

    // Animal Chess
    private val animalProgressStore  = AnimalProgressStore(context)

    val animalEngine: AnimalChessEngine = AnimalChessEngineImpl()

    val animalRepository: AnimalPuzzleRepository = AnimalPuzzleRepositoryImpl(
        progressStore = animalProgressStore
    )

    val updateAnimalStreakUseCase     = UpdateAnimalStreakUseCase(animalRepository)
    val getAnimalUserProgressUseCase  = GetAnimalUserProgressUseCase(animalRepository)
    val getAnimalAiExplanationUseCase = GetAnimalAiExplanationUseCase(openAiApiService, BuildConfig.OPENAI_API_KEY)

    val animalSettingsStore = AnimalSettingsStore(context)

    // Go / Weiqi
    private val goPuzzleAssetLoader  = GoPuzzleAssetLoader(context.assets)
    private val allGoPuzzles by lazy { goPuzzleAssetLoader.loadAll() }
    private val goProgressStore      = GoProgressStore(context)
    private val goPuzzleCache        = GoPuzzleCache(context, moshi)
    private val goproblemsApiService = GoproblemsApiService(okHttpClient, moshi)

    val goEngine: GoEngine = GoEngineImpl()

    val goRepository: GoPuzzleRepository = GoPuzzleRepositoryImpl(
        puzzleCache     = goPuzzleCache,
        progressStore   = goProgressStore,
        apiService      = goproblemsApiService,
        fallbackPuzzles = allGoPuzzles
    )

    val getNextGoPuzzleUseCase    = GetNextGoPuzzleUseCase(goRepository)
    val updateGoStreakUseCase      = UpdateGoStreakUseCase(goRepository)
    val getGoUserProgressUseCase   = GetGoUserProgressUseCase(goRepository)
    val getGoAiExplanationUseCase  = GetGoAiExplanationUseCase(openAiApiService, BuildConfig.OPENAI_API_KEY)
}
