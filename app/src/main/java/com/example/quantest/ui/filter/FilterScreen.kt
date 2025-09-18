package com.example.quantest.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.ui.component.QuanTestTopBar
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.quantest.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.quantest.data.model.CompareOp
import com.example.quantest.data.model.Indicator
import com.example.quantest.ui.theme.Navy
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val tzSeoul: TimeZone = TimeZone.getTimeZone("Asia/Seoul")
private val sdfYmd: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
    timeZone = tzSeoul
}

fun todayYmd(): String {
    val cal = Calendar.getInstance(tzSeoul) // 서울 기준 now
    return sdfYmd.format(cal.time)
}

fun Long.toYmd(): String { // epoch millis → "yyyy-MM-dd"
    return sdfYmd.format(Date(this))
}

enum class FilterTab(val title: String) {
    DATE("날짜"),
    INDUSTRY("업종"),
    CHART("차트"),
    INDICATOR("지표")
}

@Composable
fun FilterScreen(
    viewModel: FilterViewModel = viewModel(),
    onSearchClick: () -> Unit,
    onOpenIndicatorSearch: () -> Unit,
    indicatorResultFlow: StateFlow<Indicator?>? = null
) {
    var selectedTab by rememberSaveable { mutableStateOf(FilterTab.DATE) }

    var dateYmd by rememberSaveable { mutableStateOf(todayYmd()) }

    // 최초 업종 로드 (한 번만)
    LaunchedEffect(Unit) { viewModel.loadSectors() }

    // 에러/결과 상태 수집
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val results by viewModel.results.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 스낵바
    LaunchedEffect(error) {
        if (error != null) snackbarHostState.showSnackbar(error!!)
    }

    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // 하단 고정 검색하기 버튼
        bottomBar = {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Button(
                    onClick = {
                        viewModel.search(
                            date = dateYmd,
                            chartFiltersFromUi = emptyList() // ChartFilterUi 쓰면 여기 전달
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // 시스템 하단바(제스처 바) 위로 띄우기
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = if (loading) "검색 중…" else "검색하기", fontSize = 18.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 상단 탭
            QuanTestTabRow(
                tabs = FilterTab.values(),
                selected = selectedTab,
                onSelected = { selectedTab = it },
                titleProvider = { it.title }
            )

            // 선택 칩
            SelectedChipsBar(viewModel = viewModel)

            // 탭별 화면
            when (selectedTab) {
                FilterTab.DATE -> DateFilterScreen(
                    onDateSelected = { millis ->
                        dateYmd = millis.toYmd()
                    }
                )
                FilterTab.INDUSTRY -> IndustryFilterScreen(viewModel)
                FilterTab.CHART -> ChartFilterScreen(viewModel)
                FilterTab.INDICATOR -> IndicatorFilterScreen(
                    viewModel,
                    onAddIndicatorClick = onOpenIndicatorSearch,
                    selectedIndicatorFlow = indicatorResultFlow
                )
            }
        }
    }
}

@Composable
private fun SelectedChipsBar(viewModel: FilterViewModel) {
    val sectors by viewModel.sectors.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val chartSelections by viewModel.chartSelections.collectAsState()
    val indicatorSelections by viewModel.indicatorSelections.collectAsState()
    val compareSelections by viewModel.compareSelections.collectAsState()

    if (selectedIds.isEmpty() &&
        chartSelections.isEmpty() &&
        indicatorSelections.isEmpty() &&
        compareSelections.isEmpty()
    ) {
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        return
    }

    val selectedList = remember(selectedIds, sectors) {
        selectedIds.mapNotNull { id -> sectors.find { it.sectorId == id } }
    }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 업종 칩
            items(selectedList, key = { it.sectorId }) { sector ->
                AssistChip(
                    onClick = { viewModel.removeSector(sector.sectorId) },
                    label = {
                        Text(
                            sector.sectorName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    border = BorderStroke(0.dp, Color.Transparent)
                )
            }

            // 차트 칩
            items(chartSelections.values.toList(), key = { it.type }) { sel ->
                val text = "${sel.type.label} ${sel.min ?: "~"}~${sel.max ?: ""}".replace("~~", "~")
                AssistChip(
                    onClick = { viewModel.removeChartSelection(sel.type) },
                    label = {
                        Text(
                            text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    border = BorderStroke(0.dp, Color.Transparent)
                )
            }

            // 지표 라인(값) 칩
            items(indicatorSelections.values.toList(), key = { "${it.indicatorId}_${it.lineId}" }) { sel ->
                val text = "${sel.indicatorName}/${sel.lineName} ${sel.min ?: "~"}~${sel.max ?: ""}".replace("~~", "~")
                AssistChip(
                    onClick = { viewModel.removeIndicatorRange(sel.indicatorId, sel.lineId) },
                    label = {
                        Text(
                            text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    border = BorderStroke(0.dp, Color.Transparent)
                )
            }

            // 라인 비교 칩
            items(compareSelections.values.flatten(), key = { "${it.indicatorId}_${it.rowId}" }) { sel ->
                val opText = when (sel.op) {
                    CompareOp.GREATER_THAN -> ">"
                    CompareOp.GREATER_THAN_OR_EQUAL -> ">="
                    CompareOp.EQUAL -> "="
                    CompareOp.LESS_THAN_OR_EQUAL -> "<="
                    CompareOp.LESS_THAN -> "<"
                }
                val text = "${sel.indicatorName}: ${sel.leftLineName} $opText ${sel.rightLineName}"
                AssistChip(
                    onClick = { viewModel.removeCompareSelection(sel.indicatorId, sel.rowId) },
                    label = {
                        Text(
                            text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    border = BorderStroke(0.dp, Color.Transparent)
                )
            }
        }
    }
}