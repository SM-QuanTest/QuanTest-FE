package com.example.quantest.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.quantest.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.example.quantest.ui.theme.QuanTestTheme


@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    QuanTestTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SearchScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBackClick: () -> Unit = {}) {
    var searchText by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(listOf<String>()) }

    val allItems = listOf(
        "엔비디아", "구글", "한화엔진", "엔켐", "엔씨소프트"
    )

    val filteredItems = if (searchText.isEmpty()) {
        emptyList()
    } else {
        allItems.filter { it.contains(searchText, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단바 + 검색창
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
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("검색어를 입력하세요.") },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross_circle),
                                contentDescription = "Clear",
                                modifier = Modifier.clickable { searchText = "" }
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

        // 선택된 태그
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            selectedTags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(Color.LightGray, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = tag, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    selectedTags = selectedTags - tag
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 검색 결과
        LazyColumn {
            items(filteredItems) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!selectedTags.contains(item)) {
                                selectedTags = selectedTags + item
                            }
                            searchText = ""
                        }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("") },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cross_circle),
                    contentDescription = "Clear",
                    modifier = Modifier.clickable { searchText = "" }
                )
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp)) // 둥근 모서리
            .background(Color(0xFFF2F2F2)), // 회색 배경
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,   // 배경 직접 넣었으니 투명
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,  // 밑줄 제거
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
