package com.example.quantest

data class StockRankingData(
    val categoryName: String,
    val stocks: List<StockRankingDto>
)

data class StockRankingDto(
    val stockId: Long,
    val stockName: String,
    val chartChangePercentage: Double,
    val chartClose: Int,
    val recordDirection: Char? // 'u', 'd', 'n'
)

