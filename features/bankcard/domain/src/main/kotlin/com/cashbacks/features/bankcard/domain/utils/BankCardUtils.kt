package com.cashbacks.features.bankcard.domain.utils

import com.cashbacks.features.bankcard.domain.model.BasicBankCard

object BankCardUtils {
    fun String.withSpaces(): String = chunked(4).joinToString(" ")

    fun removeSpacesFromNumber(cardNumber: String) = cardNumber.filter { it != ' ' }

    fun BasicBankCard.getHiddenNumber(): String {
        if (number.length < 2) return number

        val showingLength = number.length / 2
        val startIndex = showingLength / 2
        val hiddenLength = number.length - showingLength
        return number.replaceRange(
            range = startIndex..<(startIndex + hiddenLength),
            replacement = getHidden(hiddenLength)
        )
    }

    fun getHidden(length: Int, mask: Char = '\u2022') = buildString {
        repeat(length) {
            append(mask)
        }
    }
}