package com.example.quantest.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.ui.component.QuanTestTopBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.quantest.R
import com.example.quantest.ui.theme.Navy
import androidx.compose.ui.graphics.Color

enum class FilterTab(val title: String) {
    DATE("날짜"),
    INDUSTRY("업종"),
    CHART("차트"),
    INDICATOR("지표")
}

@Composable
fun FilterScreen(
    viewModel: FilterViewModel = viewModel(),
    onSearchClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(FilterTab.DATE) }


    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) }
    ){ innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // 상단 탭
            QuanTestTabRow(
                tabs = FilterTab.values(),
                selected = selectedTab,
                onSelected = { selectedTab = it },
                titleProvider = { it.title }
            )

            // 선택 칩
            SelectedChipsBar(viewModel = viewModel)

            // 탭별 화면
            when (selectedTab) {
                FilterTab.DATE -> DateFilterScreen()
                FilterTab.INDUSTRY -> IndustryFilterScreen(viewModel)
                FilterTab.CHART -> ChartFilterScreen()
                FilterTab.INDICATOR -> IndicatorFilterScreen()
            }

        }
    }
}

@Composable
private fun SelectedChipsBar(viewModel: FilterViewModel) {
    val sectors by viewModel.sectors.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()

    if (selectedIds.isEmpty()) {
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        return
    }

    val selectedList = remember(selectedIds, sectors) {
        selectedIds.mapNotNull { id -> sectors.find { it.sectorId == id } }
    }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedList.forEach { sector ->
                AssistChip(
                    onClick = { viewModel.removeSector(sector.sectorId) }, // X 클릭으로 해제
                    label = { Text(sector.sectorName) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross_small),
                            contentDescription = "remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    border = BorderStroke(0.dp, Color.Transparent) // 테두리 제거
                )
            }
        }
    }
}
