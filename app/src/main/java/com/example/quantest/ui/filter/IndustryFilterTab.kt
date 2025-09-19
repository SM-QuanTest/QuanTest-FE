package com.example.quantest.ui.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quantest.ui.theme.Navy

@Composable
fun IndustryFilterTab(
    viewModel: FilterViewModel
) {
    LaunchedEffect(Unit) { viewModel.loadSectors() }

    val sectors by viewModel.sectors.collectAsState()
    val selected by viewModel.selectedIds.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            if (loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sectors, key = { it.sectorId }) { s ->
                        val isChecked = s.sectorId in selected

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleSector(s.sectorId) }
                                .heightIn(min = 36.dp)
                        ) {
                            Checkbox(
                                checked = s.sectorId in selected,
                                onCheckedChange = { viewModel.toggleSector(s.sectorId) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Navy,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = s.sectorName,
                                color = if (isChecked) Navy else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

    }
}