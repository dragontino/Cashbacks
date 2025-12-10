package com.cashbacks.features.login.impl

import androidx.compose.runtime.Immutable

internal sealed interface LoginAction


internal sealed interface LoginLabel {
    data class DisplayMessage(val message: String) : LoginLabel
}


internal sealed interface LoginIntent {
    data class EnterPassword(val password: String) : LoginIntent
    data class ClickEnterButton(val onSuccess: () -> Unit) : LoginIntent
}


internal sealed interface LoginMessage {
    data class UpdatePassword(val password: String) : LoginMessage
    data class UpdateState(val newState: LoginState) : LoginMessage
}


@Immutable
internal sealed class LoginState {
    open val password: String? = null

    data object Loading : LoginState()

    data class Error(val exception: Exception, override val password: String) : LoginState()

    data class SignUp(override val password: String) : LoginState()

    data class SignIn(override val password: String) : LoginState()


}