package com.example.quantest.ui.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.model.StockResponse
import com.example.quantest.model.ChangeDirection
import com.example.quantest.model.StockItem
import com.example.quantest.util.getTodayFormatted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _stockItems = mutableStateOf<List<StockItem>>(emptyList())
    val stockItems: List<StockItem> get() = _stockItems.value

    private val _chartDate = mutableStateOf<String?>(null)
    val chartDate: String? get() = _chartDate.value

    fun loadStocks(category: String, date: String = getTodayFormatted()) {
    viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getStockRankings(category, date)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    Log.d("HomeViewModel", "✅ API 성공: ${data?.categoryName}")

                    _chartDate.value = data?.chartDate ?: date

                    val rankingList = data?.stocks.orEmpty()
                    Log.d("HomeViewModel", "받은 종목 수: ${rankingList.size}")

                    _stockItems.value = rankingList.mapIndexed { index, dto ->
                        dto.toStockItem(index)
                    }
                } else {
                    Log.e("HomeViewModel", "❌ API 실패: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel",
                    "❌API 예외: ${e.javaClass.name}: ${e.message}\n${Log.getStackTraceString(e)}"
                )
            }
        }
    }

    private fun StockResponse.toStockItem(rank: Int): StockItem {
        val direction = when (recordDirection?.lowercaseChar()) {
            'u' -> ChangeDirection.UP
            'd' -> ChangeDirection.DOWN
            'n' -> ChangeDirection.FLAT
            else -> ChangeDirection.FLAT
        }

        return StockItem(
            id = stockId.toInt(),
            rank = rank + 1,
            name = stockName,
            imageUrl = "", // 없으면 빈 문자열
            price = chartClose,
            change = "${if (chartChangePercentage > 0) "+" else ""}${chartChangePercentage}%",
            direction = direction
        )
    }
}