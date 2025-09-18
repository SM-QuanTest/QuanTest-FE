package com.example.quantest.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.api.StockApi
import com.example.quantest.data.model.Indicator
import com.example.quantest.data.model.IndicatorConfig
import com.example.quantest.data.model.IndicatorLine
import com.example.quantest.data.model.Sector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FilterViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 업종 목록 로드
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds


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

    // 지표 이름 다건 조회
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _items = MutableStateFlow<List<Indicator>>(emptyList())
    val items: StateFlow<List<Indicator>> = _items

    init {
        viewModelScope.launch {
            val res = RetrofitClient.stockApi.getIndicators()
            if (res.success) {
                _items.value = res.data.map { Indicator(it.indicatorId, it.indicatorName) }
            }
        }
    }

    fun updateQuery(q: String) { _query.value = q }

    val filtered: StateFlow<List<Indicator>> =
        combine(_items, _query) { all, q ->
            if (q.isBlank()) emptyList()
            else all.filter { it.indicatorName.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 지표에 따른 지표 라인 다건 조회
    private val _indicatorLines = MutableStateFlow<List<IndicatorLine>>(emptyList())
    val indicatorLines: StateFlow<List<IndicatorLine>> = _indicatorLines

    private val _selectedLineIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedLineIds: StateFlow<Set<Int>> = _selectedLineIds

    fun clearLines() {
        _indicatorLines.value = emptyList()
        _selectedLineIds.value = emptySet()
    }

    // ── 선택된 지표 목록
    private val _selectedIndicators = MutableStateFlow<List<Indicator>>(emptyList())
    val selectedIndicators: StateFlow<List<Indicator>> = _selectedIndicators

    // 지표별 라인 목록/선택상태
    private val _linesByIndicator = MutableStateFlow<Map<Int, List<IndicatorLine>>>(emptyMap())
    val linesByIndicator: StateFlow<Map<Int, List<IndicatorLine>>> = _linesByIndicator

    private val _selectedLineIdsByIndicator = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val selectedLineIdsByIndicator: StateFlow<Map<Int, Set<Int>>> = _selectedLineIdsByIndicator

    // 지표 추가 (중복 방지) + 라인 로딩
    fun addIndicator(ind: Indicator) {
        if (_selectedIndicators.value.any { it.indicatorId == ind.indicatorId }) return
        _selectedIndicators.value = _selectedIndicators.value + ind
        loadIndicatorLines(ind.indicatorId)
    }

    // 지표 제거
    fun removeIndicator(indicatorId: Int) {
        _selectedIndicators.value = _selectedIndicators.value.filterNot { it.indicatorId == indicatorId }
        _linesByIndicator.value = _linesByIndicator.value - indicatorId
        _selectedLineIdsByIndicator.value = _selectedLineIdsByIndicator.value - indicatorId
    }

    // 라인 로딩
    fun loadIndicatorLines(indicatorId: Int) = viewModelScope.launch {
        runCatching { RetrofitClient.stockApi.getIndicatorLines(indicatorId) }
            .onSuccess { res ->
                val lines = if (res.success) res.data.map {
                    IndicatorLine(it.indicatorLineId, it.indicatorLineName)
                } else emptyList()
                _linesByIndicator.value = _linesByIndicator.value + (indicatorId to lines)
                _selectedLineIdsByIndicator.value =
                    _selectedLineIdsByIndicator.value + (indicatorId to emptySet())
            }
            .onFailure {
                _linesByIndicator.value = _linesByIndicator.value + (indicatorId to emptyList())
            }
    }

    // 라인 토글
    fun toggleLine(indicatorId: Int, lineId: Int) {
        val cur = _selectedLineIdsByIndicator.value[indicatorId].orEmpty().toMutableSet()
        if (cur.contains(lineId)) cur.remove(lineId) else cur.add(lineId)
        _selectedLineIdsByIndicator.value =
            _selectedLineIdsByIndicator.value + (indicatorId to cur)
    }

    // 지표 설정
    private val _configs = MutableStateFlow<List<IndicatorConfig>>(emptyList())
    val configs: StateFlow<List<IndicatorConfig>> = _configs

    fun loadConfigs(indicatorId: Int) {
        viewModelScope.launch {
            runCatching { RetrofitClient.stockApi.getIndicatorConfigs(indicatorId).data }
                .onSuccess { list ->
                    _configs.value = list
                }
                .onFailure {
                    _configs.value = emptyList()
                }
        }
    }

}