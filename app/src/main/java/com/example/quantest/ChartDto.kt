package com.example.quantest

data class ChartData(
    val chartId: Long,
    val chartDate: String,
    val chartOpen: Int,
    val chartHigh: Int,
    val chartLow: Int,
    val chartClose: Int,
    val chartVolume: Long
)

data class LatestChartData(
    val stockId: Long,
    val chartDate: String,
    val chartOpen: Long,
    val chartHigh: Long,
    val chartLow: Long,
    val chartClose: Long,
    val chartVolume: Long,
    val chartTurnover: Long,
    val chartChangePercentage: Double,
    val stockName: String,
    val priceChange: Int
)
