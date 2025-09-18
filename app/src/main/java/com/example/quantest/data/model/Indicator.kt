package com.example.quantest.data.model

import java.io.Serializable

data class Indicator(
    val indicatorId: Int,
    val indicatorName: String
) : Serializable

data class IndicatorLine(
    val indicatorLineId: Int,
    val indicatorLineName: String
)

data class IndicatorConfig(
    val indicatorConfigName: String,
    val indicatorConfigValue: String
)

// 연산자
enum class CompareOp {
    GREATER_THAN,        // >
    GREATER_THAN_OR_EQUAL, // >=
    EQUAL,               // ==
    LESS_THAN_OR_EQUAL,  // <=
    LESS_THAN            // <
}

// 1개의 비교식: 왼쪽 라인 vs 오른쪽 라인
data class LineComparison(
    val id: Int,                         // 로컬 식별자
    var leftLineId: Int? = null,
    var op: CompareOp = CompareOp.GREATER_THAN,
    var rightLineId: Int? = null
)

// 지표별 라인비교 상태
data class LineCompareState(
    val enabled: Boolean = false,
    val rows: List<LineComparison> = emptyList()
)