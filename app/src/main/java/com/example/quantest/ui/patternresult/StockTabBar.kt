package com.example.quantest.ui.patternresult

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// 탭 필터 기능
enum class TabType {
    ALL,   // 전체
    UP,    // 상승
    DOWN,  // 하락
    HOLD   // 보합
}

// 탭 필터
@Composable
fun StockTabBar(
    selectedTab: TabType,
    onTabSelected: (TabType) -> Unit
) {
    val tabs = listOf(TabType.ALL, TabType.UP, TabType.DOWN, TabType.HOLD)
    TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        when (tab) {
                            TabType.ALL -> "전체"
                            TabType.UP -> "상승"
                            TabType.DOWN -> "하락"
                            TabType.HOLD -> "보합"
                        }
                    )
                }
            )
        }
    }
}