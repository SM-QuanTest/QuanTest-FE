package com.example.quantest

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {
    // 종목 랭킹 리스트 조회
    @GET("/stocks/rankings")
    suspend fun getStockRankings(
        @Query("category") category: String,
        @Query("date") date: String
    ): Response<StockRankingResponse>
}
