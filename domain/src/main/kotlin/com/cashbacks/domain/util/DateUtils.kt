package com.cashbacks.domain.util

import android.os.Parcel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parceler
import java.time.format.DateTimeFormatter
import java.util.Locale


typealias DateFormatBuilder = DateTimeFormatBuilder.WithDate.() -> Unit
typealias InstantFormatBuilder = DateTimeFormatBuilder.WithDateTimeComponents.() -> Unit


object DateTimeFormats {
    // TODO: добавить часовой пояс
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

    // TODO: переделать через Kotlin
    /**
     * Represents date pattern dd MMMM yyyy
     */
    fun displayableDateFormatter(locale: Locale = Locale.getDefault()) =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
}


object LocalDateParceler : Parceler<LocalDate?> {
    override fun LocalDate?.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this?.format().toString())
    }

    override fun create(parcel: Parcel): LocalDate? {
        return when (val string = parcel.readString()) {
            null, "null" -> null
            else -> string.parseToDate()
        }
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


fun LocalDate.epochMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
    return atStartOfDayIn(timeZone).toEpochMilliseconds()
}


fun getDisplayableDateString(
    dateString: String,
    inputFormatBuilder: DateFormatBuilder = DateTimeFormats.defaultDateFormat(),
    locale: Locale = Locale.getDefault()
): String = dateString
    .parseToDate(formatBuilder = inputFormatBuilder)
    .toJavaLocalDate()
    .format(DateTimeFormats.displayableDateFormatter(locale))


fun LocalDate(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate {
    return Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
}