package com.cashbacks.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
sealed class MeasureUnit : Parcelable {
    abstract fun getDisplayableString(locale: Locale = Locale.getDefault()): String

    data object Percent : MeasureUnit() {
        override fun toString(): String = PERCENT_MARK
        override fun getDisplayableString(locale: Locale): String = PERCENT_MARK
    }

    data class Currency(val currency: java.util.Currency) : MeasureUnit() {
        override fun toString(): String = currency.currencyCode
        override fun getDisplayableString(locale: Locale) = currency.getSymbol(locale)
    }

    companion object {
        private const val PERCENT_MARK = "%"

        internal fun fromString(unitString: String): MeasureUnit = when(unitString) {
            PERCENT_MARK -> Percent
            else -> Currency(java.util.Currency.getInstance(unitString))
        }
    }
}


fun MeasureUnit(unitString: String): MeasureUnit {
    return MeasureUnit.fromString(unitString)
}