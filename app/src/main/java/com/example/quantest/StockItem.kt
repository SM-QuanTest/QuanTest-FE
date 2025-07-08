package com.example.quantest.model

data class StockItem(
    val rank: Int,
    val name: String,
    val imageUrl: String,
    val price: String,
    val change: String,
    val direction: ChangeDirection
)

enum class ChangeDirection {
    UP, DOWN, FLAT
}
