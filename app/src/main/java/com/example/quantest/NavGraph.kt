package com.example.quantest

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onStockClick = { stockId ->
                    navController.navigate("stockDetail/$stockId")
                }
            )
        }
        composable(BottomNavItem.Filter.route) { FilterScreen() }
        composable(BottomNavItem.Pattern.route) { PatternScreen() }
        composable(BottomNavItem.Menu.route) { MenuScreen() }

        // 종목 상세 화면 경로
        composable(
            route = "stockDetail/{stockId}",
            arguments = listOf(navArgument("stockId") { type = NavType.IntType })
        ) { backStackEntry ->
            val stockId = backStackEntry.arguments?.getInt("stockId") ?: 0
            StockDetailScreen(
                stockId = stockId,
                onBackClick = { navController.popBackStack() },
                onDetailClick = { /* 상세 페이지 이동 등 추가 동작 */ },
                onBuyClick = { /* 구매 버튼 클릭 동작 */ }
            )
        }
    }
}
