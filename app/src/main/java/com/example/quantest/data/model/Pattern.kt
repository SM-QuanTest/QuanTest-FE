package com.example.quantest.data.model

data class Pattern(
    val patternId: Int,
    val patternName: String,
    val patternDirection: String, // "상승형" or "하락형"
    val patternImageUrl: String
)

data class PatternStockItem(
    val stockId: Long,
    val stockName: String,
    val chartChangePercentage: Double,
    val chartClose: Int,
    val recordDirection: Char
)