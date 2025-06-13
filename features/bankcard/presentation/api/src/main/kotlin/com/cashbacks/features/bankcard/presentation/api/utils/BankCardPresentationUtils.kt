package com.cashbacks.features.bankcard.presentation.api.utils

import android.content.Context
import androidx.annotation.StringRes
import com.cashbacks.common.resources.R
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces

object BankCardPresentationUtils {
    fun BasicBankCard.getDisplayableString() = buildString {
        if (name.isNotBlank()) {
            append(name, " ")
        }
        append(getHiddenNumber().takeLast(8).withSpaces())
    }
}



sealed class CopyableBankCardPart {
    @get:StringRes
    protected abstract val descriptionRes: Int

    fun getDescription(context: Context): String {
        return context.getString(descriptionRes)
    }

    data object Number : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.card_number_for_copy
    }

    data object Holder : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.full_name_for_copy

    }

    data object Cvv : CopyableBankCardPart() {
        override val descriptionRes: Int = R.string.cvv_for_copy
    }
}