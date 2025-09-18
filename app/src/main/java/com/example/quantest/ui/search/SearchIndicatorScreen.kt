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
import com.example.quantest.ui.filter.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchIndicatorScreen(
    onBackClick: () -> Unit = {},
    onSelect: (Indicator) -> Unit,
    viewmodel: FilterViewModel = viewModel()
) {
    // ViewModel 상태 사용
    val query by viewmodel.query.collectAsState()
    val list by viewmodel.filtered.collectAsState()

    // (선택 태그 UI 유지할 거면 그대로 둬도 됨)
    var selectedTags by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "뒤로가기"
                    )
                }
            },
            title = {
                TextField(
                    value = query,                         // ViewModel 값 사용
                    onValueChange = viewmodel::updateQuery,// ViewModel로 업데이트
                    placeholder = { Text("지표 이름을 입력하세요.") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross_circle),
                                contentDescription = "Clear",
                                modifier = Modifier.clickable { viewmodel.updateQuery("") } // ✅ 초기화
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

        Spacer(modifier = Modifier.height(8.dp))

        // 선택된 태그 UI는 필요 시 유지
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            selectedTags.forEach { tag ->
                // ... 기존 태그 UI 그대로 ...
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ViewModel에서 필터된 지표 리스트 사용
        LazyColumn {
            items(list) { indicator ->
                Text(
                    text = indicator.indicatorName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(indicator)              // 선택 전달
                            // 태그를 쓰려면 아래 유지(옵션)
                            if (!selectedTags.contains(indicator.indicatorName)) {
                                selectedTags = selectedTags + indicator.indicatorName
                            }
                            viewmodel.updateQuery("")        // 검색창 비우기
                        }
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
    }
}