package com.example.quantest.navigation

import android.net.Uri

sealed class NavRoute(val route: String) {
    data object Home: NavRoute("home")
    data object Filter: NavRoute("filter")
    data object FilterResult : NavRoute("filterResult")
    data object Pattern: NavRoute("pattern")
    data object PatternResult : NavRoute("patternResult/{patternId}/{patternName}") {
        fun buildRoute(patternId: Int, patternName: String) = "patternResult/$patternId/${Uri.encode(patternName)}"
    }
    data object Menu: NavRoute("menu")
    data object SearchIndicator: NavRoute("searchIndicator")
    data object SearchStock: NavRoute("searchStock")
    data object StockDetail : NavRoute("stockDetail/{stockId}") {
        fun buildRoute(stockId: Int) = "stockDetail/$stockId"
    }
}