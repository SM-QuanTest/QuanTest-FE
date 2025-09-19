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

    fun fetchChartData(stockId: Int) {
        val calendar = Calendar.getInstance()
        // TODO: 실제 날짜로 변경
        val endDate = getTodayFormatted()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = "2024-01-01"

        Log.d("ChartFetch", "start=$startDate, end=$endDate")

        viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getChartData(stockId, startDate, endDate)
                Log.d("ChartFetch", "response: ${response.code()}, body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    _chartData.value = response.body()?.data ?: emptyList<ChartData>()
                    Log.d("ChartFetch", "Fetched ${chartData.size} items")
                } else {
                    Log.w("ChartFetch", "API error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ChartFetch", "Exception: ${e.localizedMessage}", e)
            }
        }

        // 최신 일봉 데이터 호출
        fetchLatestChartData(stockId)
    }

    private fun fetchLatestChartData(stockId: Int) {
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