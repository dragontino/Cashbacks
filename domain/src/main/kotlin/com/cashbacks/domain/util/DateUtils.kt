package com.cashbacks.domain.util

import android.os.Parcel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parceler


typealias DateFormatBuilder = DateTimeFormatBuilder.WithDate.() -> Unit

object DateTimeFormats {
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


fun LocalDate.epochMillis(timeZone: TimeZone = TimeZone.UTC): Long {
    return atStartOfDayIn(timeZone).toEpochMilliseconds()
}


fun LocalDate(epochMillis: Long, timeZone: TimeZone = TimeZone.UTC): LocalDate {
    return Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
}