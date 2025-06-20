package com.cashbacks.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.settings.domain.usecase.FetchSettingsUseCase
import com.cashbacks.features.settings.domain.usecase.UpdateSettingsUseCase
import com.cashbacks.features.settings.presentation.mvi.SettingsAction
import com.cashbacks.features.settings.presentation.mvi.SettingsIntent
import com.cashbacks.features.settings.presentation.mvi.SettingsLabel
import com.cashbacks.features.settings.presentation.mvi.SettingsMessage
import com.cashbacks.features.settings.presentation.mvi.SettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val storeFactory: StoreFactory,
    private val fetchSettings: FetchSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {

    private val settingsStore: Store<SettingsIntent, SettingsState, SettingsLabel> by lazy {
        storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsState(),
            bootstrapper = coroutineBootstrapper<SettingsAction>(Dispatchers.Default) {
                dispatch(SettingsAction.LoadingStarted)
                launch {
                    delay(250)
                    fetchSettings()
                        .catch { throwable ->
                            throwable.localizedMessage?.let {
                                dispatchFromAnotherThread(SettingsAction.DisplayMessage(it))
                            }
                        }
                        .onEach {
                            dispatchFromAnotherThread(SettingsAction.LoadSettings(it))
                            dispatchFromAnotherThread(SettingsAction.LoadingFinished)
                        }
                        .launchIn(viewModelScope)
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<SettingsAction.LoadingStarted> {
                    dispatch(SettingsMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<SettingsAction.LoadingFinished> {
                    if (state().screenState == ScreenState.Loading) {
                        dispatch(SettingsMessage.UpdateScreenState(ScreenState.Stable))
                    }
                }
                onAction<SettingsAction.DisplayMessage> {
                    publish(SettingsLabel.DisplayMessage(it.message))
                }
                onAction<SettingsAction.LoadSettings> {
                    dispatch(SettingsMessage.UpdateSettings(it.settings))
                }

                onIntent<SettingsIntent.ClickButtonBack> {
                    publish(SettingsLabel.NavigateBack)
                }
                onIntent<SettingsIntent.UpdateSetting> {
                    val currentValue = state().settings
                    val updatedValue = it.function(currentValue)
                    if (updatedValue != currentValue) {
                        launch {
                            forwardFromAnotherThread(SettingsAction.LoadingStarted)
                            delay(200)
                            updateSettings(updatedValue)
                                .onFailure { throwable ->
                                    throwable.localizedMessage?.let {
                                        publish(SettingsLabel.DisplayMessage(it))
                                    }
                                }
                            forwardFromAnotherThread(SettingsAction.LoadingFinished)
                        }
                    }
                }
            },
            reducer = { message: SettingsMessage ->
                when (message) {
                    is SettingsMessage.UpdateScreenState -> copy(
                        screenState = message.screenState
                    )
                    is SettingsMessage.UpdateSettings -> copy(
                        settings = message.settings
                    )
                }
            }
        )
    }


    internal val stateFlow: StateFlow<SettingsState> = settingsStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<SettingsLabel> by lazy { settingsStore.labels }


    internal fun sendIntent(intent: SettingsIntent) {
        settingsStore.accept(intent)
    }
}