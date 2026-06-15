package com.example.chesstacticstrainer.data.remote.dto

import com.squareup.moshi.Json

data class PychessPuzzleDto(
    @Json(name = "_id") val id: String,
    @Json(name = "f")   val fen: String,
    @Json(name = "m")   val moves: String,   // comma-separated, pychess coord (ranks 1–10)
    @Json(name = "lm")  val lastMove: String = "",
    @Json(name = "t")   val type: String = "",
    @Json(name = "e")   val eval: String = ""  // "#1", "#2", etc.
)
