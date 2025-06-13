package com.cashbacks.common.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.util.Locale


typealias DateFormatBuilder = DateTimeFormatBuilder.WithDate.() -> Unit

object DateTimeFormats {

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

    /**
    * Represents date pattern "dd/MM/yyyy"
    */
    fun defaultDateFormat(): DateFormatBuilder = {
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        year()
    }
}



object DateUtils {
    val MaxDate = LocalDate(2100, 12, 31)
    val MinDate = LocalDate(2000, 1, 1)


    fun LocalDate.getDisplayableString(
        short: Boolean = false,
        locale: Locale = Locale.getDefault(),
        displayablePattern: String? = null
    ): String {
        val formatter = displayablePattern
            ?.let { DateTimeFormatter.ofPattern(it, locale) }
            ?: DateTimeFormats.displayableDateFormatter(short, locale)

        return this.toJavaLocalDate().format(formatter)
    }


    fun LocalDate.calculateDaysBetweenToday(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Int {
        val today = Clock.System.todayIn(timeZone)
        return today.daysUntil(this)
    }
}


fun String.parseToDate(
    formatBuilder: DateFormatBuilder = DateTimeFormats.defaultDateFormat()
): LocalDate {
    val format = LocalDate.Format { formatBuilder() }
    return LocalDate.parse(this, format)
}


fun LocalDate.format(
    formatBuilder: DateFormatBuilder = DateTimeFormats.defaultDateFormat()
): String {
    val format = LocalDate.Format { formatBuilder() }
    return this.format(format = format)
}


fun LocalDate.epochMillis(timeZone: TimeZone = TimeZone.UTC): Long {
    return atStartOfDayIn(timeZone).toEpochMilliseconds()
}


fun LocalDate(epochMillis: Long, timeZone: TimeZone = TimeZone.UTC): LocalDate {
    return Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
}


fun Clock.System.today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    return Clock.System.todayIn(timeZone)
}