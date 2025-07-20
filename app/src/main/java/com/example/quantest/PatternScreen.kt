package com.example.quantest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PatternScreen() {
    Scaffold(
        topBar = { HomeTopBar() }
    ){ innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Pattern Screen",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
