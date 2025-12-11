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
    data class EnterCode(val pinCode: String) : LoginIntent
    data class ClickEnterButton(val onSuccess: () -> Unit) : LoginIntent
    data object OpenHomeScreen : LoginIntent
}


internal sealed interface LoginMessage {
    data class UpdateState(val newState: LoginState) : LoginMessage
}


@Immutable
internal sealed class LoginState {
    open val pinCode: String? = null

    data object Loading : LoginState()

    data class Error(val exception: Exception, override val pinCode: String) : LoginState()

    data class SignUp(override val pinCode: String) : LoginState()

    data class SignIn(override val pinCode: String) : LoginState()


}