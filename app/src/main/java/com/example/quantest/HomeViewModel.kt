package com.example.quantest

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.model.ChangeDirection
import com.example.quantest.model.StockItem
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var stockItems by mutableStateOf<List<StockItem>>(emptyList())
        private set

    //fun loadStocks(category: String, date: String = LocalDate.now().toString()) {
    fun loadStocks(category: String, date: String = "2025-07-18") {
    viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getStockRankings(category, date)
                if (response.isSuccessful) {
                    Log.d("HomeViewModel", "✅ API 성공: ${response.body()?.data?.categoryName}")
                    response.body()?.data?.stocks?.let { rankingList ->
                        Log.d("HomeViewModel", "받은 종목 수: ${rankingList.size}")

                        stockItems = rankingList.mapIndexed { index, dto ->
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

    private fun StockRankingDto.toStockItem(rank: Int): StockItem {
        return StockItem(
            id = stockId.toInt(),
            rank = rank + 1,
            name = stockName,
            imageUrl = "", // 없으면 빈 문자열
            price = "${chartClose}원",
            change = "${if (chartChangePercentage >= 0) "+" else ""}${chartChangePercentage}%",
            direction = when {
                chartChangePercentage > 0 -> ChangeDirection.UP
                chartChangePercentage < 0 -> ChangeDirection.DOWN
                else -> ChangeDirection.FLAT
            }
        )
    }
}
