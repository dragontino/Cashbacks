package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Cashback


@Composable
fun BasicInfoCashback(cashback: Cashback) {
    val textColor = MaterialTheme.colorScheme.onBackground.animate()
    val textStyle = MaterialTheme.typography.bodySmall

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${cashback.roundedAmount}%",
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cashback.bankCard.paymentSystem?.let {
                PaymentSystemMapper.PaymentSystemImage(
                    paymentSystem = it,
                    drawBackground = false,
                    maxWidth = 30.dp
                )
            }
            Text(text = cashback.bankCard.name, color = textColor, style = textStyle)
            Text(
                text = cashback.bankCard.hiddenLastDigitsOfNumber,
                color = textColor,
                style = textStyle
            )
        }
    }
}