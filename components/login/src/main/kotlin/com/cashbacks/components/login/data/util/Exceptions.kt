package com.cashbacks.components.login.data.util

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

internal class NoSavedCredentialsException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.credentials_not_found_exception)
    }
}


internal class WrongPinException : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getMessage(R.string.wrong_pin_exception)
    }
}