package com.example.quantest.ui.filter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.model.ChartFilter
import com.example.quantest.data.model.ChartFilterUi
import com.example.quantest.data.model.CompareOp
import com.example.quantest.data.model.FilterRequest
import com.example.quantest.data.model.Indicator
import com.example.quantest.data.model.IndicatorConfig
import com.example.quantest.data.model.IndicatorFilter
import com.example.quantest.data.model.IndicatorLine
import com.example.quantest.data.model.IndicatorLineFilter
import com.example.quantest.data.model.LineCompareState
import com.example.quantest.data.model.LineComparison
import com.example.quantest.data.model.PriceType
import com.example.quantest.data.model.Sector
import com.example.quantest.data.model.StockResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.orEmpty

class FilterViewModel : ViewModel() {

    // ── 로딩/에러 ────────────────────────────────────────────────────────
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ── 업종 선택 ─────────────────────────────────────────────────────────
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds

    fun loadSectors() {
        if (_loading.value || _sectors.value.isNotEmpty()) return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = RetrofitClient.stockApi.getSectors()
                _sectors.value = res.data
            } catch (t: Throwable) {
                _error.value = t.message ?: "업종 목록 로드 실패"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleSector(id: Int) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun removeSector(id: Int) { _selectedIds.value = _selectedIds.value - id }
    fun clearSelected() { _selectedIds.value = emptySet() }
    fun setSelected(ids: Set<Int>) { _selectedIds.value = ids }

    // ── 차트 범위 선택(칩/요청 변환용) ─────────────────────────────────────
    private val _chartSelections = MutableStateFlow<Map<PriceType, ChartRangeSelection>>(emptyMap())
    val chartSelections: StateFlow<Map<PriceType, ChartRangeSelection>> = _chartSelections

    fun applyChartRange(type: PriceType, min: Int?, max: Int?) {
        _chartSelections.value = _chartSelections.value + (type to ChartRangeSelection(type, min, max))
    }
    fun removeChartSelection(type: PriceType) {
        _chartSelections.value = _chartSelections.value - type
    }
    fun clearChartSelections() { _chartSelections.value = emptyMap() }

    // ── 지표 검색/선택 ────────────────────────────────────────────────────
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

    // ── 지표 라인/선택 상태 ───────────────────────────────────────────────
    private val _indicatorLines = MutableStateFlow<List<IndicatorLine>>(emptyList())
    val indicatorLines: StateFlow<List<IndicatorLine>> = _indicatorLines

    private val _selectedLineIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedLineIds: StateFlow<Set<Int>> = _selectedLineIds

    fun clearLines() {
        _indicatorLines.value = emptyList()
        _selectedLineIds.value = emptySet()
    }

    private val _selectedIndicators = MutableStateFlow<List<Indicator>>(emptyList())
    val selectedIndicators: StateFlow<List<Indicator>> = _selectedIndicators

    private val _linesByIndicator = MutableStateFlow<Map<Int, List<IndicatorLine>>>(emptyMap())
    val linesByIndicator: StateFlow<Map<Int, List<IndicatorLine>>> = _linesByIndicator

    private val _selectedLineIdsByIndicator = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val selectedLineIdsByIndicator: StateFlow<Map<Int, Set<Int>>> = _selectedLineIdsByIndicator

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

    fun toggleLine(indicatorId: Int, lineId: Int) {
        val cur = _selectedLineIdsByIndicator.value[indicatorId].orEmpty().toMutableSet()
        if (cur.contains(lineId)) cur.remove(lineId) else cur.add(lineId)
        _selectedLineIdsByIndicator.value =
            _selectedLineIdsByIndicator.value + (indicatorId to cur)
    }

    // ── 지표 설정 ────────────────────────────────────────────────────────
    private val _configs = MutableStateFlow<List<IndicatorConfig>>(emptyList())
    val configs: StateFlow<List<IndicatorConfig>> = _configs

    fun loadConfigs(indicatorId: Int) {
        viewModelScope.launch {
            runCatching { RetrofitClient.stockApi.getIndicatorConfigs(indicatorId).data }
                .onSuccess { list -> _configs.value = list }
                .onFailure { _configs.value = emptyList() }
        }
    }

    // ── 라인 비교 선택 상태 ───────────────────────────────────────────────
    private val _compareByIndicator = MutableStateFlow<Map<Int, LineCompareState>>(emptyMap())
    val compareByIndicator: StateFlow<Map<Int, LineCompareState>> = _compareByIndicator

    fun addIndicator(ind: Indicator) {
        if (_selectedIndicators.value.any { it.indicatorId == ind.indicatorId }) return
        _selectedIndicators.value = _selectedIndicators.value + ind
        loadIndicatorLines(ind.indicatorId)
        _compareByIndicator.value = _compareByIndicator.value + (ind.indicatorId to LineCompareState())
    }

    fun removeIndicator(indicatorId: Int) {
        _selectedIndicators.value = _selectedIndicators.value.filterNot { it.indicatorId == indicatorId }
        _linesByIndicator.value = _linesByIndicator.value - indicatorId
        _selectedLineIdsByIndicator.value = _selectedLineIdsByIndicator.value - indicatorId
        _compareByIndicator.value = _compareByIndicator.value - indicatorId
    }

    fun toggleLineCompare(indicatorId: Int, enabled: Boolean) {
        val cur = _compareByIndicator.value[indicatorId] ?: LineCompareState()
        _compareByIndicator.value = _compareByIndicator.value + (indicatorId to cur.copy(enabled = enabled))
        if (enabled && cur.rows.isEmpty()) addCompareRow(indicatorId)
    }

    fun addCompareRow(indicatorId: Int) {
        val cur = _compareByIndicator.value[indicatorId] ?: LineCompareState()
        val nextId = (cur.rows.maxOfOrNull { it.id } ?: 0) + 1
        _compareByIndicator.value = _compareByIndicator.value + (
                indicatorId to cur.copy(rows = cur.rows + LineComparison(id = nextId))
                )
    }

    fun removeCompareRow(indicatorId: Int, rowId: Int) {
        val cur = _compareByIndicator.value[indicatorId] ?: return
        _compareByIndicator.value = _compareByIndicator.value + (
                indicatorId to cur.copy(rows = cur.rows.filterNot { it.id == rowId })
                )
    }

    fun updateCompareRow(
        indicatorId: Int,
        rowId: Int,
        left: Int? = null,
        op: CompareOp? = null,
        right: Int? = null
    ) {
        val cur = _compareByIndicator.value[indicatorId] ?: return
        val newRows = cur.rows.map {
            if (it.id != rowId) it else it.copy(
                leftLineId = left ?: it.leftLineId,
                op = op ?: it.op,
                rightLineId = right ?: it.rightLineId
            )
        }
        _compareByIndicator.value = _compareByIndicator.value + (indicatorId to cur.copy(rows = newRows))
    }

    // ── 지표 값/비교 칩 상태 ─────────────────────────────────────────────
    private val _indicatorSelections =
        MutableStateFlow<Map<Pair<Int, Int>, IndicatorLineSelection>>(emptyMap())
    val indicatorSelections: StateFlow<Map<Pair<Int, Int>, IndicatorLineSelection>> = _indicatorSelections

    fun applyIndicatorRange(
        indicatorId: Int,
        indicatorName: String,
        lineId: Int,
        lineName: String,
        min: Int?,
        max: Int?
    ) {
        val key = indicatorId to lineId
        _indicatorSelections.value = _indicatorSelections.value + (
                key to IndicatorLineSelection(indicatorId, indicatorName, lineId, lineName, min, max)
                )
    }

    fun removeIndicatorRange(indicatorId: Int, lineId: Int) {
        val key = indicatorId to lineId
        _indicatorSelections.value = _indicatorSelections.value - key
    }

    private val _compareSelections = MutableStateFlow<Map<Int, List<CompareSelection>>>(emptyMap())
    val compareSelections: StateFlow<Map<Int, List<CompareSelection>>> = _compareSelections

    /** "라인 비교 > 적용" 버튼 눌렀을 때: 현재 상태를 칩으로 저장 */
    fun applyLineCompare(indicatorId: Int) {
        val state = _compareByIndicator.value[indicatorId] ?: return
        if (!state.enabled) {
            _compareSelections.value = _compareSelections.value - indicatorId
            return
        }

        val indicatorName = _selectedIndicators.value
            .firstOrNull { it.indicatorId == indicatorId }?.indicatorName ?: "지표"
        val lineNameMap = _linesByIndicator.value[indicatorId]
            ?.associateBy({ it.indicatorLineId }, { it.indicatorLineName })
            .orEmpty()

        val rows = state.rows.filter { it.leftLineId != null && it.rightLineId != null && it.op != null }
        val chips = rows.map { row ->
            CompareSelection(
                indicatorId = indicatorId,
                indicatorName = indicatorName,
                rowId = row.id,
                leftLineId = row.leftLineId!!,
                leftLineName = lineNameMap[row.leftLineId] ?: "라인${row.leftLineId}",
                op = row.op!!,
                rightLineId = row.rightLineId!!,
                rightLineName = lineNameMap[row.rightLineId] ?: "라인${row.rightLineId}"
            )
        }
        _compareSelections.value = _compareSelections.value + (indicatorId to chips)
    }

    fun removeCompareSelection(indicatorId: Int, rowId: Int) {
        val cur = _compareSelections.value[indicatorId].orEmpty()
        val next = cur.filterNot { it.rowId == rowId }
        _compareSelections.value =
            if (next.isEmpty()) _compareSelections.value - indicatorId
            else _compareSelections.value + (indicatorId to next)

        val state = _compareByIndicator.value[indicatorId] ?: return
        _compareByIndicator.value = _compareByIndicator.value + (
                indicatorId to state.copy(rows = state.rows.filterNot { it.id == rowId })
                )
    }

    // ── 검색 ─────────────────────────────────────────────────────────────
    private val _results = MutableStateFlow<List<StockResponse>>(emptyList())
    val results: StateFlow<List<StockResponse>> = _results

    fun search(
        date: String,
        chartFiltersFromUi: List<ChartFilterUi> = emptyList()
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            Log.d("FilterViewModel", "🔍 검색 시작 date=$date")   // ① 호출 확인
            try {
                val req = buildFilterRequest(chartFiltersFromUi)
                if (req.sectorIds.isEmpty() &&
                    req.chartFilterList.isEmpty() &&
                    req.indicatorFilterList.isEmpty()
                ) {
                    _results.value = emptyList()
                    _loading.value = false
                    return@launch
                }
                val res = RetrofitClient.stockApi.searchStocks(date, req)

                Log.d(
                    "FilterViewModel",
                    "✅ 응답 success=${res.success}, message=${res.message}, dataCount=${res.data.size}"
                ) // ③ 응답 확인

                if (res.success) _results.value = res.data
                else _error.value = res.message.ifBlank { "검색 실패" }
            } catch (t: Throwable) {
                Log.e("FilterViewModel", "❌ 검색 중 오류 발생", t) // ④ 예외 확인
                _error.value = t.message ?: "네트워크 오류"
            } finally {
                _loading.value = false
            }
        }
    }
}

private fun CompareOp.toServerOp(): String = when (this) {
    CompareOp.GREATER_THAN -> "GREATER_THAN"
    CompareOp.GREATER_THAN_OR_EQUAL -> "GREATER_THAN_OR_EQUAL"
    CompareOp.EQUAL -> "EQUAL"
    CompareOp.LESS_THAN_OR_EQUAL -> "LESS_THAN_OR_EQUAL"
    CompareOp.LESS_THAN -> "LESS_THAN"
}

/** 지표 한 개(indicatorId)의 LineCompareState -> IndicatorFilter */
private fun FilterViewModel.buildIndicatorFilterForCompareIndicator(
    indicatorId: Int,
    state: LineCompareState
): IndicatorFilter? {
    if (!state.enabled) return null

    val validRows = state.rows.filter { it.leftLineId != null && it.rightLineId != null && it.op != null }
    if (validRows.isEmpty()) return null

    val lineFilters = validRows.map { row ->
        IndicatorLineFilter(
            filterType = "COMPARE_INDICATOR",
            indicatorLineIdA = row.leftLineId?.toLong(),
            indicatorLineIdB = row.rightLineId?.toLong(),
            operator = row.op!!.toServerOp(),
            threshold = null
        )
    }
    return IndicatorFilter(
        indicatorId = indicatorId.toLong(),
        indicatorLineFilterList = lineFilters
    )
}

/** ViewModel 상태 -> FilterRequest */
private fun FilterViewModel.buildFilterRequest(
    chartFiltersFromUi: List<ChartFilterUi> = emptyList()
): FilterRequest {
    val sectorIds = selectedIds.value.toList()

    val chartFilterList = chartSelections.value.values.map {
        ChartFilter(priceType = it.type.api, priceMin = it.min, priceMax = it.max)
    }

    val indicatorFilters =
        compareByIndicator.value.entries.mapNotNull { (indicatorId, state) ->
            buildIndicatorFilterForCompareIndicator(indicatorId, state)
        }

    return FilterRequest(
        sectorIds = sectorIds,
        chartFilterList = chartFilterList,
        indicatorFilterList = indicatorFilters
    )
}