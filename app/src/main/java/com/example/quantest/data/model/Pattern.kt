package com.example.quantest.data.model

data class Pattern(
    val patternId: Int,
    val patternName: String,
    val patternDirection: String,
    val patternImageUrl: String
)

data class PatternStockItem(
    val stockId: Long,
    val stockName: String,
    val chartChangePercentage: Double,
    val chartClose: Int,
    val recordDirection: Char
)

data class PatternRecord(
    val patternId: Long,
    val patternName: String,
    val patternRecordId: Long,
    val patternDirection: String,
    val patternRecordDate: String
)