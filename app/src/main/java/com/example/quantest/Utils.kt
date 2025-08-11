package com.example.quantest

import java.text.NumberFormat
import java.util.Locale

fun formatPrice(price: Number?): String {
    if (price == null) return "-"
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return formatter.format(price) + "원"
}

fun formatVolume(volume: Number?): String {
    if (volume == null) return "-"
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return formatter.format(volume) + "주"
}