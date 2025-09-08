package com.example.quantest.data.api

import com.example.quantest.data.model.BaseResponse
import com.example.quantest.data.model.ChartData
import com.example.quantest.data.model.LatestChartData
import com.example.quantest.data.model.Pattern
import com.example.quantest.data.model.PatternStockItem
import com.example.quantest.data.model.StockRankingData
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

    // 패턴 다건 조회(상승/하락) (+패턴 이미지 url 추가)
    @GET("/patterns")
    suspend fun getPatterns(
        @Query("type") type: String? = null // "BULLISH" or "BEARISH"
    ): Response<BaseResponse<List<Pattern>>>

    // 패턴 탐지된 종목 다건 조회
    @GET("/patterns/{patternId}")
    suspend fun getPatternStocks(
        @Path("patternId") patternId: Long
    ): Response<BaseResponse<List<PatternStockItem>>>
}