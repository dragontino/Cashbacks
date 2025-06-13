package com.cashbacks.features.cashback.presentation.api.resources

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

data object IncorrectCashbackAmountException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.incorrect_cashback_amount)
    }
}


data object ExpiredCashbacksDeletionException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.expired_cashbacks_deletion_failure)
    }
}