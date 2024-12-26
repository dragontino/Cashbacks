package com.cashbacks.app.util

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.DateUtils.calculateDaysBetweenToday
import com.cashbacks.app.util.DateUtils.getDisplayableString
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.Locale

internal data object ColorDesignUtils {
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


    val Cashback.displayableAmount get() = "$roundedAmount${measureUnit.getDisplayableString()}"


    @Composable
    fun formatDateStatus(
        targetDate: LocalDate,
        shorted: Boolean = false,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        return when (targetDate.calculateDaysBetweenToday(timeZone)) {
            -2 -> stringResource(R.string.before_yesterday)
            -1 -> stringResource(R.string.yesterday)
            0 -> stringResource(R.string.today)
            1 -> stringResource(R.string.tomorrow)
            2 -> stringResource(R.string.after_tomorrow)
            else -> {
                val today = Clock.System.todayIn(timeZone)
                val shortDate = shorted && today.year == targetDate.year
                targetDate.getDisplayableString(shortDate, locale)
            }
        }
    }


    @Composable
    fun Cashback.getDatesTitle(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val startDate = remember(startDate) {
            startDate?.takeIf {
                it.calculateDaysBetweenToday(timeZone) > 0 || expirationDate == null
            }
        }
        val expirationDate = remember(expirationDate) {
            expirationDate?.takeIf { it.calculateDaysBetweenToday(timeZone) >= 0 }
        }

        return when {
            startDate != null -> stringResource(R.string.valid)
            expirationDate != null -> stringResource(R.string.expires)
            else -> stringResource(R.string.expired)
        }
    }

    @Composable
    fun Cashback.getDisplayableDatesText(
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        locale: Locale = Locale.getDefault()
    ): AnnotatedString = buildAnnotatedString {
        val today = Clock.System.today()
        val startDate = remember(startDate) {
            startDate?.takeIf {
                it.calculateDaysBetweenToday() > 0 || expirationDate == null
            }
        }
        val expirationDate = expirationDate

        when {
            startDate != null -> {
                append(stringResource(R.string.valid_from).lowercase(), " ")
                append(
                    startDate.getDisplayableString(
                        short = today.year == startDate.year,
                        locale = locale
                    )
                )

                if (expirationDate != null) {
                    append(" ", stringResource(R.string.valid_through).lowercase(), " ")
                    append(
                        expirationDate.getDisplayableString(
                            short = today.year == expirationDate.year,
                            locale = locale
                        )
                    )
                }
            }

            expirationDate != null -> {
                val color = when (expirationDate.calculateDaysBetweenToday()) {
                    in 0..2 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onBackground
                }

                withStyle(SpanStyle(color = color)) {
                    append(
                        formatDateStatus(
                            targetDate = expirationDate,
                            shorted = true,
                            timeZone = timeZone,
                            locale = locale
                        )
                    )
                }
            }
            else -> {
                append(stringResource(R.string.indefinitely_period))
            }
        }
    }
}


internal data object PaymentSystemUtils {
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


internal data object BankCardUtils {
    fun String.withSpaces(): String = chunked(4).joinToString(" ")

    fun removeSpacesFromNumber(cardNumber: String) = cardNumber.filter { it != ' ' }

    fun BasicBankCard.hideNumber(): String {
        if (number.length < 2) return number

        val showingLength = number.length / 2
        val startIndex = showingLength / 2
        val hiddenLength = number.length - showingLength
        return number.replaceRange(
            range = startIndex..<(startIndex + hiddenLength),
            replacement = getHidden(hiddenLength)
        )
    }

    fun BasicBankCard.getDisplayableString() = buildString {
        if (name.isNotBlank()) {
            append(name, " ")
        }
        append(hideNumber().takeLast(8).withSpaces())
    }

    fun getHidden(length: Int, mask: Char = '\u2022') = buildString {
        repeat(length) {
            append(mask)
        }
    }
}


sealed class CopyableBankCardPart {
    @get:StringRes
    protected abstract val descriptionRes: Int

    fun getDescription(context: Context): String {
        return context.getString(descriptionRes)
    }

    data object Number : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.card_number_for_copy
    }

    data object Holder : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.full_name_for_copy

    }

    data object Cvv : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.cvv_for_copy
    }
}