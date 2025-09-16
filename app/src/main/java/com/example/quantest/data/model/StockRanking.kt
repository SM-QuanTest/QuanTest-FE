package com.example.quantest.data.model

data class StockRankingData(
    val categoryName: String,
    val chartDate: String,
    val stocks: List<StockRanking>
)

data class StockRanking(
    val stockId: Long,
    val stockName: String,
    val chartChangePercentage: Double,
    val chartClose: Int,
    val recordDirection: Char? // 'u', 'd', 'n'
)

