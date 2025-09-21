package com.example.quantest.ui.stockdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantest.ui.theme.StormGray10
import com.example.quantest.ui.theme.StormGray40
import com.example.quantest.ui.theme.StormGray80
import com.example.quantest.util.formatAmountToEokWon
import com.example.quantest.util.formatChartDate
import com.example.quantest.util.formatPrice
import com.example.quantest.util.formatVolume

@Composable
fun InfoTabContent(
    viewModel: StockDetailViewModel
) {
    val data = viewModel.latestChartData

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "시세",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "${formatChartDate(data?.chartDate)} 기준",
                fontSize = 14.sp,
                color = StormGray40
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 1년 최저가 ~ 최고가 슬라이더
        // TODO: 실제 데이터 연동
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("1년 최저가\n49,900원", fontSize = 12.sp)
//            Text("1년 최고가\n88,800원", fontSize = 12.sp)
//        }

        Spacer(modifier = Modifier.height(12.dp))

        // 시가, 종가, 거래량, 거래대금
        val labelTextStyle = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = StormGray40
        )

        val valueTextStyle = TextStyle(
            fontSize = 16.sp,
            color = StormGray80
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 컬럼 (시가/종가)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("시가", style = labelTextStyle)
                    Text(formatPrice(data?.chartOpen), style = valueTextStyle)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("종가", style = labelTextStyle)
                    Text(formatPrice(data?.chartClose), style = valueTextStyle)
                }
            }

            VerticalDivider(
                color = StormGray10,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight()
                    .width(1.dp)
            )

            // 오른쪽 컬럼 (거래량/거래대금)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("거래량", style = labelTextStyle)
                    Text(formatVolume(data?.chartVolume), style = valueTextStyle)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("거래대금", style = labelTextStyle)
                    Text(formatAmountToEokWon(data?.chartTurnover), style = valueTextStyle)
                }
            }
        }
    }
}