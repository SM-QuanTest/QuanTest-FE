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
import com.example.quantest.R
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.data.model.Indicator
import com.example.quantest.ui.theme.Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorFilterScreen(
    viewModel: FilterViewModel = viewModel(),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                            style = MaterialTheme.typography.bodyLarge
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                                    // TODO: minText, maxText 값 ViewModel로 전달해서 상태 저장
                                },
                                text = "적용"
                            )
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            QuanTestOutlinedButton(
                onClick = onAddIndicatorClick,
                text = "+ 지표 추가"
            )
        }
    }

}