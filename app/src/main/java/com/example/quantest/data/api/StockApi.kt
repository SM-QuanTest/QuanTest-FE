package com.example.quantest.data.api

import com.example.quantest.data.model.BaseResponse
import com.example.quantest.data.model.Page
import com.example.quantest.data.model.ChartData
import com.example.quantest.data.model.FilterRequest
import com.example.quantest.data.model.Indicator
import com.example.quantest.data.model.IndicatorConfig
import com.example.quantest.data.model.IndicatorLine
import com.example.quantest.data.model.LatestChartData
import com.example.quantest.data.model.Pattern
import com.example.quantest.data.model.PatternStockItem
import com.example.quantest.data.model.Sector
import com.example.quantest.data.model.StockRankingData
import com.example.quantest.data.model.StockResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
    suspend fun getChartPage(
        @Path("stockId") stockId: Long,
        @Query("limit") limit: Int? = null,
        @Query("cursorDate") cursorDate: String? = null
    ): Response<BaseResponse<Page<ChartData>>>

    // 종목 일봉 차트 단건 조회
    @GET("/charts/{stockId}/latest")
    suspend fun getLatestChartData(
        @Path("stockId") stockId: Long
    ): Response<BaseResponse<LatestChartData>>

    // 패턴 다건 조회(상승/하락)
    @GET("/patterns")
    suspend fun getPatterns(
        @Query("direction") direction: String? = null
    ): Response<BaseResponse<List<Pattern>>>

    // 패턴 탐지된 종목 다건 조회
    @GET("/patterns/{patternId}")
    suspend fun getPatternStocks(
        @Path("patternId") patternId: Long
    ): Response<BaseResponse<List<PatternStockItem>>>

    // 업종 다건 조회
    @GET("/sectors")
    suspend fun getSectors(): BaseResponse<List<Sector>>

    // 지표 이름 다건 조회
    @GET("/indicators")
    suspend fun getIndicators(): BaseResponse<List<Indicator>>

    // 지표에 따른 지표 라인 다건 조회
    @GET("/indicators/{indicatorId}/lines")
    suspend fun getIndicatorLines(
        @Path("indicatorId") indicatorId: Int
    ): BaseResponse<List<IndicatorLine>>

    // 지표에 따른 지표 설정 다건 조회
    @GET("/indicators/{indicatorId}/configs")
    suspend fun getIndicatorConfigs(
        @Path("indicatorId") indicatorId: Int
    ): BaseResponse<List<IndicatorConfig>>

    // 검색(필터링)
    @POST("/search")
    suspend fun searchStocks(
        @Query("date") date: String,
        @Body body: FilterRequest
    ): BaseResponse<List<StockResponse>>
}