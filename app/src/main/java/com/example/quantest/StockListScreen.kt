package com.example.quantest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.example.quantest.model.StockItem
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quantest.model.ChangeDirection
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Red
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    patternId: Int,
    onBackClick: () -> Unit,
    onStockClick: (Long) -> Unit,
    viewModel: StockListViewModel = viewModel()
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
                // 타이틀 중앙 정렬
                title = {
                    Text(
                        text = "임시 제목",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
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
                            price = stock.chartClose.toString(),
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
enum class ChangeDirection { UP, DOWN, FLAT }

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

// 리스트 아이템
@Composable
fun StockListItem(
    name: String,
    price: String,
    change: String,
    direction: ChangeDirection,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 종목 이미지
        AsyncImage(
            model = "",
            contentDescription = "${name} 로고",
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp)),
            placeholder = painterResource(id = R.drawable.ic_placeholder),
            error = painterResource(id = R.drawable.ic_placeholder)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = price, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    fontSize = 12.sp,
                    color = when (direction) {
                        ChangeDirection.UP -> Red
                        ChangeDirection.DOWN -> Blue
                        ChangeDirection.FLAT -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // 방향 아이콘
        when (direction) {
            ChangeDirection.UP -> Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = "상승",
                tint = Red,
                modifier = Modifier.size(16.dp)
            )
            ChangeDirection.DOWN -> Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = "하락",
                tint = Blue,
                modifier = Modifier.size(16.dp)
            )
            ChangeDirection.FLAT -> Icon(
                painter = painterResource(id = R.drawable.ic_flat),
                contentDescription = "보합",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}



