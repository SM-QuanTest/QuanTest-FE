@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.quantest.ui.filter

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateFilterScreen(
    onDateSelected: (Long) -> Unit = {} // 선택된 날짜(epoch millis, 00:00가 아님에 유의)
) {
    var showPicker by remember { mutableStateOf(false) }

    // DatePicker 상태 (오늘을 기본값으로)
    val today = remember { System.currentTimeMillis() }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = today)

    // 선택 표시 텍스트 (java.time 없이 포맷)
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA) }
    val selectedText = remember(dateState.selectedDateMillis) {
        dateState.selectedDateMillis?.let { sdf.format(Date(it)) } ?: "날짜 선택"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(12.dp))

        // 선택 버튼(혹은 TextField 등 원하는 컴포넌트로 바꿔도 됨)
        Button(onClick = { showPicker = true }) {
            Text(selectedText)
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = dateState.selectedDateMillis
                        if (millis != null) onDateSelected(millis)
                        showPicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(
                state = dateState,
                // 연/월 빠른 이동 토글 제공 (달력 <-> 연도 목록)
                showModeToggle = true
            )
        }
    }
}