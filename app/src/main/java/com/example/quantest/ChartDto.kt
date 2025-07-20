package com.example.quantest

data class ChartResponse(
    val success: Boolean,
    val message: String,
    val data: List<ChartData>
)

data class ChartData(
    val chartId: Long,
    val chartDate: String,
    val chartOpen: Int,
    val chartHigh: Int,
    val chartLow: Int,
    val chartClose: Int,
    val chartVolume: Long
)
