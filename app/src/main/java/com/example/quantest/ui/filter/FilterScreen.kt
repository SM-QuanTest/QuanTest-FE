package com.example.quantest.ui.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.ui.component.QuanTestTopBar

enum class FilterTab(val title: String) {
    DATE("날짜"),
    INDUSTRY("업종"),
    CHART("차트"),
    INDICATOR("지표")
}

@Composable
fun FilterScreen(
    viewModel: FilterViewModel = viewModel(),
    onSearchClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(FilterTab.DATE) }


    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) }
    ){ innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // 상단 탭
            QuanTestTabRow(
                tabs = FilterTab.values(),
                selected = selectedTab,
                onSelected = { selectedTab = it },
                titleProvider = { it.title }
            )

            // 탭별 화면
            when (selectedTab) {
                FilterTab.DATE -> DateFilterScreen()
                FilterTab.INDUSTRY -> IndustryFilterScreen()
                FilterTab.CHART -> ChartFilterScreen()
                FilterTab.INDICATOR -> IndicatorFilterScreen()
            }

        }
    }
}

@Composable
fun DateFilterScreen() {
    Text("날짜 필터 화면")
}

@Composable
fun IndustryFilterScreen() {
    Text("업종 필터 화면")
}

@Composable
fun ChartFilterScreen() {
    Text("차트 필터 화면")
}

@Composable
fun IndicatorFilterScreen() {
    Text("지표 필터 화면")
}