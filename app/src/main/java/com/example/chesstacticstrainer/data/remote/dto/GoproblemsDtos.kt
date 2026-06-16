package com.example.chesstacticstrainer.data.remote.dto

import com.squareup.moshi.Json

data class GoproblemsPuzzleDto(
    @Json(name = "id")           val id: Int         = 0,
    @Json(name = "sgf")          val sgf: String      = "",
    @Json(name = "problemLevel") val problemLevel: Int = 0,
    @Json(name = "playerColor")  val playerColor: String = "black"
)
