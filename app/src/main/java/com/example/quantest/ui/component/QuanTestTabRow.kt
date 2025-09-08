package com.example.quantest.ui.component

import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Tab
import androidx.compose.material3.Text

@Composable
fun <T : Enum<T>> QuanTestTabRow(
    tabs: Array<T>,
    selected: T,
    onSelected: (T) -> Unit,
    titleProvider: (T) -> String
) {
    TabRow(selectedTabIndex = selected.ordinal) {
        tabs.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                text = {
                    Text(
                        text = titleProvider(tab),
                        fontWeight = if (selected == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}