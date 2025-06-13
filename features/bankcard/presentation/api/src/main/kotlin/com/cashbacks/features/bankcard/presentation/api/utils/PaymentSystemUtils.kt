package com.cashbacks.features.bankcard.presentation.api.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.common.resources.R
import com.cashbacks.features.bankcard.domain.model.PaymentSystem

data object PaymentSystemUtils {
    val PaymentSystem?.title
        @Composable
        get(): String {
            if (this == null) return stringResource(R.string.value_not_selected)
            val titles = stringArrayResource(R.array.payment_systems)
            return PaymentSystem.entries.indexOf(this).let { titles[it] }
        }

    @Composable
    fun PaymentSystemImage(
        paymentSystem: PaymentSystem,
        modifier: Modifier = Modifier,
        maxWidth: Dp = 50.dp,
        drawBackground: Boolean = true
    ) {
        val painterRes = remember(paymentSystem) {
            when (paymentSystem) {
                PaymentSystem.Visa -> com.cashbacks.features.bankcard.presentation.api.R.drawable.visa_logo
                PaymentSystem.MasterCard -> com.cashbacks.features.bankcard.presentation.api.R.drawable.mastercard_logo
                PaymentSystem.Mir -> com.cashbacks.features.bankcard.presentation.api.R.drawable.mir_logo
                PaymentSystem.JCB -> com.cashbacks.features.bankcard.presentation.api.R.drawable.jcb_logo
                PaymentSystem.UnionPay -> com.cashbacks.features.bankcard.presentation.api.R.drawable.unionpay_logo
                PaymentSystem.AmericanExpress -> com.cashbacks.features.bankcard.presentation.api.R.drawable.american_express_logo
            }
        }

        val backgroundModifier = when {
            drawBackground -> Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(Color.White)
                .padding(5.dp)

            else -> Modifier
        }

        Image(
            painter = painterResource(painterRes),
            contentDescription = "payment system image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .then(modifier)
                .widthIn(max = maxWidth)
                .then(backgroundModifier)
        )
    }
}