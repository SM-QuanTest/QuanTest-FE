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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quantest.model.ChangeDirection
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Red
import androidx.compose.foundation.lazy.items

@Preview(showBackground = true)
@Composable
fun PreviewStockListScreen() {
    val mockStockList = listOf(
        StockItem(
            id = 1,
            rank = 1,
            name = "삼성전자",
            imageUrl = "",
            price = "75,300원",
            change = "+1.25%",
            direction = ChangeDirection.UP
        ),
        StockItem(
            id = 2,
            rank = 2,
            name = "LG에너지솔루션",
            imageUrl = "",
            price = "420,000원",
            change = "0.00%",
            direction = ChangeDirection.FLAT
        ),
        StockItem(
            id = 3,
            rank = 3,
            name = "카카오",
            imageUrl = "",
            price = "51,700원",
            change = "-0.80%",
            direction = ChangeDirection.DOWN
        )
    )

    StockListScreen(
        title = "망치형 패턴",
        stockItems = mockStockList,
        onBackClick = {} // Preview용 빈 콜백
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    title: String,
    stockItems: List<StockItem>,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(TabType.ALL) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                // 타이틀 중앙 정렬
                title = {
                    Text(
                        text = title,
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
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            StockTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // TODO: 리스트 항목 클릭 시 상세 화면으로 이동 처리
            LazyColumn {
                items(stockItems.filterByTab(selectedTab)) { item ->
                    StockListItem(item = item)
                }
            }
        }
    }
}

// 상단 탭

// 탭 필터 기능
// ChangeDirection을 기준으로 분기
fun List<StockItem>.filterByTab(tab: TabType): List<StockItem> = when (tab) {
    TabType.ALL -> this
    TabType.UP -> filter { it.direction == ChangeDirection.UP }
    TabType.DOWN -> filter { it.direction == ChangeDirection.DOWN }
    TabType.HOLD -> filter { it.direction == ChangeDirection.FLAT }
}

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
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = when (tab) {
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
fun StockListItem(item: StockItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 종목 이미지
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "${item.name} 로고",
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp)),
            placeholder = painterResource(id = R.drawable.ic_placeholder),
            error = painterResource(id = R.drawable.ic_placeholder)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.price, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.change,
                    fontSize = 12.sp,
                    color = when (item.direction) {
                        ChangeDirection.UP -> Red
                        ChangeDirection.DOWN -> Blue
                        ChangeDirection.FLAT -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // 방향 아이콘
        when (item.direction) {
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



