package com.cashbacks.features.login.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.utils.mvi.IntentSender
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoginRoot(
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                else -> TODO("Handle labels")
            }
        }
    }

    LoginScreen(
        state = state,
        sendIntent = IntentSender(viewModel::sendIntent)
    )
}

@Composable
internal fun LoginScreen(
    state: LoginState,
    sendIntent: IntentSender<LoginIntent>,
) {

}

@Preview
@Composable
private fun LoginScreenPreview() {
    CashbacksTheme {
        LoginScreen(
            state = LoginState.SignIn(password = "12345"),
            sendIntent = IntentSender()
        )
    }
}