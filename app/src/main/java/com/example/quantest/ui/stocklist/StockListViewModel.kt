package com.example.quantest.ui.stocklist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.model.PatternStockItem
import com.example.quantest.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockListViewModel() : ViewModel() {

    private val _stocks = MutableStateFlow<List<PatternStockItem>>(emptyList())
    val stocks: StateFlow<List<PatternStockItem>> = _stocks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadStocks(patternId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.stockApi.getPatternStocks(patternId)
                Log.d("StockListViewModel", "서버 응답: $response")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("StockListViewModel", "응답 Body: $body")
                    if (body != null && body.success) {
                        _stocks.value = body.data
                        Log.d("StockListViewModel", "받아온 종목 수: ${body.data.size}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("StockListViewModel", "종목 로드 실패", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}