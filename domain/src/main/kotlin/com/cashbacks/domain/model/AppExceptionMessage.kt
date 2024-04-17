package com.cashbacks.domain.model

interface AppExceptionMessage {
    fun getMessage(exception: AppException): String

    fun getMessage(throwable: Throwable): String? = when (throwable) {
        is AppException -> getMessage(throwable)
        is Exception -> throwable.localizedMessage
        else -> null
    }
}