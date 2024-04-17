package com.cashbacks.app.model

import android.content.Context
import com.cashbacks.domain.model.AppException
import com.cashbacks.domain.model.AppExceptionMessage

class AppExceptionMessageImpl(private val context: Context) : AppExceptionMessage {
    override fun getMessage(exception: AppException): String {
        return exception.getMessage(context.resources)
    }
}