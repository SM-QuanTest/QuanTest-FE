package com.example.quantest.ui.search

import kotlin.collections.filter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.model.Stock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StockSearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class StockSearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StockSearchUiState(isLoading = true))
    val uiState: StateFlow<StockSearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    // 디바운스 필터
    val filtered: StateFlow<List<Stock>> =
        combine(_stocks, _query.debounce(200)) { list, q ->
            if (q.isBlank()) list
            else list.filter { it.stockName.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var loadJob: Job? = null

    init { load() }

    fun updateQuery(value: String) { _query.value = value }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = StockSearchUiState(isLoading = true)
            try {
                val res = RetrofitClient.stockApi.getStocks()
                if (res.success) {
                    _stocks.value = res.data
                    _uiState.value = StockSearchUiState(isLoading = false)
                } else {
                    _uiState.value = StockSearchUiState(
                        isLoading = false,
                        error = res.message.ifBlank { "서버 오류" }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = StockSearchUiState(
                    isLoading = false,
                    error = e.message ?: "네트워크 오류"
                )
            }
        }
    }
}