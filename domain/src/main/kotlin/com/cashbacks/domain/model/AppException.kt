package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R

sealed class AppException : Exception() {
    protected fun readResolve(): Any = Exception()

    abstract fun getMessage(resources: Resources): String
}


sealed class EntityException(private val type: Type) : AppException() {
    enum class Type {
        Category,
        Shop,
        Cashback
    }

    protected fun getTypeName(resources: Resources): String {
        return when (type) {
            Type.Category -> resources.getString(R.string.category_title)
            Type.Shop -> resources.getString(R.string.shop_title)
            Type.Cashback -> resources.getString(R.string.cashback_title)
            else -> ""
        }.lowercase()
    }
}


data object SaveSettingsException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.save_settings_exception)
    }
}


data object SettingsNotFoundException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.settings_not_found_exception)
    }
}

class EntryAlreadyExistsException(type: Type) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.entry_already_exists_exception,
            getTypeName(resources)
        )
    }
}

class InsertionException(type: Type, private val entityName: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.insertion_exception,
            getTypeName(resources),
            entityName
        )
    }
}

class UpdateException(type: Type, val name: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.update_exception,
            getTypeName(resources),
            name
        )
    }
}

class DeletionException(type: Type, val name: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.deletion_exception, getTypeName(resources), name)
    }
}

class EntityNotFoundException(type: Type, private val id: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.entity_not_found_exception, getTypeName(resources), id)
    }
}

data object ExpiredCashbacksDeletionException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.expired_cashbacks_deletion_failure)
    }
}

data object ShopNameNotSelectedException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.shop_name_not_selected)
    }
}

data object CategoryNotSelectedException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.category_not_selected)
    }
}

data object ShopNotSelectedException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.shop_not_selected)
    }
}

data object IncorrectCashbackAmountException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.incorrect_cashback_amount)
    }
}

data object BankCardNotSelectedException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.bank_card_not_selected)
    }

}