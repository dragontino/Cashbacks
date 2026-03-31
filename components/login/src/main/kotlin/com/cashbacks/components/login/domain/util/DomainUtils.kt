package com.cashbacks.components.login.domain.util

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.components.login.domain.model.LoginCredentials

const val REQUIRED_PASSWORD_LENGTH = 4

internal fun LoginCredentials.validate(context: Context) = runCatching {
    if (password.length < REQUIRED_PASSWORD_LENGTH) {
        throw TooShortPasswordException(REQUIRED_PASSWORD_LENGTH).toException(context)
    }
}