package com.cashbacks.features.login.impl

import androidx.compose.runtime.Immutable

internal sealed interface LoginAction {
    data object StartScreenLoading : LoginAction
    data class FinishScreenLoading(val isSignedUp: Boolean, val savedPinCode: String?) : LoginAction
    data class DisplayMessage(val message: String) : LoginAction
}


internal sealed interface LoginLabel {
    data class DisplayMessage(val message: String) : LoginLabel
    data object NavigateToHomeScreen : LoginLabel
}


internal sealed interface LoginIntent {
    data class EnterDigit(val digit: Int) : LoginIntent
    data object ClearLastDigit : LoginIntent
    data object OpenHomeScreen : LoginIntent
}


internal sealed interface LoginMessage {
    data class UpdateState(val newState: LoginState) : LoginMessage
}


@Immutable
internal sealed class LoginState {
    open val pinCode: String? = null

    data object Loading : LoginState()

    sealed class Sign : LoginState() {
        abstract override val pinCode: String
    }

    data class Error(val exception: Exception, val previousState: LoginState) : LoginState() {
        override val pinCode: String? = previousState.pinCode
        val errorMessage: String? get() = exception.localizedMessage
    }

    data class SignUp(
        override val pinCode: String,
        val repeatedPinCode: String = "",
    ) : Sign()

    data class SignIn(override val pinCode: String) : Sign()
}