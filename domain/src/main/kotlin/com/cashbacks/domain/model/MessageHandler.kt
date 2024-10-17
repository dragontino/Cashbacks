package com.cashbacks.domain.model

interface MessageHandler {
    fun getExceptionMessage(throwable: Throwable): String?
}