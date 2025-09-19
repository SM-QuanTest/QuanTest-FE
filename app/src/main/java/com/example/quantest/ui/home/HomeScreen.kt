package com.example.quantest.ui.home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
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
import com.example.quantest.ui.component.QuanTestTopBar
import com.example.quantest.R
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.util.formatChartDate
import com.example.quantest.util.formatPrice

enum class HomeTab(val title: String, val category: String) {
    TURNOVER("거래대금", "TURNOVER"),
    VOLUME("거래량", "VOLUME"),
    RISE("상승", "RISE"),
    FALL("하락", "FALL")
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onSearchClick: () -> Unit,
    onStockClick: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.TURNOVER) }

    LaunchedEffect(selectedTab) {
        viewModel.loadStocks(selectedTab.category)
    }

    val chartDate = viewModel.chartDate
    val stockItems = viewModel.stockItems

    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            RankingTitle(chartDate = chartDate)

            QuanTestTabRow(
                tabs = HomeTab.values(),
                selected = selectedTab,
                onSelected = { selectedTab = it },
                titleProvider = { it.title }
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
fun RankingTitle(chartDate: String?) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "코스닥 랭킹",
            fontSize = 24.sp, // MaterialTheme.typography.titleLarge
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!chartDate.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${formatChartDate(chartDate)} 기준",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RankingList(items: List<StockItem>, onItemClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        items(
            count = items.size,
            key = { index -> items[index].id }
        ) { index ->
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
                modifier = Modifier.size(20.dp)
            )
            ChangeDirection.DOWN -> Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = "하락",
                tint = Blue,
                modifier = Modifier.size(20.dp)
            )
            ChangeDirection.FLAT -> Icon(
                painter = painterResource(id = R.drawable.ic_flat),
                contentDescription = "보합",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
