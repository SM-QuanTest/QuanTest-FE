package com.example.quantest

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.model.ChangeDirection
import com.example.quantest.model.StockItem
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Navy
import com.example.quantest.ui.theme.Red
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onSearchClick: () -> Unit,
    onStockClick: (Int) -> Unit
) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    val categoryMap = listOf("TURNOVER", "VOLUME", "RISE", "FALL")
    val selectedCategory = categoryMap.getOrNull(selectedIndex) ?: "TURNOVER"

    LaunchedEffect(selectedIndex) {
        viewModel.loadStocks(selectedCategory)
    }

    val stockItems = viewModel.stockItems

    Scaffold(
        topBar = { CommonTopBar(onSearchClick = onSearchClick) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            RankingTitle()
            FilterTabs(
                selectedIndex = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )
            RankingList(
                items = stockItems,
                onItemClick = { stockId ->
                    Log.d("HomeScreen", "Clicked stockId: $stockId")
                    onStockClick(stockId)
                }
            )
        }
    }
}

@Composable
fun RankingTitle() {
    Text(
        text = "코스닥 랭킹",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun FilterTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("거래대금", "거래량", "상승", "하락")

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = label) }
            )
        }
    }
}

@Composable
fun RankingList(items: List<StockItem>, onItemClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            StockRankItem(item = item, onClick = { onItemClick(item.id) })
        }
    }
}

@Composable
fun StockRankItem(item: StockItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 순위 텍스트
        Text(
            text = item.rank.toString(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(32.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            color = Navy
        )

//        // 로고 이미지
//        AsyncImage(
//            model = item.imageUrl,
//            contentDescription = "${item.name} 로고",
//            modifier = Modifier
//                .size(40.dp)
//                .clip(RoundedCornerShape(18.dp)),
//            placeholder = painterResource(id = R.drawable.ic_placeholder),
//            error = painterResource(id = R.drawable.ic_placeholder)
//        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatPrice(item.price), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.change,
                    fontSize = 13.sp,
                    color = when {
                        item.change.startsWith("+") -> Red
                        item.change.startsWith("-") -> Blue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

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
