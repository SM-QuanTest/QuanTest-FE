package com.example.quantest.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.quantest.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.data.model.Indicator
import com.example.quantest.model.Stock
import com.example.quantest.ui.filter.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchStockScreen(
    onBackClick: () -> Unit = {},
    onSelect: (Stock) -> Unit,
    viewmodel: StockSearchViewModel = viewModel()
) {
    val query by viewmodel.query.collectAsState()
    val list by viewmodel.filtered.collectAsState()
    val uiState by viewmodel.uiState.collectAsState()
    var selectedTags by remember { mutableStateOf(listOf<String>()) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = { IconButton(onClick = onBackClick) {
                Icon(painterResource(id = R.drawable.ic_arrow_back), contentDescription = "뒤로가기")
            }},
            title = {
                TextField(
                    value = query,
                    onValueChange = viewmodel::updateQuery,
                    placeholder = { Text("종목 이름을 입력하세요.") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Icon(
                                painterResource(id = R.drawable.ic_cross_circle),
                                contentDescription = "Clear",
                                modifier = Modifier.clickable { viewmodel.updateQuery("") }
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }
        )

        if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        uiState.error?.let {
            Text("오류: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
            TextButton(onClick = { viewmodel.load() }, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("다시 시도")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            selectedTags.forEach { tag ->
                AssistChip(onClick = {}, label = { Text(tag) }, modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(list, key = { it.stockId }) { stock ->
                Text(
                    text = stock.stockName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(stock)
                            if (!selectedTags.contains(stock.stockName)) {
                                selectedTags = selectedTags + stock.stockName
                            }
                            viewmodel.updateQuery("")
                        }
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
    }
}