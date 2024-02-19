package com.cashbacks.domain.model

import kotlin.reflect.KClass

sealed class AppException(override val message: String? = null) : Exception(message)

data object SettingsNotFoundException : AppException()

data object EntryAlreadyExistsException : AppException()

data class InsertionException(override val message: String? = null) : AppException(message)

data class DeletionException(val type: KClass<*>, val name: String) : AppException()

data object ExpiredCashbacksDeletionException : AppException()