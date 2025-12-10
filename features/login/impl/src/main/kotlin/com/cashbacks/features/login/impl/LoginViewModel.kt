package com.cashbacks.features.login.impl

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.utils.mvi.IntentReceiverViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

@Stable
internal class LoginViewModel(
    private val storeFactory: StoreFactory,
    private val stateHandle: SavedStateHandle
) : IntentReceiverViewModel<LoginIntent>() {

    private val loginStore: Store<LoginIntent, LoginState, LoginLabel> by lazy {
        object : Store<LoginIntent, LoginState, LoginLabel> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginState.Loading,
            bootstrapper = coroutineBootstrapper<LoginAction> {

            },
            executorFactory = coroutineExecutorFactory {
                TODO("Handle actions and intents")
            },
            reducer = { message: LoginMessage ->
                when (message) {
                    else -> TODO("Handle messages")
                }
            }
        ) {}
    }


    internal val stateFlow: StateFlow<LoginState> = loginStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<LoginLabel> by lazy { loginStore.labels }

    override val scope get() = viewModelScope

    override fun acceptIntent(intent: LoginIntent) {
        loginStore.accept(intent)
    }
}