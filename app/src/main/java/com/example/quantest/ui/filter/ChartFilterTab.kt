package com.example.quantest.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import com.example.quantest.data.model.PriceType
import com.example.quantest.ui.component.QuanTestOutlinedButton


@Composable
fun ChartFilterTab(
    viewModel: FilterViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
    ) {
        ChartRangeRow(label = "시가", type = PriceType.OPEN, onApply = viewModel::applyChartRange)
        HorizontalDivider()
        ChartRangeRow(label = "종가", type = PriceType.CLOSE, onApply = viewModel::applyChartRange)
        HorizontalDivider()
        ChartRangeRow(label = "고가", type = PriceType.HIGH, onApply = viewModel::applyChartRange)
        HorizontalDivider()
        ChartRangeRow(label = "저가", type = PriceType.LOW, onApply = viewModel::applyChartRange)
        HorizontalDivider()
        ChartRangeRow(label = "거래량", type = PriceType.VOLUME, onApply = viewModel::applyChartRange)
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChartRangeRow(
    label: String,
    type: PriceType,
    onApply: (PriceType, Int?, Int?) -> Unit
) {
    var minText by remember { mutableStateOf("") }
    var maxText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // 라벨
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // 입력칸과 버튼
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
                    onApply(type, min, max)     // ViewModel 상태에 반영
                },
                text = "적용"
            )
        }
    }
}