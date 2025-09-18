package com.example.quantest.data.model

data class StockRankingData(
    val categoryName: String,
    val chartDate: String,
    val stocks: List<StockResponse>
)

data class StockResponse(
    val stockId: Long,
    val stockName: String,
    val chartChangePercentage: Double,
    val chartClose: Int,
    val recordDirection: Char?
)

