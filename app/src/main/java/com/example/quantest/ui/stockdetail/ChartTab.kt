package com.example.quantest.ui.stockdetail

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quantest.R
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Green
import com.example.quantest.ui.theme.Magenta
import com.example.quantest.ui.theme.Orange
import com.example.quantest.ui.theme.Red
import com.example.quantest.util.ChartDateFormatter
import com.example.quantest.util.calculateMA
import com.example.quantest.util.chartDataToEntries
import com.example.quantest.util.chartDataToVolumeEntries
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

private const val VISIBLE_COUNT = 60f
private const val TRIGGER_COOLDOWN_MS = 250L
private const val TRIGGER_RATIO = 0.10f   // 왼쪽 10%에서 트리거
private const val RESET_RATIO   = 0.12f   // 12% 이상 벗어나면 재무장

@Composable
fun ChartTabContent(
    viewModel: StockDetailViewModel,
    onLoadMore: () -> Unit,
    focusDate: String?,
) {
    val data = viewModel.chartData
    if (data.isEmpty()) return

    val context = LocalContext.current
    var chart by remember { mutableStateOf<CandleStickChart?>(null) }
    var priceRef by remember { mutableStateOf<CombinedChart?>(null) }
    var volRef   by remember { mutableStateOf<BarChart?>(null) }

    // 로컬 상태
    var lastSize by remember { mutableStateOf(0) }
    var loadRequested by remember { mutableStateOf(false) }

    var gestureActive by remember { mutableStateOf(false) }
    var armed by remember { mutableStateOf(true) } // 트리거 무장 상태
    var lastTriggerMs by remember { mutableStateOf(0L) }

    // 이동 방향 체크용
    var lastLowest by remember { mutableStateOf(Float.NaN) }

    // 날짜 포커스 (날짜/데이터/차트 레퍼런스 변화에 반응)
    LaunchedEffect(focusDate, data, priceRef, volRef) {
        val date = focusDate ?: return@LaunchedEffect
        val i = data.indexOfFirst { it.chartDate == date }
        val p = priceRef ?: return@LaunchedEffect
        if (i >= 0) {
            focusAtIndex(p, volRef, i)
        } else {
            // (옵션) 현재 페이지에 없으면 더 로드 후 재시도 로직을 넣고 싶으면 여기서 호출
            // onLoadMore()
        }
    }

    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.candle_with_volume, null) as LinearLayout

            val priceChart = view.findViewById<CombinedChart>(R.id.candleChart)
            val volumeChart = view.findViewById<BarChart>(R.id.volumeChart)

            // 외부에서 접근할 수 있도록 보관
            priceRef = priceChart
            volRef   = volumeChart

            fun applyVisibleRange() {
                //priceChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
                priceChart.setVisibleXRangeMaximum(VISIBLE_COUNT)
                //volumeChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
                volumeChart.setVisibleXRangeMaximum(VISIBLE_COUNT)
            }

            fun bindData() {
                // ===== 데이터 준비 =====
                val candleDataSet = CandleDataSet(chartDataToEntries(data), "일봉").apply {
                    color = Color.GRAY
                    shadowColor = Color.DKGRAY
                    shadowWidth = 0.7f
                    decreasingColor = Blue.toArgb()
                    decreasingPaintStyle = Paint.Style.FILL
                    increasingColor = Red.toArgb()
                    increasingPaintStyle = Paint.Style.FILL
                    neutralColor = Color.BLUE
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
                    createMASet(calculateMA(data, 60), "60", Orange.toArgb())
                )

                val volumeDataSet = BarDataSet(chartDataToVolumeEntries(data), "거래량").apply {
                    color = Gray.toArgb()
                    setDrawValues(false)
                }
                val barData = BarData(volumeDataSet).apply { barWidth = 0.7f }

                // ===== 차트 세팅 =====
                val priceCombined = CombinedData().apply {
                    setData(candleData)
                    setData(lineData)
                }
                priceChart.data = priceCombined
                volumeChart.data = barData

                priceChart.apply {
                    description.isEnabled = false
                    legend.apply {
                        isEnabled = true
                        isWordWrapEnabled = true
                        verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                        orientation = Legend.LegendOrientation.HORIZONTAL
                        setDrawInside(false)
                    }
                    axisRight.isEnabled = true
                    axisLeft.isEnabled = false
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
                    setViewPortOffsets(16f, 16f, 96f, 8f)
                    applyVisibleRange()
                }

                volumeChart.apply {
                    description.isEnabled = false
                    legend.isEnabled = false
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
                        axisMinimum = 0f
                    }
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawLabels(true)
                        setDrawGridLines(false)
                        granularity = 1f
                        valueFormatter = ChartDateFormatter(data)
                        setAvoidFirstLastClipping(true)
                    }
                    setViewPortOffsets(16f, 0f, 96f, 48f)
                    setPinchZoom(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setDrawGridBackground(false)
                    isDoubleTapToZoomEnabled = true
                    applyVisibleRange()
                }

                // 최초 위치는 가장 최근(오른쪽)으로
                if (lastSize == 0) {
                    val right = (data.size - VISIBLE_COUNT).coerceAtLeast(0f)
                    priceChart.moveViewToX(right)
                    volumeChart.moveViewToX(right)
                }
            }

            fun syncMatrix(src: BarLineChartBase<*>, dst: BarLineChartBase<*>) {
                val newMatrix = android.graphics.Matrix(src.viewPortHandler.matrixTouch)
                dst.viewPortHandler.refresh(newMatrix, dst, true)
                dst.xAxis.axisMinimum = src.xAxis.axisMinimum
                dst.xAxis.axisMaximum = src.xAxis.axisMaximum
            }

            bindData()

            // 트리거 함수 (방향 체크 + 라벨 사용)
            val askMoreIfNeeded: (BarLineChartBase<*>) -> Unit = askMore@{ chart ->
                if (!gestureActive) return@askMore

                // 이동 방향: 왼쪽으로 가는 중일 때만 검사
                val curLowest = chart.lowestVisibleX
                val movingLeft = !lastLowest.isNaN() && curLowest < lastLowest - 0.01f
                lastLowest = curLowest
                if (!movingLeft) return@askMore

                val window   = chart.highestVisibleX - chart.lowestVisibleX
                val leftEdge = chart.lowestVisibleX
                val minX     = chart.xChartMin

                val inTriggerZone  = leftEdge <= minX + window * TRIGGER_RATIO
                val outOfResetZone = leftEdge >  minX + window * RESET_RATIO
                val now = android.os.SystemClock.uptimeMillis()

                // 오른쪽으로 약간만 벗어나면 재무장
                if (outOfResetZone && !armed) {
                    armed = true
                }

                if (inTriggerZone && armed && !loadRequested && now - lastTriggerMs > TRIGGER_COOLDOWN_MS) {
                    loadRequested = true
                    armed = false
                    lastTriggerMs = now
                    Log.d("ChartScroll", "trigger loadMore() (armed->disarmed)")
                    onLoadMore()
                }
            }

            val priceGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, g: ChartTouchListener.ChartGesture?) { gestureActive = true }
                override fun onChartGestureEnd(me: MotionEvent?, g: ChartTouchListener.ChartGesture?) { gestureActive = false }
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, vX: Float, vY: Float) {}
                override fun onChartScale(me: MotionEvent?, sx: Float, sy: Float) {
                    syncMatrix(priceChart, volumeChart)
                    askMoreIfNeeded(priceChart)
                }
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    syncMatrix(priceChart, volumeChart)
                    askMoreIfNeeded(priceChart)
                }
            }

            val volumeGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, g: ChartTouchListener.ChartGesture?) { gestureActive = true }
                override fun onChartGestureEnd(me: MotionEvent?, g: ChartTouchListener.ChartGesture?) { gestureActive = false }
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, vX: Float, vY: Float) {}
                override fun onChartScale(me: MotionEvent?, sx: Float, sy: Float) {
                    syncMatrix(volumeChart, priceChart)
                    askMoreIfNeeded(volumeChart)
                }
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    syncMatrix(volumeChart, priceChart)
                    askMoreIfNeeded(volumeChart)
                }
            }

            priceChart.onChartGestureListener = priceGestureListener
            volumeChart.onChartGestureListener = volumeGestureListener

            priceChart.post {
                val right = (data.size - VISIBLE_COUNT).coerceAtLeast(0f)
                priceChart.setPinchZoom(true)
                volumeChart.setPinchZoom(true)
                // 보이는 개수 고정 (초기 보장)
                //priceChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
                priceChart.setVisibleXRangeMaximum(VISIBLE_COUNT)
                //volumeChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
                volumeChart.setVisibleXRangeMaximum(VISIBLE_COUNT)
                priceChart.moveViewToX(right)
                volumeChart.moveViewToX(right)
            }

            // 최초 동기화 & 그리기
            syncMatrix(priceChart, volumeChart)
            priceChart.invalidate()
            volumeChart.invalidate()

            view
        },
        // 데이터 갱신 시(더 불러온 뒤) 뷰포트 보존 + 재무장
        update = { view ->
            val priceChart = view.findViewById<CombinedChart>(R.id.candleChart)
            val volumeChart = view.findViewById<BarChart>(R.id.volumeChart)

            // 1) 현재 뷰포트 행렬 백업 (스케일/위치 유지)
            val priceMatrix = android.graphics.Matrix(priceChart.viewPortHandler.matrixTouch)
            val volumeMatrix = android.graphics.Matrix(volumeChart.viewPortHandler.matrixTouch)

            val prevLow = priceChart.lowestVisibleX
            val prevHigh = priceChart.highestVisibleX
            val prevSize = lastSize
            val added = (data.size - prevSize).coerceAtLeast(0)

            // 새 formatter에 최신 data 전달되도록 전체 바인딩 재생성
            val candleDataSet = CandleDataSet(chartDataToEntries(data), "일봉").apply {
                color = Color.GRAY
                shadowColor = Color.DKGRAY
                shadowWidth = 0.7f
                decreasingColor = Blue.toArgb()
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = Red.toArgb()
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = Color.BLUE
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
                createMASet(calculateMA(data, 60), "60", Orange.toArgb())
            )
            val priceCombined = CombinedData().apply {
                setData(candleData)
                setData(lineData)
            }
            priceChart.data = priceCombined

            val volumeDataSet = BarDataSet(chartDataToVolumeEntries(data), "거래량").apply {
                color = Gray.toArgb()
                setDrawValues(false)
            }
            volumeChart.data = BarData(volumeDataSet).apply { barWidth = 0.7f }

            priceChart.xAxis.valueFormatter = ChartDateFormatter(data)
            volumeChart.xAxis.valueFormatter = ChartDateFormatter(data)

            // 2) 뷰포트 행렬 복구
            priceChart.viewPortHandler.refresh(priceMatrix, priceChart, false)
            volumeChart.viewPortHandler.refresh(volumeMatrix, volumeChart, false)

            // 가시폭 고정 재보장
            //priceChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
            priceChart.setVisibleXRangeMaximum(VISIBLE_COUNT)
            //volumeChart.setVisibleXRangeMinimum(VISIBLE_COUNT)
            volumeChart.setVisibleXRangeMaximum(VISIBLE_COUNT)

            // 뷰포트 보존: 왼쪽(과거)으로 prepend된 만큼 X를 우측으로 보정
            if (added > 0 && prevSize > 0) {
                val newLow = (prevLow + added).coerceAtMost(data.lastIndex.toFloat())
                priceChart.moveViewToX(newLow)
                volumeChart.moveViewToX(newLow)

                // 다음 로드 가능하도록 즉시 해제 + 재무장
                loadRequested = false
                armed = true
            }

            lastSize = data.size

            priceChart.notifyDataSetChanged()
            volumeChart.notifyDataSetChanged()

            val window = priceChart.highestVisibleX - priceChart.lowestVisibleX
            val atFarLeft = priceChart.lowestVisibleX <= priceChart.xChartMin + window * TRIGGER_RATIO

            // 추가 데이터가 없었고 여전히 좌측이면(실수로 막혔을 때) 재시도 허용
            if (added == 0 && atFarLeft) {
                Log.d("ChartScroll", "no added data; allow retry")
                loadRequested = false
                armed = true
            }

            priceChart.invalidate()
            volumeChart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    )
}

private fun focusAtIndex(
    price: CombinedChart,
    volume: BarChart?,
    index: Int
) {
    val x = index.toFloat()
    price.centerViewToAnimated(x, 0f, YAxis.AxisDependency.LEFT, 450)

    // 하이라이트
    //price.highlightValue(x, /*dataSetIndex=*/0, /*callListener=*/true)

    price.moveViewToX(x)
    volume?.moveViewToX(x)
}