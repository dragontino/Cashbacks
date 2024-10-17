package com.cashbacks.app.model

import android.content.Context
import com.cashbacks.domain.model.AppException
import com.cashbacks.domain.model.MessageHandler

class MessageHandlerImpl(private val context: Context) : MessageHandler {
    override fun getExceptionMessage(throwable: Throwable): String? = when (throwable) {
        is AppException -> throwable.getMessage(context.applicationContext.resources)
        else -> throwable.localizedMessage ?: throwable.message
    }
}