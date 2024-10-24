package com.cashbacks.app.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Devices
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
import com.cashbacks.domain.R
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.PaymentSystem

data object ColorDesignMapper {

    val ColorDesign.icon get() = when (this) {
        ColorDesign.Light -> Icons.Outlined.LightMode
        ColorDesign.Dark -> Icons.Outlined.DarkMode
        ColorDesign.System -> Icons.Rounded.Devices
    }

    val ColorDesign.isDark: Boolean
    @Composable get() = when (this) {
        ColorDesign.Light -> false
        ColorDesign.Dark -> true
        ColorDesign.System -> isSystemInDarkTheme()
    }
}


internal data object CashbackUtils {
    val Cashback.roundedAmount: String get() = amount
        .toDoubleOrNull()
        ?.takeIf { it % 1 == 0.0 }
        ?.toInt()?.toString()
        ?: amount


    val Cashback.displayableAmount get() = "$roundedAmount${calculationUnit.getDisplayableString()}"

    @Composable
    fun Cashback.getDisplayableExpirationDate(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): AnnotatedString = buildAnnotatedString {
        val numberOfDaysBeforeExpiration = calculateNumberOfDaysBeforeExpiration(timeZone)
        val text = when (numberOfDaysBeforeExpiration) {
            0 -> stringResource(R.string.today)
            1 -> stringResource(R.string.tomorrow)
            2 -> stringResource(R.string.after_tomorrow)
            else -> expirationDate?.getDisplayableString()
        }?.lowercase()

        val color = when (numberOfDaysBeforeExpiration) {
            in 0..2 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onBackground
        }

        if (text != null) {
            withStyle(SpanStyle(color = color)) {
                append(text)
            }
        }
    }


    fun Cashback.calculateNumberOfDaysBeforeExpiration(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Int? {
        val today = Clock.System.todayIn(timeZone)
        val expirationDate = expirationDate ?: return Int.MAX_VALUE
        return today.until(expirationDate, DateTimeUnit.DAY).takeIf { it >= 0 }
    }
}


internal data object PaymentSystemUtils {
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
        val painterRes = remember(paymentSystem) {
            when (paymentSystem) {
                PaymentSystem.Visa -> R.drawable.visa_logo
                PaymentSystem.MasterCard -> R.drawable.mastercard_logo
                PaymentSystem.Mir -> R.drawable.mir_logo
                PaymentSystem.JCB -> R.drawable.jcb_logo
                PaymentSystem.UnionPay -> R.drawable.unionpay_logo
                PaymentSystem.AmericanExpress -> R.drawable.american_express_logo
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


data object BankCardUtils {
    fun addSpacesToCardNumber(numberWithoutSpaces: String) = buildString {
        for (i in numberWithoutSpaces.indices) {
            if (i > 0 && i % 4 == 0) append(" ")
            append(numberWithoutSpaces[i])
        }
    }.trimIndent()

    fun removeSpacesFromNumber(cardNumber: String) = cardNumber.filter { it != ' ' }
}


sealed class CopyableBankCardPart {
    @get:StringRes
    protected abstract val descriptionRes: Int

    fun getDescription(context: Context): String {
        return context.getString(descriptionRes)
    }

    data object Number : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.card_number
    }

    data object Holder : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.full_name

    }

    data object Cvv : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.cvv_for_copy
    }
}