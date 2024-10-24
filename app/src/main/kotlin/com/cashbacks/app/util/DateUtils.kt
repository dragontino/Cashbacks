package com.cashbacks.app.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object DateFormats {
    // TODO: переделать через Kotlin
    /**
     * Represents date pattern dd MMMM yyyy
     */
    fun displayableDateFormatter(locale: Locale = Locale.getDefault()) =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
}

internal fun LocalDate.getDisplayableString(
    locale: Locale = Locale.getDefault(),
    displayablePattern: String? = null
): String {
    val formatter = displayablePattern
        ?.let { DateTimeFormatter.ofPattern(it, locale) }
        ?: DateFormats.displayableDateFormatter(locale)

    return this.toJavaLocalDate().format(formatter)
}