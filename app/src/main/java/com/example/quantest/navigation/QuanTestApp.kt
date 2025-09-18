package com.example.quantest.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quantest.data.model.Indicator
import com.example.quantest.ui.component.QuanTestBottomBar
import com.example.quantest.ui.filter.FilterScreen
import com.example.quantest.ui.filter.FilterViewModel
import com.example.quantest.ui.filter.FilterResultScreen
import com.example.quantest.ui.home.HomeScreen
import com.example.quantest.ui.menu.MenuScreen
import com.example.quantest.ui.pattern.PatternScreen
import com.example.quantest.ui.search.SearchIndicatorScreen
import com.example.quantest.ui.search.SearchStockScreen
import com.example.quantest.ui.stockdetail.StockDetailScreen
import com.example.quantest.ui.stocklist.StockListScreen

@Composable
fun QuanTestApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = { QuanTestBottomBar(navBackStackEntry, navController)}
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = NavRoute.Home.route) {
                HomeScreen(
                    onSearchClick = {
                        navController.navigate(NavRoute.SearchStock.route)
                    },
                    onStockClick = { stockId ->
                        navController.navigate(NavRoute.StockDetail.buildRoute(stockId))
                    }
                )
            }

            composable(route = NavRoute.Filter.route) {
                FilterScreen(
                    onSearchClick = {
                        navController.navigate(NavRoute.SearchStock.route)
                    },
                    onOpenIndicatorSearch = {
                        navController.navigate(NavRoute.SearchIndicator.route) },
                    onNavigateToResult = { navController.navigate(NavRoute.FilterResult.route) },
                    indicatorResultFlow = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.getStateFlow<Indicator?>(key = "indicator_result", initialValue = null)
                )
            }

            composable(route = NavRoute.FilterResult.route) { backStackEntry ->
                // Filter 라우트의 BackStackEntry를 parent로 가져와 같은 VM 사용
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoute.Filter.route)
                }
                val filterViewModel: FilterViewModel = viewModel(parentEntry)

                FilterResultScreen(
                    viewModel = filterViewModel,              // ✅ 같은 VM 전달
                    onBackClick = { navController.popBackStack() },
                    onStockClick = { stockId ->
                        navController.navigate(NavRoute.StockDetail.buildRoute(stockId.toInt()))
                    }
                )
            }


            composable(route = NavRoute.Pattern.route) {
                PatternScreen(
                    onSearchClick = {
                        navController.navigate(NavRoute.SearchStock.route)
                    },
                    onPatternClick = { patternId ->
                        navController.navigate(NavRoute.StockList.buildRoute(patternId))
                    }
                )
            }

            composable(route = NavRoute.Menu.route) {
                MenuScreen()
            }

            composable(route = NavRoute.SearchStock.route) {
                SearchStockScreen(
                    onBackClick = { navController.popBackStack() },
                    onSelect = { indicator -> // TODO: 해당 종목 화면으로 이동
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("indicator_result", indicator)
                        navController.popBackStack()
                    }
                )
            }

            composable(route = NavRoute.SearchIndicator.route) {
                SearchIndicatorScreen(
                    onBackClick = { navController.popBackStack() },
                    onSelect = { indicator ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("indicator_result", indicator)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = NavRoute.StockDetail.route,
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

            composable(
                route = NavRoute.StockList.route,
                arguments = listOf(navArgument("patternId") { type = NavType.IntType })
            ) { backStackEntry ->
                val patternId = backStackEntry.arguments?.getInt("patternId") ?: 0
                StockListScreen(
                    patternId = patternId,
                    onBackClick = { navController.popBackStack() },
                    onStockClick = { stockId ->
                        navController.navigate("stockDetail/$stockId") // TODO
                    }
                )
            }
        }
    }
}
