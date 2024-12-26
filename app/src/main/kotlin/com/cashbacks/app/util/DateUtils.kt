package com.cashbacks.app.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object DateFormats {
    /**
     * Represents date pattern dd MMMM yyyy
     */
    fun displayableDateFormatter(
        short: Boolean,
        locale: Locale = Locale.getDefault()
    ): DateTimeFormatter {
        val pattern = when {
            short -> "d MMMM"
            else -> "dd MMMM yyyy"
        }
        return DateTimeFormatter.ofPattern(pattern, locale)
    }
}


internal object DateUtils {
    fun LocalDate.getDisplayableString(
        short: Boolean = false,
        locale: Locale = Locale.getDefault(),
        displayablePattern: String? = null
    ): String {
        val formatter = displayablePattern
            ?.let { DateTimeFormatter.ofPattern(it, locale) }
            ?: DateFormats.displayableDateFormatter(short, locale)

        return this.toJavaLocalDate().format(formatter)
    }


    fun LocalDate.calculateDaysBetweenToday(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Int {
        val today = Clock.System.todayIn(timeZone)
        return today.daysUntil(this)
    }
}