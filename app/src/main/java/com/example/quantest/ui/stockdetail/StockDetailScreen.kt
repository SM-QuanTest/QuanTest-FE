package com.example.quantest.ui.stockdetail

import android.R.id.tabs
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
    stockId: Int,
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
            StockDetailTab.CHART -> ChartTabContent(viewModel.chartData)
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

@Composable
fun ChartTabContent(data: List<ChartData>) {
    if (data.isEmpty()) return

    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.candle_with_volume, null) as LinearLayout

            val priceChart = view.findViewById<CombinedChart>(R.id.candleChart)
            val volumeChart = view.findViewById<BarChart>(R.id.volumeChart)

            // ===== 데이터 준비 =====
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
                axisDependency = YAxis.AxisDependency.RIGHT
            }
            val candleData = CandleData(candleDataSet)

            fun createMASet(maData: List<Entry>, label: String, colorInt: Int) =
                LineDataSet(maData, label).apply {
                    color = colorInt
                    lineWidth = 1.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.RIGHT
                }

            val lineData = LineData(
                createMASet(calculateMA(data, 5), "5", Green.toArgb()),
                createMASet(calculateMA(data, 20), "20", Magenta.toArgb()),
                createMASet(calculateMA(data, 60), "60", Orange.toArgb()) // <- 60 라벨 수정
            )

            val volumeDataSet = BarDataSet(chartDataToVolumeEntries(data), "거래량").apply {
                color = Gray.toArgb()
                setDrawValues(false)
                axisDependency = YAxis.AxisDependency.LEFT
            }
            val barData = BarData(volumeDataSet).apply {
                barWidth = 0.7f
            }

            // ===== 가격 차트(캔들+이평) =====
            val priceCombined = CombinedData().apply {
                setData(candleData)
                setData(lineData)
            }
            priceChart.data = priceCombined

            priceChart.apply {
                description.isEnabled = false

                legend.apply {
                    isEnabled = true
                    isWordWrapEnabled = true

                    // 상단으로 이동
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    orientation = Legend.LegendOrientation.HORIZONTAL

                    // 차트 내부/외부 위치
                    setDrawInside(false)
                }

                axisRight.isEnabled = true
                axisLeft.isEnabled = false // 가격은 우측축만 사용

                // 가격 차트의 X축 라벨은 겹치니 숨기고, 아래(거래량)에서만 표시
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = ChartDateFormatter(data)
                }

                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setDrawGridBackground(false)
                isDoubleTapToZoomEnabled = true
                isDragDecelerationEnabled = true
                dragDecelerationFrictionCoef = 0.9f

                // 여백
                setViewPortOffsets(16f, 16f, 96f, 8f)

                // 초기 가시범위 및 위치
                setVisibleXRangeMaximum(45f)
                moveViewToX(data.size - 45f)
            }

            // ===== 거래량 차트 =====
            volumeChart.data = barData
            volumeChart.apply {
                description.isEnabled = false
                legend.isEnabled = false

                // 우측축만 사용
                axisLeft.isEnabled = false
                axisRight.apply {
                    isEnabled = true
                    setDrawGridLines(false)
                    setLabelCount(3, true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when {
                                value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
                                value >= 1_000 -> String.format("%.1fK", value / 1_000f)
                                else -> value.toInt().toString()
                            }
                        }
                    }
                    setAxisMinimum(0f)
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawLabels(true)
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = ChartDateFormatter(data)
                    setAvoidFirstLastClipping(true)
                }

                // 여백
                setViewPortOffsets(16f, 0f, 96f, 48f)

                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setDrawGridBackground(false)
                isDoubleTapToZoomEnabled = true

                setVisibleXRangeMaximum(45f)
                moveViewToX(data.size - 45f)
            }

            // ===== 스케일/스크롤 동기화 =====
            fun syncMatrix(src: BarLineChartBase<*>, dst: BarLineChartBase<*>) {
                val newMatrix = android.graphics.Matrix(src.viewPortHandler.matrixTouch)
                dst.viewPortHandler.refresh(newMatrix, dst, true)
                // X축 최소/최대도 동일하게
                dst.xAxis.axisMinimum = src.xAxis.axisMinimum
                dst.xAxis.axisMaximum = src.xAxis.axisMaximum
            }

            val priceGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) = syncMatrix(priceChart, volumeChart)
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = syncMatrix(priceChart, volumeChart)
            }

            val volumeGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) = syncMatrix(volumeChart, priceChart)
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = syncMatrix(volumeChart, priceChart)
            }

            priceChart.onChartGestureListener = priceGestureListener
            volumeChart.onChartGestureListener = volumeGestureListener

            // 최초 한 번 동기화
            syncMatrix(priceChart, volumeChart)

            priceChart.invalidate()
            volumeChart.invalidate()
            view
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    )
}

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