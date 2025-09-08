package com.example.quantest.model

data class StockItem(
    val id: Int,
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
