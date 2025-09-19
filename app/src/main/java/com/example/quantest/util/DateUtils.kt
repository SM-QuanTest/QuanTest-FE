package com.example.quantest.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 오늘 날짜를 yyyy-MM-dd 형식으로 반환
fun getTodayFormatted(): String {
    val today = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
    return formatter.format(today)
}

// 어제 날짜를 yyyy-MM-dd 형식으로 반환
fun getYesterdayFormatted(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, -1) // 하루 전으로 설정
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
    return formatter.format(calendar.time)
}