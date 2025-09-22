@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.quantest.ui.filter

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.quantest.R
import com.example.quantest.ui.theme.StormGray60
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateFilterTab(
    onDateSelected: (Long) -> Unit = {}
) {
    var showPicker by remember { mutableStateOf(false) }

    // DatePicker 상태 (오늘을 기본값으로)
    val today = remember { System.currentTimeMillis() }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = today)

    // 선택 표시 텍스트
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA) }
    val selectedText = remember(dateState.selectedDateMillis) {
        dateState.selectedDateMillis?.let { sdf.format(Date(it)) } ?: "날짜 선택"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 선택 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { showPicker = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedText)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_box),
                    contentDescription = "date",
                    tint = StormGray60,
                    modifier = Modifier.size(20.dp)
                )
            }
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
                // 연/월 빠른 이동 토글
                showModeToggle = true
            )
        }
    }
}