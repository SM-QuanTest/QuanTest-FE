package com.example.quantest.navigation

sealed class NavRoute(val route: String) {
    data object Home: NavRoute("home")
    data object Filter: NavRoute("filter")
    data object Pattern: NavRoute("pattern")
    data object Menu: NavRoute("menu")
    data object SearchIndicator: NavRoute("searchIndicator")
    data object SearchStock: NavRoute("searchStock")

    data object StockDetail : NavRoute("stockDetail/{stockId}") {
        fun buildRoute(id: Int) = "stockDetail/$id"
    }
    data object StockList : NavRoute("stockList/{patternId}") {
        fun buildRoute(id: Int) = "stockList/$id"
    }

}