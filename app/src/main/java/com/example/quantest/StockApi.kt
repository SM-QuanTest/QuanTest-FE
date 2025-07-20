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
    ): Response<BaseResponse<StockRankingData>>

    // 종목 일봉 차트 조회
    @GET("/charts/{stockId}")
    suspend fun getChartData(
        @Path("stockId") stockId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<BaseResponse<List<ChartData>>>

    // 종목 일봉 차트 단건 조회
    @GET("/charts/{stockId}/latest")
    suspend fun getLatestChartData(@Path("stockId") stockId: Int): Response<BaseResponse<LatestChartData>>

}
