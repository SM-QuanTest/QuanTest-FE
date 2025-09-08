package com.example.quantest.data.api

import com.example.quantest.data.api.StockApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "https://8b91e4d5fcdf.ngrok-free.app/"

    val stockApi: StockApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }

}