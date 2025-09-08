package com.example.quantest.ui.component

import com.example.quantest.R

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