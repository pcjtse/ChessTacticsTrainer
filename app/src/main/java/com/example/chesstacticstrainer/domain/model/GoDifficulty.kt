package com.example.chesstacticstrainer.domain.model

// Level ranges map to goproblems.com's levelFrom/levelTo query params.
// Confirmed mapping from live API:
//   level 0-5   → ~30-25 kyu  (beginner)
//   level 5-10  → ~25 kyu     (easy)
//   level 10-15 → ~18 kyu     (medium)
//   level 15-20 → ~13 kyu     (hard)
//   level 20-30 → dan/expert  (expert)
enum class GoDifficulty(
    val displayName: String,
    val description: String,
    val levelFrom: Int,
    val levelTo: Int
) {
    BEGINNER("入门",  "25–30 级",   0,  5),
    EASY    ("简单",  "20–25 级",   5, 10),
    MEDIUM  ("中级",  "15–20 级",  10, 15),
    HARD    ("困难",  "10–15 级",  15, 20),
    EXPERT  ("高手",  "段位水平",   20, 30);

    fun contains(problemLevel: Int): Boolean = problemLevel in levelFrom..levelTo

    companion object {
        fun fromOrdinal(ordinal: Int): GoDifficulty =
            entries.getOrElse(ordinal) { MEDIUM }
    }
}
