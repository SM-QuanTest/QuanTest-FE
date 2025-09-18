package com.example.quantest.ui.filter

import com.example.quantest.data.model.CompareOp
import com.example.quantest.data.model.PriceType

data class ChartRangeSelection(
    val type: PriceType,
    val min: Int?,   // null 허용
    val max: Int?
)

data class IndicatorLineSelection(
    val indicatorId: Int,
    val indicatorName: String,
    val lineId: Int,
    val lineName: String,
    val min: Int?,
    val max: Int?
)

data class CompareSelection(
    val indicatorId: Int,
    val indicatorName: String,
    val rowId: Int,
    val leftLineId: Int,
    val leftLineName: String,
    val op: CompareOp,
    val rightLineId: Int,
    val rightLineName: String
)