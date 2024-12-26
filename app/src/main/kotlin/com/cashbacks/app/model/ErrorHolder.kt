package com.cashbacks.app.model

import com.cashbacks.domain.model.MessageHandler

internal interface ErrorHolder<Error : Any> {
    val errors: Map<Error, String>
    val haveErrors: Boolean get() = errors.isNotEmpty()
    val errorMessage: String?

    fun updateErrorMessage(error: Error, messageHandler: MessageHandler)
}