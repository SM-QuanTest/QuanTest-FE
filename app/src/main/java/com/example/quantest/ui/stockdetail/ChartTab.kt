package com.example.quantest.ui.stockdetail

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quantest.R
import com.example.quantest.data.model.ChartData
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