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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.domain.model.AppException
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.model.SettingsNotFoundException

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
    val PaymentSystem?.title @Composable
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
        val painterRes = when (paymentSystem) {
            PaymentSystem.Visa -> R.drawable.visa_logo
            PaymentSystem.MasterCard -> R.drawable.mastercard_logo
            PaymentSystem.Mir -> R.drawable.mir_logo
            PaymentSystem.JCB -> R.drawable.jcb_logo
            PaymentSystem.UnionPay -> R.drawable.unionpay_logo
            PaymentSystem.AmericanExpress -> R.drawable.american_express_logo
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


data object BankCardMapper {
    fun addSpacesToCardNumber(numberWithoutSpaces: String) = buildString {
        for (i in numberWithoutSpaces.indices) {
            if (i > 0 && i % 4 == 0) append(" ")
            append(numberWithoutSpaces[i])
        }
    }.trimIndent()

    fun removeSpacesFromNumber(cardNumber: String) = cardNumber.filter { it != ' ' }
}


data object ExceptionMapper {
    fun AppException.getMessage(context: Context) = when (this) {
        EntryAlreadyExistsException -> context.getString(R.string.entry_already_exists_exception)
        is InsertionException -> this.message ?: ""
        SettingsNotFoundException -> context.getString(R.string.settings_not_found_exception)
    }

    fun Throwable.getMessage(context: Context) = when (this) {
        is AppException -> getMessage(context)
        else -> ""
    }
}