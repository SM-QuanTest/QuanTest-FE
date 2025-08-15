package com.example.quantest

import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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
    val viewModel = remember { StockDetailViewModel() }
    LaunchedEffect(stockId) {
        Log.d("StockDetailScreen", "Received stockId: $stockId")
        viewModel.fetchChartData(stockId)
    }


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
                fontSize = 28.sp
            )

            val priceChange = latest?.priceChange ?: 0
            val changePercent = latest?.chartChangePercentage ?: 0.0
            val isRise = priceChange >= 0
            val changeText = if (latest != null) {
                val sign = if (isRise) "+" else "-"
                "$sign${formatPrice(kotlin.math.abs(priceChange))} (${String.format("%.2f", kotlin.math.abs(changePercent))}%)"
            } else {
                ""
            }

            Text(
                text = changeText,
                fontSize = 14.sp,
                color = if (isRise) Color.Red else Color.Blue
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
            0 -> ChartTabContent(viewModel.chartData)
            1 -> InfoTabContent(viewModel)
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

// 차트 데이터 → CandleEntry 변환
fun chartDataToEntries(data: List<ChartData>): List<CandleEntry> {
    return data.mapIndexed { index, item ->
        CandleEntry(
            index.toFloat(),
            item.chartHigh.toFloat(),
            item.chartLow.toFloat(),
            item.chartOpen.toFloat(),
            item.chartClose.toFloat()
        )
    }
}

// 날짜 포맷터 (yyyy-MM-dd → MM/dd)
class ChartDateFormatter(
    private val data: List<ChartData>
) : ValueFormatter() {
    private val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    private val sdfOutput = SimpleDateFormat("MM/dd", Locale.KOREA)

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index in data.indices) {
            try {
                val date = sdfInput.parse(data[index].chartDate)
                date?.let { sdfOutput.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }
}

// 차트 탭
@Composable
fun ChartTabContent(data: List<ChartData>) {

    if (data.isEmpty()) return

    val entries = chartDataToEntries(data)

    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.candle_chart, null) as FrameLayout
            val chart = view.findViewById<CandleStickChart>(R.id.candleChart)
            val dataSet = CandleDataSet(entries, "일봉").apply {
                color = AndroidColor.GRAY
                shadowColor = AndroidColor.DKGRAY
                shadowWidth = 0.7f
                decreasingColor = AndroidColor.RED
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = AndroidColor.BLUE
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = AndroidColor.BLUE
                setDrawValues(false)
            }
            chart.data = CandleData(dataSet)

            chart.apply {
                // X축
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f // 1일 단위
                    setDrawGridLines(false)
                    valueFormatter = ChartDateFormatter(data)
                }

                // 초기 확대 상태
                setVisibleXRangeMaximum(60f) // X축에 한 번에 보일 최대 개수
                moveViewToX(data.size - 60f) // 마지막 데이터 쪽으로 이동

                axisLeft.isEnabled = false
                axisRight.isEnabled = true

                description.isEnabled = false
                legend.isEnabled = false

                // 확대/축소, 드래그 활성화
                setPinchZoom(true)        // 핀치로 X/Y 동시에 확대
                isDragEnabled = true      // 드래그 가능
                setScaleEnabled(true)     // 확대/축소 가능
                setDrawGridBackground(false)

                // 더블탭 확대
                isDoubleTapToZoomEnabled = true

                // 드래그 후 관성 스크롤
                isDragDecelerationEnabled = true
                dragDecelerationFrictionCoef = 0.9f
            }

            chart.invalidate()
            view

        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

// 종목 정보 탭
@Composable
fun InfoTabContent(viewModel: StockDetailViewModel) {
    val data = viewModel.latestChartData

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "시세", fontWeight = FontWeight.Bold)
        Text(text = "${data?.chartDate ?: ""} 기준", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // 1년 최저가 ~ 최고가 슬라이더
        // TODO: 실제 데이터 연동
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("1년 최저가\n49,900원", fontSize = 12.sp)
//            Text("1년 최고가\n88,800원", fontSize = 12.sp)
//        }

        Spacer(modifier = Modifier.height(12.dp))

        // 시가, 종가, 거래량, 거래대금
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("시가", fontWeight = FontWeight.SemiBold)
                Text(formatPrice(data?.chartOpen))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("종가", fontWeight = FontWeight.SemiBold)
                Text(formatPrice(data?.chartClose))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("거래량", fontWeight = FontWeight.SemiBold)
                Text(formatVolume(data?.chartVolume))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("거래대금", fontWeight = FontWeight.SemiBold)
                Text(formatPrice(data?.chartTurnover))
            }
        }
    }
}
