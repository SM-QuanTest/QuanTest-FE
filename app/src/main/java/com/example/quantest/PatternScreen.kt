package com.example.quantest

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PatternScreen(
    viewModel: PatternViewModel = viewModel()
    //onPatternClick: (Int) -> Unit
) {
    val tabs = listOf("상승형 패턴", "하락형 패턴")
    var selectedTabIndex by remember { mutableStateOf(0) }

    val patternList by viewModel.patterns.collectAsState()
    val filteredPatterns = patternList.filter {
        if (selectedTabIndex == 0) it.patternDirection == "상승형" else it.patternDirection == "하락형"
    }

    Scaffold(
        topBar = { HomeTopBar() }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // 상단 탭
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 패턴 카드 리스트 (2열 그리드)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPatterns) { pattern ->
                    PatternCard(
                        patternName = pattern.patternName,
                        onClick = {
                            Log.d("PatternScreen", "Clicked patternId: ${pattern.patternId}")
                            //onPatternClick(pattern.patternId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PatternCard(
    patternName: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = patternName, fontSize = 14.sp)
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = "next",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}