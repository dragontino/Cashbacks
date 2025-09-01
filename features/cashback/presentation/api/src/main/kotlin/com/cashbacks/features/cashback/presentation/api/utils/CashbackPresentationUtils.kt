package com.cashbacks.features.cashback.presentation.api.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.DateUtils.calculateDaysBetweenToday
import com.cashbacks.common.utils.DateUtils.getDisplayableString
import com.cashbacks.common.utils.now
import com.cashbacks.features.cashback.domain.model.Cashback
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.util.Locale


object CashbackPresentationUtils {
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
                val today = LocalDate.now(timeZone)
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
        val today = LocalDate.now()
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