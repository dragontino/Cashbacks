package com.cashbacks.app.ui.features.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.settings.mvi.SettingsAction
import com.cashbacks.app.ui.features.settings.mvi.SettingsEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val useCase: SettingsUseCase,
    private val messageHandler: MessageHandler
) : MviViewModel<SettingsAction, SettingsEvent>() {

    var settings by mutableStateOf(Settings())
        private set

    var state by mutableStateOf(ScreenState.Loading)
        private set

    override suspend fun bootstrap() {
        state = ScreenState.Loading
        delay(250)

        useCase.fetchSettings(
            onFailure = { throwable ->
                messageHandler.getExceptionMessage(throwable)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { push(SettingsEvent.ShowSnackbar(it)) }
            }
        ).onEach {
            settings = it
            if (state == ScreenState.Loading) {
                state = ScreenState.Showing
            }
        }.launchIn(viewModelScope)
    }

    override suspend fun actor(action: SettingsAction) {
        when (action) {
            is SettingsAction.ClickButtonBack -> push(SettingsEvent.NavigateBack)
            is SettingsAction.UpdateSetting -> {
                val updatedValue = action.function(settings)
                if (updatedValue != settings) {
                    state = ScreenState.Loading
                    delay(200)
                    useCase.updateSettings(updatedValue)
                        .onFailure { throwable ->
                            messageHandler.getExceptionMessage(throwable)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { push(SettingsEvent.ShowSnackbar(it)) }
                        }
                    state = ScreenState.Showing
                }
            }
        }
    }
}