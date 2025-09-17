package com.example.quantest.ui.component

import androidx.annotation.DrawableRes
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

data class BottomItem(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
)

@Composable
fun QuanTestBottomBar(
    currentBackStackEntry: NavBackStackEntry?,
    navController: NavHostController
) {

    val bottomItems = listOf(
        BottomItem(NavRoute.Home.route, stringResource(R.string.home), R.drawable.ic_home),
        BottomItem(NavRoute.Filter.route, stringResource(R.string.filter), R.drawable.ic_filter),
        BottomItem(NavRoute.Pattern.route, stringResource(R.string.pattern), R.drawable.ic_pattern),
        BottomItem(NavRoute.Menu.route, stringResource(R.string.menu), R.drawable.ic_menu)
    )
    val bottomRoutes = bottomItems.map { it.route }.toSet()
    val isBottomBarVisible = currentBackStackEntry?.destination
        ?.hierarchy
        ?.any { it.route in bottomRoutes } == true

    val selectedRoute = currentBackStackEntry?.destination?.route

    if (!isBottomBarVisible) return

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
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp // 평면 스타일
        ) {
            bottomItems.forEach { item ->
                NavigationBarItem(
                    selected = selectedRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
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
