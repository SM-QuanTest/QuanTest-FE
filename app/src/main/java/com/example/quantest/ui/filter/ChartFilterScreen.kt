package com.example.quantest.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(54.dp))

        OutlinedTextField(
            value = minText,
            onValueChange = { value -> minText = value.filter { it.isDigit() } },
            placeholder = { Text("0") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Text("  -  ")

        OutlinedTextField(
            value = maxText,
            onValueChange = { value -> maxText = value.filter { it.isDigit() } },
            placeholder = { Text("최대") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Button(onClick = { /* TODO: 동작 */ }) {
            Text("적용")
        }
    }
}