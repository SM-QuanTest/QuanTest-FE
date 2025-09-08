package com.example.quantest.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.model.Sector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FilterViewModel : ViewModel() {
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 업종 목록 로드 (/sectors)
    fun loadSectors() {
        if (_loading.value || _sectors.value.isNotEmpty()) return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = RetrofitClient.stockApi.getSectors() // SectorResponse
                _sectors.value = res.data
            } catch (t: Throwable) {
                _error.value = t.message ?: "업종 목록 로드 실패"
            } finally {
                _loading.value = false
            }
        }
    }

    /** 토글 선택 */
    fun toggleSector(id: Int) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    /** 칩 X 버튼 */
    fun removeSector(id: Int) {
        _selectedIds.value = _selectedIds.value - id
    }

    fun clearSelected() { _selectedIds.value = emptySet() }
    fun setSelected(ids: Set<Int>) { _selectedIds.value = ids }

}