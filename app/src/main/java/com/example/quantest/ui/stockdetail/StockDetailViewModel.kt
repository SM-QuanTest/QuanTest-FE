package com.example.quantest.ui.stockdetail

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.data.model.ChartData
import com.example.quantest.data.model.LatestChartData
import com.example.quantest.data.model.PatternRecord
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StockDetailViewModel : ViewModel() {

    private val _chartData = mutableStateOf<List<ChartData>>(emptyList())
    val chartData: List<ChartData> get() = _chartData.value

    private val _latestChartData = mutableStateOf<LatestChartData?>(null)
    val latestChartData: LatestChartData? get() = _latestChartData.value

    private var nextCursor: String? = null
    private var hasNext: Boolean = true
    private var isLoading = false
    private var loadRetryGuard = false // 무한 재시도 방지

    fun fetchChartData(stockId: Long) {
        Log.d("ChartFetch", ">>> fetchChartData() START for stockId=$stockId")
        // 초기화
        nextCursor = null
        hasNext = true

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.stockApi.getChartPage(
                    stockId = stockId,
                    cursorDate = null,
                    limit = null
                )
                Log.d("ChartFetch", "HTTP ${response.code()} raw=${response.raw()}")
                Log.d("ChartFetch", "body=${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val page = response.body()!!.data!!
                    Log.d("ChartFetch", "page.contents.size=${page.contents.size}, nextCursor=${page.nextCursor}, hasNext=${page.hasNext}")

                    _chartData.value = page.contents.sortedBy { it.chartDate }
                    nextCursor = page.nextCursor
                    hasNext = page.hasNext

                    val first = _chartData.value.firstOrNull()?.chartDate
                    val last = _chartData.value.lastOrNull()?.chartDate
                    Log.d("ChartFetch", "Fetched ${chartData.size} items, dateRange=$first..$last")
                } else {
                    Log.w("ChartFetch", "API error: ${response.message()}, code=${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChartFetch", "Exception: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
                Log.d("ChartFetch", "<<< fetchChartData() END isLoading=$isLoading")
            }
        }

        fetchLatestChartData(stockId)
    }

    fun loadMore(stockId: Long) {
        Log.d("ChartMore", ">>> loadMore() enter: hasNext=$hasNext, isLoading=$isLoading, cursor=$nextCursor")

        if (!hasNext || isLoading) {
            Log.d("ChartMore", "BLOCKED: hasNext=$hasNext, isLoading=$isLoading, cursor=$nextCursor")
            return
        }

        val serverCursor = nextCursor
        if (serverCursor == null) {
            Log.d("ChartMore", "BLOCKED: cursor is null")
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                // 1차: 서버 커서 그대로 요청
                Log.d("ChartMore", "REQUEST(1) stockId=$stockId, cursor=$serverCursor")
                val r1 = RetrofitClient.stockApi.getChartPage(
                    stockId = stockId,
                    cursorDate = serverCursor,
                    limit = null
                )

                if (!r1.isSuccessful || r1.body()?.success != true) {
                    Log.w("ChartMore", "API(1) fail: code=${r1.code()}, msg=${r1.message()}, err=${r1.errorBody()?.string()}")
                    return@launch
                }

                val p1 = r1.body()!!.data!!
                val before1 = _chartData.value.size
                val merged1 = (_chartData.value + p1.contents)
                    .distinctBy { it.chartId }
                    .sortedBy { it.chartDate }
                val after1 = merged1.size
                val added1 = after1 - before1

                Log.d("ChartMore", "RESP(1) count=${p1.contents.size}, serverNext=${p1.nextCursor}, serverHasNext=${p1.hasNext}")
                Log.d("ChartMore", "MERGE(1) before=$before1 -> after=$after1 (added=$added1)")

                // 상태 갱신
                _chartData.value = merged1
                nextCursor = p1.nextCursor
                hasNext = p1.hasNext

                // === 중복 페이지(added=0) 혹은 nextCursor 정체 시: fallback 커서로 한 번 더 시도 ===
                val stuck = (added1 == 0) || (p1.nextCursor == serverCursor)
                if (stuck && !loadRetryGuard) {
                    loadRetryGuard = true
                    val fallback = oldestDateMinusOne(_chartData.value) // yyyy-MM-dd - 1일 (API24 호환)
                    Log.d("ChartMore", "STUCK: retry with fallbackCursor=$fallback (from oldest-1day)")

                    if (fallback != null && fallback != serverCursor) {
                        val r2 = RetrofitClient.stockApi.getChartPage(
                            stockId = stockId,
                            cursorDate = fallback,
                            limit = null
                        )
                        if (r2.isSuccessful && r2.body()?.success == true) {
                            val p2 = r2.body()!!.data!!
                            val before2 = _chartData.value.size
                            val merged2 = (_chartData.value + p2.contents)
                                .distinctBy { it.chartId }
                                .sortedBy { it.chartDate }
                            val after2 = merged2.size
                            val added2 = after2 - before2

                            _chartData.value = merged2
                            nextCursor = p2.nextCursor
                            hasNext = p2.hasNext

                            Log.d("ChartMore", "RESP(2) count=${p2.contents.size}, serverNext=${p2.nextCursor}, serverHasNext=${p2.hasNext}")
                            Log.d("ChartMore", "MERGE(2) before=$before2 -> after=$after2 (added=$added2)")

                            // 여전히 추가분이 없다면 더 내려갈 수 없다고 보고 hasNext=false로 차단
                            if (added2 == 0) {
                                Log.w("ChartMore", "STILL STUCK: no added after fallback. Forcing hasNext=false to stop loop.")
                                hasNext = false
                            }
                        } else {
                            Log.w("ChartMore", "API(2) fail: code=${r2.code()}, msg=${r2.message()}, err=${r2.errorBody()?.string()}")
                        }
                    } else {
                        Log.w("ChartMore", "No valid fallback cursor; stop further loads.")
                        hasNext = false
                    }
                }
            } catch (e: Exception) {
                Log.e("ChartMore", "Exception: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
                loadRetryGuard = false
                Log.d("ChartMore", "<<< loadMore() END isLoading=$isLoading")
            }
        }
    }

    private fun fetchLatestChartData(stockId: Long) {
        Log.d("LatestChart", ">>> fetchLatestChartData() stockId=$stockId")
        viewModelScope.launch {
            try {
                val response = RetrofitClient.stockApi.getLatestChartData(stockId)
                Log.d("LatestChart", "HTTP ${response.code()}, body=${response.body()}")
                if (response.isSuccessful) {
                    _latestChartData.value = response.body()?.data
                    Log.d("LatestChart", "Fetched latest=$latestChartData")
                } else {
                    Log.e("LatestChart", "실패: code=${response.code()}, msg=${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("LatestChart", "에러: ${e.message}", e)
            } finally {
                Log.d("LatestChart", "<<< fetchLatestChartData() END")
            }
        }
    }

    // ----------------------- Pattern Records -----------------------
    private val _patternRecords = mutableStateOf<List<PatternRecord>>(emptyList())
    val patternRecords: List<PatternRecord> get() = _patternRecords.value

    private var patternNextCursor: String? = null
    private var patternHasNext: Boolean = true
    private var patternIsLoading = false

    fun fetchPatternRecords(stockId: Long, limit: Int? = null) {
        Log.d("PatternFetch", ">>> fetchPatternRecords() START stockId=$stockId")
        // 초기화
        patternNextCursor = null
        patternHasNext = true

        viewModelScope.launch {
            patternIsLoading = true
            try {
                val res = RetrofitClient.stockApi.getPatternRecords(
                    stockId = stockId,
                    limit = limit,
                    cursorDate = null
                )
                Log.d("PatternFetch", "HTTP ${res.code()} body=${res.body()}")

                if (res.isSuccessful && res.body()?.success == true) {
                    val page = res.body()!!.data!!
                    _patternRecords.value = page.contents.sortedByDescending { it.patternRecordDate }
                    patternNextCursor = page.nextCursor
                    patternHasNext = page.hasNext
                    Log.d("PatternFetch", "loaded=${_patternRecords.value.size} next=$patternNextCursor hasNext=$patternHasNext")
                } else {
                    Log.w("PatternFetch", "API error: ${res.message()}, code=${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("PatternFetch", "Exception: ${e.localizedMessage}", e)
            } finally {
                patternIsLoading = false
                Log.d("PatternFetch", "<<< fetchPatternRecords() END")
            }
        }
    }

    fun loadMorePatterns(stockId: Long, limit: Int? = null) {
        Log.d("PatternMore", ">>> loadMorePatterns() hasNext=$patternHasNext isLoading=$patternIsLoading cursor=$patternNextCursor")
        if (!patternHasNext || patternIsLoading) return

        val cursor = patternNextCursor ?: return
        viewModelScope.launch {
            patternIsLoading = true
            try {
                val res = RetrofitClient.stockApi.getPatternRecords(
                    stockId = stockId,
                    limit = limit,
                    cursorDate = cursor
                )
                if (!res.isSuccessful || res.body()?.success != true) {
                    Log.w("PatternMore", "API fail code=${res.code()} msg=${res.message()}")
                    return@launch
                }

                val page = res.body()!!.data!!
                val before = _patternRecords.value.size
                val merged = (_patternRecords.value + page.contents)
                    .distinctBy { it.patternRecordId }
                    .sortedByDescending { it.patternRecordDate }
                _patternRecords.value = merged
                patternNextCursor = page.nextCursor
                patternHasNext = page.hasNext

                Log.d("PatternMore", "added=${merged.size - before} next=$patternNextCursor hasNext=$patternHasNext")
            } catch (e: Exception) {
                Log.e("PatternMore", "Exception: ${e.localizedMessage}", e)
            } finally {
                patternIsLoading = false
                Log.d("PatternMore", "<<< loadMorePatterns() END")
            }
        }
    }

    fun isPatternLoading(): Boolean = patternIsLoading
    fun hasMorePatterns(): Boolean = patternHasNext

    // 이동
    private val _chartFocusDate = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val chartFocusDate = _chartFocusDate.asSharedFlow()

    fun focusChartOn(date: String) {
        _chartFocusDate.tryEmit(date)   // yyyy-MM-dd
    }
}

// API 24 호환(yyyy-MM-dd 하루 빼기)
private fun oldestDateMinusOne(data: List<ChartData>): String? {
    if (data.isEmpty()) return null
    val oldest = data.minByOrNull { it.chartDate }?.chartDate ?: return null // "yyyy-MM-dd"
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREAN)
        val d = sdf.parse(oldest) ?: return null
        val cal = java.util.Calendar.getInstance()
        cal.time = d
        cal.add(java.util.Calendar.DATE, -1)
        sdf.format(cal.time)
    } catch (_: Exception) {
        null
    }
}