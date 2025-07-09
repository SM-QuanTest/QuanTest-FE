package com.example.quantest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.quantest.ui.theme.Navy


@Preview(showBackground = true)
@Composable
fun StockDetailScreenPreview() {
    MaterialTheme {
        StockDetailScreen(
            stockId = 1234,
            onBackClick = {},
            onDetailClick = {},
            onBuyClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stockId: Int,
    onBackClick: () -> Unit,
    onDetailClick: () -> Unit,
    onBuyClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("차트", "종목정보")

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 바
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "뒤로가기"
                    )
                }
            },
            title = { /* 생략 */ }
        )

        // 종목 기본 정보
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "엔비디아", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "54,321원", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
            Text(
                text = "+308원 (0.6%)",
                fontSize = 14.sp,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 탭
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        when (selectedTab) {
            0 -> ChartTabContent()
            1 -> InfoTabContent()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 일별 시세 보기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDetailClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "일별 시세 보기", fontWeight = FontWeight.Medium)
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = "더보기",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 구매하기 버튼
        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("구매하기")
        }
    }
}

// 차트 탭
@Composable
fun ChartTabContent() {
    // 실제 구현 시 WebView or Chart API 사용
}

// 종목 정보 탭
@Composable
fun InfoTabContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "시세", fontWeight = FontWeight.Bold)
        Text(text = "5월 23일 기준", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // 1년 최저가 ~ 최고가 슬라이더 (단순 텍스트 표현 예시)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1년 최저가\n49,900원", fontSize = 12.sp)
            Text("1년 최고가\n88,800원", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 시가, 종가, 거래량, 거래대금
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("시가", fontWeight = FontWeight.SemiBold)
                Text("54,300원")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("종가", fontWeight = FontWeight.SemiBold)
                Text("54,500원")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("거래량", fontWeight = FontWeight.SemiBold)
                Text("14,557,811주")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("거래대금", fontWeight = FontWeight.SemiBold)
                Text("7,907억원")
            }
        }
    }
}
