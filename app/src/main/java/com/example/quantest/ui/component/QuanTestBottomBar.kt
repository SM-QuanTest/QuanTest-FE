package com.example.quantest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.quantest.navigation.NavRoute
import com.example.quantest.ui.theme.StormGray10
import com.example.quantest.R
import androidx.compose.runtime.setValue


@Composable
fun QuanTestBottomBar(
    currentBackStackEntry: NavBackStackEntry?,
    navController: NavHostController
) {

    val currentScreen = currentBackStackEntry?.destination?.route
    val isBottomBar =
        currentScreen !in listOf(NavRoute.Home.route)

    val bottomItems = listOf(
        Triple(NavRoute.Home.route, stringResource(R.string.home), R.drawable.ic_home),
        Triple(NavRoute.Filter.route, stringResource(R.string.filter), R.drawable.ic_filter),
        Triple(NavRoute.Pattern.route, stringResource(R.string.pattern), R.drawable.ic_pattern),
        Triple(NavRoute.Menu.route, stringResource(R.string.menu), R.drawable.ic_menu)
    )
    var selectedItem by remember { mutableStateOf(bottomItems.first().first) }

    when (currentScreen) {
        NavRoute.Home.route -> selectedItem = NavRoute.Home.route
        NavRoute.Filter.route -> selectedItem = NavRoute.Filter.route
        NavRoute.Pattern.route -> selectedItem = NavRoute.Pattern.route
        NavRoute.Menu.route -> selectedItem = NavRoute.Menu.route
    }

    // 테두리/모서리 스타일 처리
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color.Transparent)
            .border(
                width = 0.4.dp,
                color = StormGray10,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        if (isBottomBar) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp // 평면 스타일
            ) {
                bottomItems.forEach { item ->
                    val (route, label, iconRes) = item
                    NavigationBarItem(
                        selected = selectedItem == route,
                        onClick = {
                            navController.navigate(route) {
                                selectedItem = label
                                popUpTo(NavRoute.Home.route) {
                                    inclusive = true
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
