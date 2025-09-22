package com.example.quantest.data.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor

object RetrofitClient {
    const val BASE_URL = "https://78d2fa7f1848.ngrok-free.app/"
    // URL 로깅용 Interceptor
    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        Log.d("RetrofitClient", "요청 URL: ${request.url}")
        chain.proceed(request)
    }

    // OkHttpClient 에 Interceptor 추가
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 요청 주소 자동 로그
        .build()

    val stockApi: StockApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }

}