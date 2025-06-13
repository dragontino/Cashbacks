package com.cashbacks.common.resources

import android.content.Context

interface MessageException {
    fun getMessage(messageHandler: MessageHandler): String
}


fun MessageException.toException(
    appContext: Context,
    cause: Throwable? = null
): Exception = object : Exception(cause) {
    override val message: String get() {
        val messageHandler = MessageHandler(appContext)
        return getMessage(messageHandler)
    }
}