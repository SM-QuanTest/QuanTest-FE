package com.example.quantest.ui.stockdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quantest.data.model.PatternRecord
import com.example.quantest.util.formatDate
import androidx.compose.foundation.clickable

@Composable
fun PatternTabContent(
    viewModel: StockDetailViewModel,
    stockId: Long,
    modifier: Modifier = Modifier
) {
    // 최초 로드
    LaunchedEffect(stockId) {
        viewModel.fetchPatternRecords(stockId)
    }

    val items by viewModel.patternRecords.collectAsState()
    val listState = rememberLazyListState()

    // 끝에 가까워지면 더 불러오기
    LaunchedEffect(stockId) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (
                total > 0 &&
                lastVisible >= total - 3 &&
                viewModel.hasMorePatterns() &&
                !viewModel.isPatternLoading()
            ) {
                viewModel.loadMorePatterns(stockId)
            }
        }
    }

    if (items.isEmpty() && viewModel.isPatternLoading()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("최근 탐지된 패턴이 없어요")
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(items, key = { _, it -> it.patternRecordId }) { index, item ->
            PatternRow(
                item = item,
                onClick = { viewModel.focusChartOn(item.patternRecordDate) } // ← 여기!
            )
            if (index < items.lastIndex) HorizontalDivider(thickness = 0.5.dp)
        }

        if (viewModel.isPatternLoading()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }
}

@Composable
private fun PatternRow(
    item: PatternRecord,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(formatDate(item.patternRecordDate), style = MaterialTheme.typography.bodyLarge)
        Text(
            item.patternName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
