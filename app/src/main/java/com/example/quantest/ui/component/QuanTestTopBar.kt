package com.example.quantest.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.quantest.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(onSearchClick: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {},
        actions = {
            IconButton(onClick = { onSearchClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer // 원하는 색
                )
            }
        }
    )
}