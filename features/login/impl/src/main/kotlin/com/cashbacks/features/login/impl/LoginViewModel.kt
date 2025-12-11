package com.cashbacks.features.login.impl

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.common.utils.mvi.IntentReceiverViewModel
import com.cashbacks.common.utils.publishFromAnotherThread
import com.cashbacks.components.login.domain.model.LoginCredentials
import com.cashbacks.components.login.domain.usecase.CheckIsAlreadySignedUpUseCase
import com.cashbacks.components.login.domain.usecase.SignInUseCase
import com.cashbacks.components.login.domain.usecase.SignUpUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
internal class LoginViewModel(
    private val storeFactory: StoreFactory,
    private val stateHandle: SavedStateHandle,
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase,
    private val checkIsAlreadySignedUp: CheckIsAlreadySignedUpUseCase
) : IntentReceiverViewModel<LoginIntent>() {

    private val loginStore: Store<LoginIntent, LoginState, LoginLabel> by lazy {
        object : Store<LoginIntent, LoginState, LoginLabel> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginState.Loading,
            bootstrapper = coroutineBootstrapper<LoginAction>(mainContext = Dispatchers.Default) {
                launch {
                    dispatchFromAnotherThread(LoginAction.StartScreenLoading)

                    val isSignUpAsync = async {
                        delay(250)
                        val isSignedUp = checkIsAlreadySignedUp()
                            .getOrElse { throwable ->
                                throwable.localizedMessage
                                    ?.let(LoginAction::DisplayMessage)
                                    ?.let { dispatchFromAnotherThread(it) }
                                return@getOrElse false
                            }
                        return@async isSignedUp
                    }
                    val savedDataAsync = async {
                        stateHandle.get<String>(PIN_CODE_SAVED_KEY)
                    }

                    dispatchFromAnotherThread(
                        LoginAction.FinishScreenLoading(
                            isSignedUp = isSignUpAsync.await(),
                            savedPinCode = savedDataAsync.await()
                        )
                    )
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<LoginAction.StartScreenLoading> {
                    dispatch(LoginMessage.UpdateState(LoginState.Loading))
                }
                onAction<LoginAction.FinishScreenLoading> {
                    val pinCode = it.savedPinCode.orEmpty()
                    val state = when {
                        it.isSignedUp -> LoginState.SignIn(pinCode)
                        else -> LoginState.SignUp(pinCode)
                    }
                    dispatch(LoginMessage.UpdateState(state))
                }
                onAction<LoginAction.DisplayMessage> {
                    publish(LoginLabel.DisplayMessage(it.message))
                }

                onIntent<LoginIntent.EnterCode> {
                    val newState = when (val state = state()) {
                        is LoginState.SignIn -> state.copy(pinCode = it.pinCode)
                        is LoginState.SignUp -> state.copy(pinCode = it.pinCode)
                        else -> return@onIntent
                    }
                    stateHandle[PIN_CODE_SAVED_KEY] = it.pinCode
                    dispatch(LoginMessage.UpdateState(newState))
                }
                onIntent<LoginIntent.ClickEnterButton> { intent ->
                    val state = state()
                    dispatch(LoginMessage.UpdateState(LoginState.Loading))
                    launch {
                        val result = when (state) {
                            is LoginState.Error -> {
                                state.exception.localizedMessage?.let {
                                    publishFromAnotherThread(LoginLabel.DisplayMessage(it))
                                }
                                return@launch
                            }

                            is LoginState.Loading -> return@launch
                            is LoginState.SignIn -> {
                                val credentials = LoginCredentials(password = state.pinCode)
                                signIn(credentials)
                            }

                            is LoginState.SignUp -> {
                                val credentials = LoginCredentials(password = state.pinCode)
                                signUp(credentials)
                            }
                        }

                        result
                            .onSuccess { intent.onSuccess() }
                            .onFailure { throwable ->
                                throwable.localizedMessage
                                    ?.let(LoginAction::DisplayMessage)
                                    ?.let { forwardFromAnotherThread(it) }
                            }
                    }
                }
                onIntent<LoginIntent.OpenHomeScreen> {
                    publish(LoginLabel.NavigateToHomeScreen)
                }
            },
            reducer = Reducer<LoginState, LoginMessage> { message ->
                when (message) {
                    is LoginMessage.UpdateState -> message.newState
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


    private companion object {
        const val PIN_CODE_SAVED_KEY = "PinCode"
    }
}