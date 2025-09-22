package com.example.quantest.model

data class Stock(
    val stockId: Int,
    val stockName: String
)

data class StockItem(
    val id: Long,
    val rank: Int?,
    val name: String,
    val imageUrl: String,
    val price: Int,
    val change: String,
    val direction: ChangeDirection
)

enum class ChangeDirection {
    UP, DOWN, FLAT
}
