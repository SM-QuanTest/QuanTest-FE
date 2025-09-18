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