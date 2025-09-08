package com.example.quantest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quantest.ui.component.BottomNavItem
import com.example.quantest.ui.theme.StormGray10

@Composable
fun QuanTestBottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavItem.Home,
        BottomNavItem.Filter,
        BottomNavItem.Pattern,
        BottomNavItem.Menu
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
            screens.forEach { screen ->
                AddItem(
                    item = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    item: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    val interactionSource = remember { MutableInteractionSource() }

    NavigationBarItem(
        selected = selected,
        onClick = {
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.route,
                modifier = Modifier.size(24.dp),
            )
        },
        label = {
            Text(
                text = stringResource(id = item.label),
                fontSize = 12.sp,
            )
        },
        alwaysShowLabel = true,
        interactionSource = interactionSource,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent
        )
    )
}
