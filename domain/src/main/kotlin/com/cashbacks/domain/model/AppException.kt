package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R
import kotlin.reflect.KClass

sealed class AppException(override val message: String? = null) : Exception(message) {
    protected fun readResolve(): Any = SettingsNotFoundException

    abstract fun getMessage(resources: Resources): String
}

data object SettingsNotFoundException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.settings_not_found_exception)
    }
}

data object EntryAlreadyExistsException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.entry_already_exists_exception)
    }
}

data class InsertionException(override val message: String? = null) : AppException(message) {
    // TODO: 17.04.2024 доделать сообщения
    override fun getMessage(resources: Resources): String {
        return message ?: ""
    }
}

data class DeletionException(val type: KClass<*>, val name: String) : AppException() {
    override fun getMessage(resources: Resources): String {
        val typeName = when (type) {
            Category::class -> resources.getString(R.string.category_title)
            Shop::class -> resources.getString(R.string.shop)
            Cashback::class -> resources.getString(R.string.cashback_title)
            else -> ""
        }.lowercase()
        return resources.getString(R.string.deletion_exception, typeName, name)
    }
}

data object ExpiredCashbacksDeletionException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.expired_cashbacks_deletion_failture)
    }
}