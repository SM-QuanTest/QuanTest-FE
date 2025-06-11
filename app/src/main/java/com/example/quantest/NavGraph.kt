package com.example.quantest

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Filter.route) { FilterScreen() }
        composable(BottomNavItem.Pattern.route) { PatternScreen() }
        composable(BottomNavItem.Menu.route) { MenuScreen() }
    }
}
