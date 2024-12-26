package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

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
        }
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
            getTypeName(resources).lowercase(),
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

data class InsertCashbackException(val cashback: Cashback, val failedMonth: LocalDate) : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.insert_cashback_exception,
            cashback.bankCard.name.ifBlank { cashback.bankCard.number },
            failedMonth.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMMM uuuu"))
        )
    }

}

data object BankCardNotSelectedException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.bank_card_not_selected)
    }
}

data object IncorrectCardNumberException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.incorrect_card_number)
    }
}

data object EmptyCardValidityPeriodException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.empty_card_validity_period_exception)
    }
}

data object IncorrectCardCvvException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.incorrect_card_cvv)
    }
}

data object EmptyPinCodeException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.empty_pin_code)
    }
}