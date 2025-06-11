package com.example.quantest

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: Int,
    val icon: Int
) {
    object Home : BottomNavItem("home", R.string.tab_home, R.drawable.ic_home)
    object Filter : BottomNavItem("filter", R.string.tab_filter, R.drawable.ic_filter)
    object Pattern : BottomNavItem("pattern", R.string.tab_pattern, R.drawable.ic_pattern)
    object Menu : BottomNavItem("menu", R.string.tab_menu, R.drawable.ic_menu)
}
