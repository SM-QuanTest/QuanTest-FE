package com.example.quantest.ui.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.model.Pattern
import com.example.quantest.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatternViewModel : ViewModel() {
    private val _patterns = MutableStateFlow<List<Pattern>>(emptyList())
    val patterns: StateFlow<List<Pattern>> = _patterns

    // 에러 메시지 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 탭 인덱스 상태
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    init {
        fetchPatterns()
    }

    // 탭 인덱스 변경 함수
    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
        fetchPatterns() // 탭이 바뀌면 다시 데이터 가져오기
    }

    private fun fetchPatterns() {
        viewModelScope.launch {
            try {
                // StateFlow 값은 value로 읽음
                val type = if (_selectedTabIndex.value == 0) "BULLISH" else "BEARISH"
                val response = RetrofitClient.stockApi.getPatterns(type)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.data?.let { list ->
                        val patternList = list.map {
                            Pattern(
                                patternId = it.patternId,
                                patternName = it.patternName,
                                patternDirection = it.patternDirection,
                                patternImageUrl = it.patternImageUrl
                            )
                        }
                        _patterns.value = patternList
                    } ?: run {
                        _errorMessage.value = "No data"
                    }
                } else {
                    _errorMessage.value = "API error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Exception: ${e.message}"
            }
        }
    }
}