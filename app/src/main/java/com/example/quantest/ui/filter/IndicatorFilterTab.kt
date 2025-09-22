package com.example.quantest.ui.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.quantest.ui.component.QuanTestOutlinedButton
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.quantest.R
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.quantest.data.model.CompareOp
import com.example.quantest.data.model.Indicator
import com.example.quantest.data.model.IndicatorLine
import com.example.quantest.data.model.LineCompareState
import com.example.quantest.data.model.LineComparison
import com.example.quantest.ui.theme.Navy
import com.example.quantest.ui.theme.StormGray60
import com.example.quantest.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorFilterTab(
    viewModel: FilterViewModel,
    onAddIndicatorClick: () -> Unit,
    selectedIndicatorFlow: StateFlow<Indicator?>?
) {
    // 새로 고른 "마지막 지표"를 받음
    val lastSelected by selectedIndicatorFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    // 들어오면 누적 추가
    LaunchedEffect(lastSelected?.indicatorId) {
        lastSelected?.let { viewModel.addIndicator(it) }
    }

    val selectedIndicators by viewModel.selectedIndicators.collectAsState()
    val linesByIndicator by viewModel.linesByIndicator.collectAsState()
    val selectedLineIdsByIndicator by viewModel.selectedLineIdsByIndicator.collectAsState()

    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val configs by viewModel.configs.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // 지표별 블록 렌더
        selectedIndicators.forEach { indicator ->
            val lines = linesByIndicator[indicator.indicatorId].orEmpty()
            val checked = selectedLineIdsByIndicator[indicator.indicatorId].orEmpty()

            // 지표 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = indicator.indicatorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // info 아이콘 + 툴팁
                Spacer(Modifier.width(8.dp))
                TooltipBox(
                    positionProvider = rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            if (configs.isEmpty()) {
                                Text("설정을 불러오는 중…")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    configs.forEach { config ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = config.indicatorConfigName,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = config.indicatorConfigValue,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    state = tooltipState
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.loadConfigs(indicator.indicatorId)
                                tooltipState.show()   // 아이콘 누르면 툴팁 열기
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "지표 설정 보기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.removeIndicator(indicator.indicatorId) }) {
                    Text("삭제")
                }
            }

            // 라인 체크박스
            lines.forEach { line ->
                val isChecked = checked.contains(line.indicatorLineId)
                var minText by remember { mutableStateOf("") }
                var maxText by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // 체크박스 Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.toggleLine(indicator.indicatorId, line.indicatorLineId) }
                    ) {
                        Checkbox(
                            checked = checked.contains(line.indicatorLineId),
                            onCheckedChange = { viewModel.toggleLine(indicator.indicatorId, line.indicatorLineId) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Navy,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = line.indicatorLineName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isChecked) Navy else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // 체크되었을 때만 입력칸 & 버튼 보이기
                    if (isChecked) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = minText,
                                onValueChange = { value -> minText = value.filter { it.isDigit() } },
                                placeholder = { Text("0") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = " - ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            OutlinedTextField(
                                value = maxText,
                                onValueChange = { value -> maxText = value.filter { it.isDigit() } },
                                placeholder = { Text("최대") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(Modifier.width(8.dp))

                            QuanTestOutlinedButton(
                                onClick = {
                                    val min = minText.toIntOrNull()
                                    val max = maxText.toIntOrNull()
                                    viewModel.applyIndicatorRange(
                                        indicator.indicatorId,
                                        indicator.indicatorName,
                                        line.indicatorLineId,
                                        line.indicatorLineName,
                                        min,
                                        max
                                    )
                                },
                                text = "적용"
                            )
                        }
                    }
                }
            }

            // 라인 비교
            CompareSection(
                indicatorId = indicator.indicatorId,
                lines = lines,
                viewModel = viewModel
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }

        // 지표 추가 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = onAddIndicatorClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ 지표 추가")
            }
        }
    }

}

@Composable
private fun CompareSection(
    indicatorId: Int,
    lines: List<IndicatorLine>,
    viewModel: FilterViewModel
) {
    val compareMap by viewModel.compareByIndicator.collectAsState()
    val state = compareMap[indicatorId] ?: LineCompareState()

    // 항상 보이게
    Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleLineCompare(indicatorId, !state.enabled) }
                .padding(vertical = 6.dp)
        ) {
            Checkbox(
                checked = state.enabled,
                onCheckedChange = { viewModel.toggleLineCompare(indicatorId, it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Navy,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "라인 비교" ,
                color = if (state.enabled) Navy else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (state.enabled) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (state.enabled) {
            Spacer(Modifier.height(6.dp))

            state.rows.forEach { row ->
                LineCompareRow(
                    lines = lines,
                    value = row,
                    onChangeLeft = { viewModel.updateCompareRow(indicatorId, row.id, left = it) },
                    onChangeOp = { viewModel.updateCompareRow(indicatorId, row.id, op = it) },
                    onChangeRight = { viewModel.updateCompareRow(indicatorId, row.id, right = it) },
                    onRemove = { viewModel.removeCompareRow(indicatorId, row.id) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.addCompareRow(indicatorId) },
                    shape = RoundedCornerShape(8.dp),
                    ) {
                    Text("+ 라인 비교 추가")
                }
                Button(
                    onClick = { viewModel.applyLineCompare(indicatorId) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StormGray60,
                        contentColor = White
                    )
                    ) {
                    Text("적용")
                }
            }
        }
    }
}

@Composable
private fun LineCompareRow(
    lines: List<IndicatorLine>,
    value: LineComparison,
    onChangeLeft: (Int?) -> Unit,
    onChangeOp: (CompareOp) -> Unit,
    onChangeRight: (Int?) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        LinePicker(
            label = "라인",
            lines = lines,
            selectedId = value.leftLineId,
            onSelect = onChangeLeft
        )

        OperatorPicker(
            selected = value.op,
            onSelect = onChangeOp
        )

        LinePicker(
            label = "라인",
            lines = lines,
            selectedId = value.rightLineId,
            onSelect = onChangeRight
        )

        IconButton(onClick = onRemove) {
            Icon(
                painterResource(id = R.drawable.ic_cross_circle),
                contentDescription = "삭제",
                tint = StormGray60,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LinePicker(
    label: String,
    lines: List<IndicatorLine>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { expanded = true },
        shape = RoundedCornerShape(8.dp),
        ) {
        Text(
            lines.firstOrNull { it.indicatorLineId == selectedId }?.indicatorLineName
                ?: "$label 선택"
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        lines.forEach { line ->
            DropdownMenuItem(
                text = { Text(line.indicatorLineName) },
                onClick = {
                    onSelect(line.indicatorLineId)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun OperatorPicker(
    selected: CompareOp,
    onSelect: (CompareOp) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val text = when (selected) {
        CompareOp.GREATER_THAN -> ">"
        CompareOp.GREATER_THAN_OR_EQUAL -> ">="
        CompareOp.EQUAL -> "="
        CompareOp.LESS_THAN_OR_EQUAL -> "<="
        CompareOp.LESS_THAN -> "<"
    }
    OutlinedButton(
        onClick = { expanded = true },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text)
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        CompareOp.values().forEach { op ->
            DropdownMenuItem(
                text = { Text(
                    when (op) {
                        CompareOp.GREATER_THAN -> ">"
                        CompareOp.GREATER_THAN_OR_EQUAL -> ">="
                        CompareOp.EQUAL -> "="
                        CompareOp.LESS_THAN_OR_EQUAL -> "<="
                        CompareOp.LESS_THAN -> "<"
                    }
                ) },
                onClick = { onSelect(op); expanded = false }
            )
        }
    }
}