package com.cashbacks.app.model

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.PaymentSystem

data object ColorDesignMapper {
    fun ColorDesign.title(context: Context): String = when (this) {
        ColorDesign.Light -> context.getString(R.string.light_theme)
        ColorDesign.Dark -> context.getString(R.string.dark_theme)
        ColorDesign.System -> context.getString(R.string.system_theme)
    }

    val ColorDesign.icon get() = when (this) {
        ColorDesign.Light -> Icons.Outlined.LightMode
        ColorDesign.Dark -> Icons.Outlined.DarkMode
        ColorDesign.System -> Icons.Rounded.Devices
    }
}


data object PaymentSystemMapper {
    @Composable
    fun PaymentSystemImage(
        paymentSystem: PaymentSystem,
        modifier: Modifier = Modifier,
        maxWidth: Dp = 50.dp
    ) {
        val painterRes = when (paymentSystem) {
            PaymentSystem.Visa -> R.drawable.visa_logo
            PaymentSystem.MasterCard -> R.drawable.mastercard_logo
            PaymentSystem.Mir -> R.drawable.mir_logo
            PaymentSystem.JCB -> R.drawable.jcb_logo
            PaymentSystem.UnionPay -> R.drawable.unionpay_logo
            PaymentSystem.AmericanExpress -> R.drawable.american_express_logo
        }

        Image(
            painter = painterResource(painterRes),
            contentDescription = "payment system image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .then(modifier)
                .widthIn(max = maxWidth)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(Color.White)
                .padding(5.dp)
        )
    }
}