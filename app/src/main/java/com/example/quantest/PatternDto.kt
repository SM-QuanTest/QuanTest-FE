package com.example.quantest

data class Pattern(
    val patternId: Int,
    val patternName: String,
    val patternDirection: String // "상승형" or "하락형"
)

// API 응답용 DTO
data class PatternResponse(
    val patternId: Int,
    val patternName: String,
    val patternDirection: String
)