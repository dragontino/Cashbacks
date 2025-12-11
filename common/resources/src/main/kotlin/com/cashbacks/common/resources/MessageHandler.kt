package com.cashbacks.common.resources

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class MessageHandler(context: Context) {
    private val context = context.applicationContext

    fun getMessage(@StringRes resourceId: Int): String {
        return context.getString(resourceId)
    }

    fun getMessage(@StringRes resourceId: Int, vararg arguments: Any): String {
        return context.getString(resourceId, *arguments)
    }

    fun getPluralString(@PluralsRes resourceId: Int, quantity: Int): String {
        return context.resources.getQuantityString(resourceId, quantity)
    }

    fun getPluralString(@PluralsRes resourceId: Int, quantity: Int, vararg arguments: Any): String {
        return context.resources.getQuantityString(resourceId, quantity, *arguments)
    }
}