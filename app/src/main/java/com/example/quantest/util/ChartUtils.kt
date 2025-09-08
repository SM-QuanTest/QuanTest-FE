package com.example.quantest.util

import com.example.quantest.data.model.ChartData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

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

// 이동평균 계산 함수
fun calculateMA(data: List<ChartData>, period: Int): List<Entry> {
    val result = mutableListOf<Entry>()
    for (i in period - 1 until data.size) {
        var sum = 0f
        for (j in 0 until period) {
            sum += data[i - j].chartClose.toFloat()
        }
        val avg = sum / period
        result.add(Entry(i.toFloat(), avg))
    }
    return result
}

// 거래량 데이터셋 만들기
fun chartDataToVolumeEntries(data: List<ChartData>): List<BarEntry> {
    return data.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.chartVolume.toFloat())
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

// 거래량 포맷터
class VolumeValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return when {
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000) // 백만 단위
            value >= 1_000 -> String.format("%.1fK", value / 1_000)         // 천 단위
            else -> value.toInt().toString()
        }
    }
}

// 거래대금 포맷터
fun formatAmountToEokWon(amount: Long?): String {
    if (amount == null) return "-"
    val eok = amount / 100_000_000        // 1억 단위
    return "%,d억원".format(eok)
}


fun formatChartDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val date = parser.parse(dateStr)

        val formatter = SimpleDateFormat("M월 d일", Locale.KOREA)
        formatter.format(date!!)
    } catch (e: Exception) {
        "" // 포맷 실패 시 빈 문자열
    }
}