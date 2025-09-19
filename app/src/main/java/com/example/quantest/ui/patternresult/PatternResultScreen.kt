package com.example.quantest.ui.patternresult

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.quantest.model.ChangeDirection
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.data.model.PatternStockItem
import com.example.quantest.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternResultScreen(
    patternId: Int,
    patternName: String,
    onBackClick: () -> Unit,
    onStockClick: (Long) -> Unit,
    viewModel: PatternResultViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(TabType.ALL) }

    val stocks by viewModel.stocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(patternId) {
        viewModel.loadStocks(patternId.toLong())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${patternName} 패턴",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 상단 탭
            StockTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 필터된 리스트 표시
                LazyColumn {
                    items(stocks.filterByTab(selectedTab)) { stock ->
                        StockListItem(
                            name = stock.stockName,
                            price = stock.chartClose,
                            change = "${(stock.chartChangePercentage * 100).format(2)}%",
                            direction = stock.toChangeDirection(),
                            onClick = { onStockClick(stock.stockId) }
                        )
                    }
                }
            }
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

// 상단 탭

fun PatternStockItem.toChangeDirection(): ChangeDirection {
    return when (recordDirection.lowercaseChar()) {
        'u' -> ChangeDirection.UP
        'd' -> ChangeDirection.DOWN
        else -> ChangeDirection.FLAT
    }
}

fun List<PatternStockItem>.filterByTab(tab: TabType): List<PatternStockItem> = when (tab) {
    TabType.ALL -> this
    TabType.UP -> filter { it.toChangeDirection() == ChangeDirection.UP }
    TabType.DOWN -> filter { it.toChangeDirection() == ChangeDirection.DOWN }
    TabType.HOLD -> filter { it.toChangeDirection() == ChangeDirection.FLAT }
}




