package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R
import kotlin.reflect.KClass

sealed class AppException : Exception() {
    protected fun readResolve(): Any = Exception()

    abstract fun getMessage(resources: Resources): String
}


sealed class EntityException(private val type: KClass<*>) : AppException() {
    protected fun getTypeName(resources: Resources) = when (type) {
        Category::class -> resources.getString(R.string.category_title)
        Shop::class -> resources.getString(R.string.shop)
        Cashback::class -> resources.getString(R.string.cashback_title)
        else -> ""
    }.lowercase()
}


data object SettingsNotFoundException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.settings_not_found_exception)
    }
}

class EntryAlreadyExistsException(type: KClass<*>) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.entry_already_exists_exception,
            getTypeName(resources)
        )
    }
}

class InsertionException(type: KClass<*>, private val entityName: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.insertion_exception,
            getTypeName(resources),
            entityName
        )
    }
}

class UpdateException(type: KClass<*>, val name: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(
            R.string.update_exception,
            getTypeName(resources),
            name
        )
    }
}

class DeletionException(type: KClass<*>, val name: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.deletion_exception, getTypeName(resources), name)
    }
}

class EntityNotFoundException(type: KClass<*>, private val id: String) : EntityException(type) {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.entity_not_found_exception, getTypeName(resources), id)
    }
}

data object ExpiredCashbacksDeletionException : AppException() {
    override fun getMessage(resources: Resources): String {
        return resources.getString(R.string.expired_cashbacks_deletion_failure)
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