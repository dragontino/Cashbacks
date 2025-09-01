package com.cashbacks.common.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


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
        day()
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
        return LocalDate.now(timeZone).daysUntil(this)
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


@OptIn(ExperimentalTime::class)
fun LocalDate.epochMillis(timeZone: TimeZone = TimeZone.UTC): Long {
    return atStartOfDayIn(timeZone).toEpochMilliseconds()
}


@OptIn(ExperimentalTime::class)
fun LocalDate(epochMillis: Long, timeZone: TimeZone = TimeZone.UTC): LocalDate {
    return Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
}


fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    return LocalDateTime.now(timeZone).date
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return Clock.System.now().toLocalDateTime(timeZone)
}