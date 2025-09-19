package com.example.quantest.ui.patternresult

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quantest.R
import com.example.quantest.model.ChangeDirection
import com.example.quantest.ui.theme.Blue
import com.example.quantest.ui.theme.Red
import com.example.quantest.util.formatPrice

@Composable
fun StockListItem(
    name: String,
    price: Int,
    change: String,
    direction: ChangeDirection,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        AsyncImage(
//            model = "",
//            contentDescription = "${name} 로고",
//            modifier = Modifier
//                .size(36.dp)
//                .clip(RoundedCornerShape(18.dp)),
//            placeholder = painterResource(id = R.drawable.ic_placeholder),
//            error = painterResource(id = R.drawable.ic_placeholder)
//        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatPrice(price), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    fontSize = 13.sp,
                    color = when {
                        change.startsWith("+") -> Red
                        change.startsWith("-") -> Blue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        when (direction) {
            ChangeDirection.UP -> Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = "상승",
                tint = Red,
                modifier = Modifier.size(20.dp)
            )
            ChangeDirection.DOWN -> Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = "하락",
                tint = Blue,
                modifier = Modifier.size(20.dp)
            )
            ChangeDirection.FLAT -> Icon(
                painter = painterResource(id = R.drawable.ic_flat),
                contentDescription = "보합",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

    }
}