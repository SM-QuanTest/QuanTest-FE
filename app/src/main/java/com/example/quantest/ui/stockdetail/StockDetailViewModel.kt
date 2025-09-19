package com.example.quantest.ui.stockdetail

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.model.ChartData
import com.example.quantest.data.model.LatestChartData
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.util.getTodayFormatted
import kotlinx.coroutines.launch
import java.util.Calendar

class StockDetailViewModel : ViewModel() {

    private val _chartData = mutableStateOf<List<ChartData>>(emptyList())
    val chartData: List<ChartData> get() = _chartData.value

    private val _latestChartData = mutableStateOf<LatestChartData?>(null)
    val latestChartData: LatestChartData? get() = _latestChartData.value

    private var nextCursor: String? = null
    private var hasNext: Boolean = true
    private var isLoading = false

    fun fetchChartData(stockId: Long) {
        // 초기화
        nextCursor = null
        hasNext = true

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.stockApi.getChartPage(
                    stockId = stockId,
                    cursorDate = null,
                    limit = null
                )
                Log.d("ChartFetch", "response: ${response.code()}, body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val page = response.body()!!.data!!

                    _chartData.value = page.contents.sortedBy { it.chartDate }
                    nextCursor = page.nextCursor
                    hasNext = page.hasNext

                    Log.d("ChartFetch", "Fetched ${chartData.size} items")
                } else {
                    Log.w("ChartFetch", "API error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ChartFetch", "Exception: ${e.localizedMessage}", e)
            }
        }

        fetchLatestChartData(stockId)
    }

    fun loadMore(stockId: Long) {
        if (!hasNext || isLoading) return
        val cursor = nextCursor ?: return

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.stockApi.getChartPage(
                    stockId = stockId.toLong(),
                    cursorDate = cursor,
                    limit = null
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val page = response.body()!!.data!!
                    val merged = (_chartData.value + page.contents)
                        .distinctBy { it.chartId }
                        .sortedBy { it.chartDate } // 항상 시간 오름차순으로 고정

                    _chartData.value = merged
                    nextCursor = page.nextCursor
                    hasNext = page.hasNext
                } else {
                    Log.w("ChartMore", "API ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChartMore", "Exception: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchLatestChartData(stockId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getLatestChartData(stockId)
                if (response.isSuccessful) {
                    _latestChartData.value = response.body()?.data
                    Log.d("LatestChart", "불러온 데이터: $latestChartData")
                } else {
                    Log.e("LatestChart", "실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LatestChart", "에러: ${e.message}")
            }
        }
    }

}