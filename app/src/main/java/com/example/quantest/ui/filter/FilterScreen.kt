package com.example.quantest.ui.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.ui.component.QuanTestTopBar

@Composable
fun FilterScreen(
    viewModel: FilterViewModel = viewModel(),
    onSearchClick: () -> Unit
) {
    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) }
    ){ innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Filter Screen",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
