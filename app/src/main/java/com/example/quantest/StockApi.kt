package com.example.quantest

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApi {
    // 종목 랭킹 리스트 조회
    @GET("/stocks/rankings")
    suspend fun getStockRankings(
        @Query("category") category: String,
        @Query("date") date: String
    ): Response<StockRankingResponse>

    // 종목 일봉 차트 조회
    @GET("/charts/{stockId}")
    suspend fun getChartData(
        @Path("stockId") stockId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ChartResponse>
}
