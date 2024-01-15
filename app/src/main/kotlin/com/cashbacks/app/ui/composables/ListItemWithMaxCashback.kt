package com.cashbacks.app.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.PaymentSystem


@Composable
internal fun ListItemWithMaxCashback(
    name: String,
    maxCashback: Cashback?,
    cashbackPlaceholder: String,
    onClick: () -> Unit
) {
    val clickableModifier = when (maxCashback) {
        null -> Modifier
        else -> Modifier.clickable(onClick = onClick)
    }

    ListItem(
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            if (maxCashback == null) {
                Text(
                    text = cashbackPlaceholder,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        trailingContent = {
            when (maxCashback) {
                null -> IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "edit"
                    )
                }
                else -> BasicInfoAboutCashback(cashback = maxCashback)
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            supportingColor = MaterialTheme.colorScheme.error.animate(),
            trailingIconColor = MaterialTheme.colorScheme.primary.animate()
        ),
        modifier = Modifier
            .then(clickableModifier)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                    ).map { it.animate() }
                ),
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
    )
}


@Composable
private fun BasicInfoAboutCashback(cashback: Cashback) {
    val textColor = MaterialTheme.colorScheme.onBackground.animate()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.max_cashback_title), color = textColor)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "${cashback.amount}%", color = textColor)
            Row {
                Text(text = cashback.bankCard.name, color = textColor)
                Spacer(Modifier.width(10.dp))
                BankCardPaymentSystem(paymentSystem = cashback.bankCard.paymentSystem)
                Spacer(Modifier.width(5.dp))
                Text(text = "···· ${cashback.bankCard.hiddenNumber}", color = textColor)
            }
        }
    }
}


@Composable
private fun BankCardPaymentSystem(paymentSystem: PaymentSystem) {
    val resource = when (paymentSystem) {
        PaymentSystem.Visa -> R.drawable.visa_logo
        PaymentSystem.MasterCard -> R.drawable.mastercard_logo
        PaymentSystem.Mir -> R.drawable.mir_logo
        PaymentSystem.JCB -> R.drawable.jcb_logo
        PaymentSystem.UnionPay -> R.drawable.unionpay_logo
        PaymentSystem.AmericanExpress -> R.drawable.american_express_logo
    }

    Image(
        painter = painterResource(resource),
        contentDescription = "payment system logo"
    )
}