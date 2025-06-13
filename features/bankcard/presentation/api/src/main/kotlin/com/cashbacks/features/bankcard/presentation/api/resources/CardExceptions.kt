package com.cashbacks.features.bankcard.presentation.api.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R


data object BankCardNotSelectedException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.bank_card_not_selected)
    }
}

data object IncorrectCardNumberException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.incorrect_card_number)
    }
}

data object EmptyCardValidityPeriodException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.empty_card_validity_period_exception)
    }
}

data object IncorrectCardCvvException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.incorrect_card_cvv)
    }
}

data object EmptyPinCodeException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.empty_pin_code)
    }
}