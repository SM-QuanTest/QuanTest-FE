package com.example.quantest.ui.stockdetail

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
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import android.graphics.Color as AndroidColor
import android.graphics.Paint
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
import com.example.quantest.data.model.ChartData
import com.example.quantest.R
import com.example.quantest.util.formatPrice
import com.example.quantest.util.formatVolume
import com.example.quantest.util.ChartDateFormatter
import com.example.quantest.util.calculateMA
import com.example.quantest.util.chartDataToEntries
import com.example.quantest.util.chartDataToVolumeEntries
import com.example.quantest.util.formatAmountToEokWon
import com.example.quantest.util.formatChartDate
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
                fontSize = 30.sp
            )

            val priceChange = latest?.priceChange ?: 0
            val changePercent = latest?.chartChangePercent ?: 0.0
            val isRise = priceChange >= 0
            val changeText = if (latest != null) {
                val sign = if (isRise) "+" else "-"
                "$sign${formatPrice(abs(priceChange))} (${String.format("%.2f", abs(changePercent))}%)"
            } else {
                ""
            }

            Text(
                text = changeText,
                fontSize = 16.sp,
                color = if (isRise) Red else Blue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 탭
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label
                            )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ChartTabContent(viewModel.chartData)
            1 -> InfoTabContent(viewModel)
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
            horizontalArrangement = Arrangement.Center,           // 🔴 중앙 정렬
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

// 차트 탭
@Composable
fun ChartTabContent(data: List<ChartData>) {

    if (data.isEmpty()) return

    val candleEntries = chartDataToEntries(data)

    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.candle_chart, null) as FrameLayout
            val chart = view.findViewById<CombinedChart>(R.id.candleChart)

            // --- 캔들 데이터 ---
            val candleDataSet = CandleDataSet(chartDataToEntries(data), "일봉").apply {
                color = AndroidColor.GRAY
                shadowColor = AndroidColor.DKGRAY
                shadowWidth = 0.7f
                decreasingColor = Blue.toArgb()
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = Red.toArgb()
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = AndroidColor.BLUE
                setDrawValues(false)
                axisDependency = YAxis.AxisDependency.RIGHT // 캔들은 우측 Y축
            }
            val candleData = CandleData(candleDataSet)

            // --- 이동평균선 ---
            fun createMASet(maData: List<Entry>, label: String, colorInt: Int) = LineDataSet(maData, label).apply {
                color = colorInt
                lineWidth = 1.5f
                setDrawCircles(false)
                setDrawValues(false)
                axisDependency = YAxis.AxisDependency.RIGHT // MA도 우측 Y축
            }

            val lineData = LineData(
                createMASet(calculateMA(data, 5), "5", Green.toArgb()),
                createMASet(calculateMA(data, 20), "20", Magenta.toArgb()),
                createMASet(calculateMA(data, 60), "160", Orange.toArgb())
            )

            // --- 거래량 ---
            val volumeDataSet = BarDataSet(chartDataToVolumeEntries(data), "거래량").apply {
                color = Gray.toArgb()
                setDrawValues(false)
                axisDependency = YAxis.AxisDependency.LEFT // 거래량은 좌측 Y축
            }
            val barData = BarData(volumeDataSet)

            // --- CombinedData ---
            val combinedData = CombinedData().apply {
                setData(candleData)
                setData(lineData)
                setData(barData)
            }

            chart.data = combinedData

            // --- 차트 속성 ---
            chart.apply {
                // X축
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = ChartDateFormatter(data)
                }

                // 좌측 Y축 = 거래량
                chart.axisLeft.apply {
                    isEnabled = true
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when {
                                value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
                                value >= 1_000 -> String.format("%.1fK", value / 1_000)
                                else -> value.toInt().toString()
                            }
                        }
                    }
                }
                axisRight.isEnabled = true

                description.isEnabled = false
                legend.isEnabled = true
                legend.isWordWrapEnabled = true

                // 확대/축소, 드래그
                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setDrawGridBackground(false)
                isDoubleTapToZoomEnabled = true
                isDragDecelerationEnabled = true
                dragDecelerationFrictionCoef = 0.9f

                // 초기 화면
                setVisibleXRangeMaximum(45f)
                moveViewToX(data.size - 45f)
            }

            chart.invalidate()
            view
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    )
}

// 종목 정보 탭
@Composable
fun InfoTabContent(viewModel: StockDetailViewModel) {
    val data = viewModel.latestChartData

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "시세",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "${formatChartDate(data?.chartDate)} 기준",
                fontSize = 14.sp,
                color = StormGray40
            )
        }

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
        val labelTextStyle = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = StormGray40
        )

        val valueTextStyle = TextStyle(
            fontSize = 16.sp,
            color = StormGray80
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 컬럼 (시가/종가)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("시가", style = labelTextStyle)
                    Text(formatPrice(data?.chartOpen), style = valueTextStyle)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("종가", style = labelTextStyle)
                    Text(formatPrice(data?.chartClose), style = valueTextStyle)
                }
            }

            VerticalDivider(
                color = StormGray10,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight()
                    .width(1.dp)
            )

            // 오른쪽 컬럼 (거래량/거래대금)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("거래량", style = labelTextStyle)
                    Text(formatVolume(data?.chartVolume), style = valueTextStyle)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("거래대금", style = labelTextStyle)
                    Text(formatAmountToEokWon(data?.chartTurnover), style = valueTextStyle)
                }
            }
        }
    }
}