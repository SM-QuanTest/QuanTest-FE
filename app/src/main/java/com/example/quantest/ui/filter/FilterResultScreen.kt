package com.example.quantest.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.R
import com.example.quantest.data.model.StockResponse
import com.example.quantest.ui.patternresult.StockListItem
import com.example.quantest.ui.patternresult.StockTabBar
import com.example.quantest.ui.patternresult.TabType
import java.util.Locale
import com.example.quantest.model.ChangeDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterResultScreen(
    onBackClick: () -> Unit,
    onStockClick: (Long) -> Unit,
    viewModel: FilterViewModel = viewModel()   // FilterViewModel 공유
) {
    var selectedTab by remember { mutableStateOf(TabType.ALL) }

    val loading by viewModel.loading.collectAsState()
    val stocks by viewModel.results.collectAsState()  // 검색 결과 사용

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "검색 결과",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            StockTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val uiList = stocks.toUiList().filterByTab(selectedTab)
                if (uiList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("조건에 맞는 종목이 없습니다")
                    }
                } else {
                    LazyColumn {
                        items(uiList, key = { it.stockId }) { s ->
                            StockListItem(
                                name = s.stockName,
                                price = s.chartClose,
                                change = "${if (s.chartChangePercentage > 0) "+" else ""}${s.chartChangePercentage}%",
                                direction = s.toChangeDirection(),
                                onClick = { onStockClick(s.stockId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ----- 확장/헬퍼 ----- */

private fun List<StockResponse>.toUiList(): List<StockResponse> = this

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun StockResponse.toChangeDirection(): ChangeDirection =
    when (recordDirection?.lowercase(Locale.getDefault())?.firstOrNull()) {
        'u' -> ChangeDirection.UP
        'd' -> ChangeDirection.DOWN
        else -> ChangeDirection.FLAT
    }

fun List<StockResponse>.filterByTab(tab: TabType): List<StockResponse> = when (tab) {
    TabType.ALL  -> this
    TabType.UP   -> filter { it.toChangeDirection() == ChangeDirection.UP }
    TabType.DOWN -> filter { it.toChangeDirection() == ChangeDirection.DOWN }
    TabType.HOLD -> filter { it.toChangeDirection() == ChangeDirection.FLAT }
}