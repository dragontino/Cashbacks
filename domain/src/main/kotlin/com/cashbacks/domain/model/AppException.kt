package com.cashbacks.domain.model

sealed class AppException(override val message: String? = null) : Exception(message)

data object SettingsNotFoundException : AppException()

data object EntryAlreadyExistsException : AppException()

data class InsertionException(override val message: String? = null) : AppException(message)