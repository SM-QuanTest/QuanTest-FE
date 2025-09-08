package com.example.quantest

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quantest.navigation.NavGraph
import com.example.quantest.ui.component.BottomNavItem
import com.example.quantest.ui.component.QuanTestBottomBar

@Composable
fun QuanTestApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단 탭이 필요한 화면만 정의
    val bottomBarRoutes = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Filter.route,
        BottomNavItem.Pattern.route,
        BottomNavItem.Menu.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                QuanTestBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavGraph(navController = navController)
        }
    }
}
