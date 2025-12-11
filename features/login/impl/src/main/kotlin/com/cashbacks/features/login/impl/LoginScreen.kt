package com.cashbacks.features.login.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.utils.mvi.IntentSender
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoginRoot(
    navigateToMainScreen: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is LoginLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }

                is LoginLabel.NavigateToHomeScreen -> navigateToMainScreen()
            }
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intentSender = IntentSender(viewModel::sendIntent)
    )
}

@Composable
internal fun LoginScreen(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<LoginIntent>,
) {

}

@Preview
@Composable
private fun LoginScreenPreview() {
    CashbacksTheme {
        LoginScreen(
            state = LoginState.SignIn(pinCode = "12345"),
            snackbarHostState = remember { SnackbarHostState() },
            intentSender = IntentSender()
        )
    }
}