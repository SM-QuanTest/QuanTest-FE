@file:JvmName("HomeScreenKt")

package com.example.quantest

import android.R.id
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quantest.model.ChangeDirection
import com.example.quantest.model.StockItem
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Navy
import com.example.quantest.ui.theme.Red


@Composable
fun HomeScreen() {

    var selectedIndex by remember { mutableStateOf(0) }

    // 더미 데이터
    val byAmountList = listOf(
        StockItem(id = 1001, rank = 1, name = "에코프로", imageUrl = "", price = "138,000원", change = "+2.1%", direction = ChangeDirection.UP),
        StockItem(1002, 2, "셀트리온헬스케어", "", "83,500원", "-1.3%", ChangeDirection.DOWN),
        StockItem(1003, 3, "엘앤에프", "", "170,200원", "+0.7%", ChangeDirection.UP),
        StockItem(1004, 4, "펄어비스", "", "43,000원", "-0.5%", ChangeDirection.DOWN),
        StockItem(1005, 5, "씨젠", "", "31,100원", "0.0%", ChangeDirection.FLAT),
    )

    val byVolumeList = listOf(
        StockItem(2001, 1, "HLB", "", "58,000원", "+3.2%", ChangeDirection.UP),
        StockItem(2002, 2, "위메이드", "", "26,700원", "-1.7%", ChangeDirection.DOWN),
        StockItem(2003, 3, "CJ ENM", "", "89,000원", "+0.9%", ChangeDirection.UP),
        StockItem(2004, 4, "동화약품", "", "15,200원", "-0.8%", ChangeDirection.DOWN),
        StockItem(2005, 5, "KMH", "", "9,300원", "0.0%", ChangeDirection.FLAT),
    )

    val gainersList = listOf(
        StockItem(3001, 1, "대주전자재료", "", "71,000원", "+7.5%", ChangeDirection.UP),
        StockItem(3002, 2, "고영", "", "34,200원", "+5.3%", ChangeDirection.UP),
        StockItem(3003, 3, "아프리카TV", "", "62,800원", "+4.2%", ChangeDirection.UP),
        StockItem(3004, 4, "크래프톤", "", "212,000원", "+3.9%", ChangeDirection.UP),
        StockItem(3005, 5, "한글과컴퓨터", "", "22,100원", "+3.6%", ChangeDirection.UP),
    )

    val losersList = listOf(
        StockItem(4001, 1, "카카오게임즈", "", "41,800원", "-5.2%", ChangeDirection.DOWN),
        StockItem(4002, 2, "컴투스", "", "64,300원", "-4.6%", ChangeDirection.DOWN),
        StockItem(4003, 3, "코미팜", "", "5,800원", "-3.9%", ChangeDirection.DOWN),
        StockItem(4004, 4, "알서포트", "", "4,200원", "-2.8%", ChangeDirection.DOWN),
        StockItem(4005, 5, "이연제약", "", "30,000원", "-2.4%", ChangeDirection.DOWN),
    )

    val popularList = listOf(
        StockItem(5001, 1, "엔비디아", "", "1,234,000원", "+2.1%", ChangeDirection.UP),
        StockItem(5002, 2, "삼성전자", "", "72,000원", "+1.0%", ChangeDirection.UP),
        StockItem(5003, 3, "LG에너지솔루션", "", "580,000원", "-0.8%", ChangeDirection.DOWN),
        StockItem(5004, 4, "SK하이닉스", "", "135,000원", "+0.5%", ChangeDirection.UP),
        StockItem(5005, 5, "포스코퓨처엠", "", "395,000원", "0.0%", ChangeDirection.FLAT),
    )

    val stockItems = when (selectedIndex) {
        0 -> byAmountList
        1 -> byVolumeList
        2 -> gainersList
        3 -> losersList
        4 -> popularList
        else -> emptyList()
    }

    Scaffold(
        topBar = { HomeTopBar() },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            RankingTitle()
            FilterTabs(
                selectedIndex = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )
            RankingList(stockItems) // 종목 리스트 출력
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {},
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
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
    val filters = listOf("거래대금", "거래량", "상승", "하락", "인기")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        filters.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }, // 클릭 시 외부 상태 업데이트
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                // 밑줄
                Box(
                    modifier = Modifier
                        .height(
                            if (index == selectedIndex) 2.dp
                            else 1.dp
                        )
                        .fillMaxWidth()
                        .background(
                            color = color,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

@Composable
fun RankingList(items: List<StockItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        items(items.size) { index ->
            StockRankItem(items[index])
        }
    }
}

@Composable
fun StockRankItem(item: StockItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 순위 텍스트
        Text(
            text = item.rank.toString(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            color = Navy
        )
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
