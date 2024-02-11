package com.cashbacks.app.model

import android.content.Context
import com.cashbacks.app.R
import com.cashbacks.domain.model.AppException
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.SettingsNotFoundException

class AppExceptionMessageImpl(private val context: Context) : AppExceptionMessage {
    // TODO: 11.02.2024 доделать сообщения
    override fun getMessage(exception: AppException): String {
        return when (exception) {
            EntryAlreadyExistsException -> context.getString(R.string.entry_already_exists_exception)
            is InsertionException -> exception.message ?: ""
            SettingsNotFoundException -> context.getString(R.string.settings_not_found_exception)
        }
    }
}