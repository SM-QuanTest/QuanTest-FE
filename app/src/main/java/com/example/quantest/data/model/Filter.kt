package com.example.quantest.data.model

enum class PriceType(val api: String, val label: String) {
    OPEN("CHART_OPEN", "시가"),
    CLOSE("CHART_CLOSE", "종가"),
    HIGH("CHART_HIGH", "고가"),
    LOW("CHART_LOW", "저가"),
    VOLUME("CHART_VOLUME", "거래량");
}

// Chart 필터
data class ChartFilter(
    val priceType: String, // "CHART_OPEN" 등
    val priceMin: Int?,
    val priceMax: Int?
)

// Indicator 라인 필터
data class IndicatorLineFilter(
    val filterType: String, // "COMPARE_INDICATOR" 등
    val indicatorLineIdA: Long?,
    val indicatorLineIdB: Long?,
    val operator: String?,
    val threshold: Double?
)

// Indicator 필터
data class IndicatorFilter(
    val indicatorId: Long,
    val indicatorLineFilterList: List<IndicatorLineFilter>
)

// 최종 요청 바디
data class FilterRequest(
    val sectorIds: List<Int>,
    val chartFilterList: List<ChartFilter>,
    val indicatorFilterList: List<IndicatorFilter>
)

// 필요 시 ViewModel 상태로 들고가도 됨
data class ChartFilterUi(
    val priceType: String, // "CHART_OPEN" | "CHART_CLOSE" | ...
    val min: Int? = null,
    val max: Int? = null
)