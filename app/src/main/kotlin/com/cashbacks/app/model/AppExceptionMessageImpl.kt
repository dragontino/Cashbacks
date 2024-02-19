package com.cashbacks.app.model

import android.content.Context
import com.cashbacks.app.R
import com.cashbacks.domain.model.AppException
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.DeletionException
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.ExpiredCashbacksDeletionException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.SettingsNotFoundException
import com.cashbacks.domain.model.Shop

class AppExceptionMessageImpl(private val context: Context) : AppExceptionMessage {
    // TODO: 11.02.2024 доделать сообщения
    override fun getMessage(exception: AppException): String {
        return when (exception) {
            EntryAlreadyExistsException -> context.getString(R.string.entry_already_exists_exception)
            is InsertionException -> exception.message ?: ""
            SettingsNotFoundException -> context.getString(R.string.settings_not_found_exception)
            is DeletionException -> {
                val typeName = when (exception.type) {
                    Category::class -> context.getString(R.string.category_title)
                    Shop::class -> context.getString(R.string.shop)
                    Cashback::class -> context.getString(R.string.cashback_title)
                    else -> ""
                }.lowercase()
                return context.getString(R.string.deletion_exception, typeName, exception.name)
            }
            is ExpiredCashbacksDeletionException ->
                context.getString(R.string.expired_cashbacks_deletion_failture)
        }
    }
}