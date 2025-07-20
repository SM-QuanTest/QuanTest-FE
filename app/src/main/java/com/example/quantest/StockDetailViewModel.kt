package com.example.quantest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class StockDetailViewModel : ViewModel() {

    var chartData by mutableStateOf<List<ChartData>>(emptyList())
        private set
    var latestChartData by mutableStateOf<LatestChartData?>(null)

    fun fetchChartData(stockId: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        //val endDate = sdf.format(calendar.time)
        val endDate = "2024-04-09"
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        //val startDate = sdf.format(calendar.time)
        val startDate = "2024-01-01"

        Log.d("ChartFetch", "start=$startDate, end=$endDate")

        viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getChartData(stockId, startDate, endDate)
                Log.d("ChartFetch", "response: ${response.code()}, body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    chartData = response.body()?.data ?: emptyList()
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
                    latestChartData = response.body()?.data
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
