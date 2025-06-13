package com.cashbacks.features.cashback.domain.model

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
sealed class MeasureUnit {
    abstract fun getDisplayableString(locale: Locale = Locale.getDefault()): String

    data object Percent : MeasureUnit() {
        override fun toString(): String = PERCENT_MARK
        override fun getDisplayableString(locale: Locale): String = PERCENT_MARK
    }

    data class Currency(val currency: java.util.Currency) : MeasureUnit() {
        override fun toString(): String = currency.currencyCode
        override fun getDisplayableString(locale: Locale): String = currency.getSymbol(locale)
    }

    companion object {
        const val PERCENT_MARK = "%"

        internal fun fromString(unitString: String): MeasureUnit = when(unitString) {
            PERCENT_MARK -> Percent
            else -> Currency(java.util.Currency.getInstance(unitString))
        }
    }
}


fun MeasureUnit(unitString: String): MeasureUnit {
    return MeasureUnit.fromString(unitString)
}