package com.example.quantest.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantest.ui.theme.QuanTestTheme
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import com.example.quantest.ui.component.QuanTestOutlinedButton

@Preview(
    name = "ChartFilterScreen - Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun Preview_ChartFilterScreen_Light() {
    QuanTestTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ChartFilterScreen()
        }
    }
}

@Composable
fun ChartFilterScreen() {
    Column(Modifier.fillMaxSize()) {
        ChartRangeRow(label = "시가")
        HorizontalDivider()
        ChartRangeRow(label = "종가")
        HorizontalDivider()
        ChartRangeRow(label = "고가")
        HorizontalDivider()
        ChartRangeRow(label = "저가")
        HorizontalDivider()
        ChartRangeRow(label = "거래량")
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChartRangeRow(
    label: String,
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
                onClick = { /* TODO */ },
                text = "적용"
            )
        }
    }
}