package com.cashbacks.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class CalculationUnit : Parcelable {
    abstract fun getDisplayableString(): String

    data object Percent : CalculationUnit() {
        override fun toString(): String = PERCENT_MARK
        override fun getDisplayableString(): String = PERCENT_MARK
    }

    data class Currency(val currency: java.util.Currency) : CalculationUnit() {
        override fun toString(): String = currency.currencyCode
        override fun getDisplayableString() = currency.symbol
    }

    companion object {
        private const val PERCENT_MARK = "%"

        internal fun fromString(unitString: String): CalculationUnit = when(unitString) {
            PERCENT_MARK -> Percent
            else -> Currency(java.util.Currency.getInstance(unitString))
        }
    }
}


fun CalculationUnit(unitString: String): CalculationUnit {
    return CalculationUnit.fromString(unitString)
}