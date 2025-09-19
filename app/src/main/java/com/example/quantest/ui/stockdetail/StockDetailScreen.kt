package com.example.quantest.ui.stockdetail

import android.util.Log
import android.view.LayoutInflater
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color.Companion.Gray
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.data.Entry
import androidx.compose.ui.graphics.toArgb
import com.example.quantest.ui.theme.Green
import com.example.quantest.ui.theme.Red
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Orange
import com.example.quantest.ui.theme.Magenta
import com.example.quantest.ui.theme.StormGray40
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import androidx.compose.ui.Alignment
import com.example.quantest.ui.theme.Navy
import com.example.quantest.ui.theme.StormGray10
import com.example.quantest.ui.theme.StormGray80
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.data.model.ChartData
import com.example.quantest.R
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.util.formatPrice
import com.example.quantest.util.formatVolume
import com.example.quantest.util.ChartDateFormatter
import com.example.quantest.util.calculateMA
import com.example.quantest.util.chartDataToEntries
import com.example.quantest.util.chartDataToVolumeEntries
import com.example.quantest.util.formatAmountToEokWon
import com.example.quantest.util.formatChartDate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlin.math.abs

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

enum class StockDetailTab(val title: String) {
    CHART("차트"),
    INFO("종목정보")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel = viewModel(),
    stockId: Long,
    onBackClick: () -> Unit,
    onDetailClick: () -> Unit,
    onBuyClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(StockDetailTab.CHART) }

    LaunchedEffect(stockId) {
        Log.d("StockDetailScreen", "Received stockId: $stockId")
        viewModel.fetchChartData(stockId)
    }

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
        StockBasicInfo(viewModel = viewModel)

        Spacer(modifier = Modifier.height(8.dp))

        // 상단 탭
        QuanTestTabRow(
            tabs = StockDetailTab.values(),
            selected = selectedTab,
            onSelected = { selectedTab = it },
            titleProvider = { it.title }
        )

        when (selectedTab) {
            StockDetailTab.CHART -> ChartTabContent(
                data = viewModel.chartData
            )
            StockDetailTab.INFO  -> InfoTabContent(viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            color = StormGray10,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 일별 시세 보기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDetailClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "일별 시세 보기",
                    fontWeight = FontWeight.Medium,
                    color = StormGray40
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = "더보기",
                    tint = StormGray40,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(
            color = StormGray10,
            thickness = 12.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // 구매하기 버튼
        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Navy,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "구매하기",
                fontSize = 18.sp
            )
        }
    }
}

// 종목 기본 정보
@Composable
fun StockBasicInfo(viewModel: StockDetailViewModel) {
    val latest = viewModel.latestChartData

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = latest?.stockName ?: "-",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = latest?.chartClose?.let { formatPrice(it) } ?: "-원",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )

        val priceChange = latest?.priceChange ?: 0
        val changePercent = latest?.chartChangePercent ?: 0.0
        val isRise = priceChange >= 0
        val changeText = if (latest != null) {
            val sign = if (isRise) "+" else "-"
            "$sign${formatPrice(abs(priceChange))} (${String.format("%.2f", abs(changePercent))}%)"
        } else ""

        Text(
            text = changeText,
            fontSize = 16.sp,
            color = if (isRise) Red else Blue
        )
    }
}
