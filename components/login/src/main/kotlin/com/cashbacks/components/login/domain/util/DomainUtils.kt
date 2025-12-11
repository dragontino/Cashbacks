package com.cashbacks.components.login.domain.util

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.components.login.domain.model.LoginCredentials

internal fun LoginCredentials.validate(context: Context) = runCatching {
    if (password.length < 4) {
        throw TooShortPasswordException(4).toException(context)
    }
}