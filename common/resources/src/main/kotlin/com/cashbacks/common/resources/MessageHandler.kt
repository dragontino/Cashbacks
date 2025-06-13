package com.cashbacks.common.resources

import android.content.Context
import androidx.annotation.StringRes

class MessageHandler(private val context: Context) {
    fun getMessage(@StringRes resourceId: Int): String {
        return context.applicationContext.getString(resourceId)
    }

    fun getMessage(@StringRes resourceId: Int, vararg arguments: Any): String {
        return context.applicationContext.getString(resourceId, arguments)
    }
}