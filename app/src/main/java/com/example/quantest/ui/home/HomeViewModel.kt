package com.example.quantest.ui.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.model.StockRanking
import com.example.quantest.model.ChangeDirection
import com.example.quantest.model.StockItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _stockItems = mutableStateOf<List<StockItem>>(emptyList())
    val stockItems: List<StockItem> get() = _stockItems.value

    // 오늘 날짜를 yyyy-MM-dd 형식으로 반환
    private fun getTodayFormatted(): String {
        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        return formatter.format(today)
    }

    // 어제 날짜를 yyyy-MM-dd 형식으로 반환
    private fun getYesterdayFormatted(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1) // 하루 전으로 설정
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        return formatter.format(calendar.time)
    }

    // TODO: 실제 날짜로 변경
    //fun loadStocks(category: String, date: String = getYesterdayFormatted()) {
    fun loadStocks(category: String, date: String = "2025-08-04") {
    viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getStockRankings(category, date)
                if (response.isSuccessful) {
                    Log.d("HomeViewModel", "✅ API 성공: ${response.body()?.data?.categoryName}")
                    response.body()?.data?.stocks?.let { rankingList ->
                        Log.d("HomeViewModel", "받은 종목 수: ${rankingList.size}")

                        _stockItems.value = rankingList.mapIndexed { index, dto ->
                            dto.toStockItem(index)
                        }
                    }
                } else {
                    Log.e("HomeViewModel", "❌ API 실패: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                // 에러 로깅 또는 UI 처리
                Log.e("HomeViewModel", "❌ API 호출 중 예외 발생", e)
            }
        }
    }

    private fun StockRanking.toStockItem(rank: Int): StockItem {
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
            change = "${if (chartChangePercentage >= 0) "+" else ""}${chartChangePercentage}%",
            direction = direction
        )
    }
}