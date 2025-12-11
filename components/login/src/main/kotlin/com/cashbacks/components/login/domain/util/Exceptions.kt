package com.cashbacks.components.login.domain.util

import com.cashbacks.common.resources.MessageException
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.resources.R

internal class TooShortPasswordException(private val minLength: Int) : MessageException {
    override fun getMessage(messageHandler: MessageHandler): String {
        return messageHandler.getPluralString(
            R.plurals.too_short_password_exception,
            minLength,
            minLength
        )
    }
}