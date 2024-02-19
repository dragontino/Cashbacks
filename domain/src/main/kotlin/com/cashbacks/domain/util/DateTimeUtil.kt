package com.cashbacks.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimePatterns {
    const val DatePattern = "dd/MM/yyyy"
}

fun String.parseToDate(pattern: String = DateTimePatterns.DatePattern): LocalDate {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return LocalDate.parse(this, formatter)
}


fun LocalDate.parseToString(pattern: String = DateTimePatterns.DatePattern): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}

val LocalDate.epochMillis: Long
    get() = toEpochDay() * 24 * 3600000


fun LocalDate(epochMillis: Long): LocalDate {
    return LocalDate.ofEpochDay(epochMillis / 3600000 / 24)
}