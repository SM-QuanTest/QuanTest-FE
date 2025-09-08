package com.example.quantest.ui.pattern

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quantest.data.model.Pattern
import com.example.quantest.R
import com.example.quantest.data.api.RetrofitClient
import com.example.quantest.ui.component.QuanTestTabRow
import com.example.quantest.ui.component.QuanTestTopBar
import com.example.quantest.ui.theme.StormGray10
import com.example.quantest.ui.theme.StormGray40
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class PatternTab(val title: String, val direction: String) {
    BULLISH("상승형 패턴", "상승형"),
    BEARISH("하락형 패턴", "하락형")
}

@Composable
fun PatternScreen(
    viewModel: PatternViewModel = viewModel(),
    onSearchClick: () -> Unit,
    onPatternClick: (Int) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(PatternTab.BULLISH) }

    val patternList by viewModel.patterns.collectAsState()
    val filteredPatterns by remember(patternList, selectedTab) {
        derivedStateOf { patternList.filter { it.patternDirection == selectedTab.direction } }
    }

    Scaffold(
        topBar = { QuanTestTopBar(onSearchClick = onSearchClick) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // 상단 탭
            QuanTestTabRow(
                tabs = PatternTab.values(),
                selected = selectedTab,
                onSelected = { selectedTab = it },
                titleProvider = { it.title }
            )

            // 패턴 카드 리스트 (3열 그리드)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPatterns) { pattern ->
                    PatternCard(
                        pattern = pattern,
                        onClick = {
                            Log.d("PatternScreen", "Clicked patternId: ${pattern.patternId}")
                            onPatternClick(pattern.patternId)
                        }
                    )
                }
            }
        }
    }
}


private val IMAGE_BASE_URL: HttpUrl = RetrofitClient.BASE_URL.toHttpUrl()

fun String.toAbsoluteImageUrl(base: HttpUrl = IMAGE_BASE_URL): String {
    if (startsWith("http://", true) || startsWith("https://", true)) return this
    return base.resolve(this)?.toString()
        ?: (base.toString().trimEnd('/') + "/" + trimStart('/'))
}

@Composable
fun PatternCard(
    pattern: Pattern,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StormGray10)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pattern.patternImageUrl.toAbsoluteImageUrl())
                    .crossfade(true)
                    .listener(
                        onStart = { req ->
                            Log.d("PatternCard", "▶️ load start url=${req.data}")
                        },
                        onError = { req, result ->
                            Log.e(
                                "PatternCard",
                                "❌ load error url=${req.data}, throwable=${result.throwable}"
                            )
                        },
                        onSuccess = { req, result ->
                            Log.d(
                                "PatternCard",
                                "✅ load success url=${req.data}, source=${result.dataSource}"
                            )
                        }
                    )
                    .build(),
                contentDescription = pattern.patternName,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pattern.patternName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = "next",
                modifier = Modifier.size(16.dp),
                tint = StormGray40
            )
        }
    }
}