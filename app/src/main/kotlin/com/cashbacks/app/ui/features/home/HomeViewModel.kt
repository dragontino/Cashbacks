package com.cashbacks.app.ui.features.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.home.mvi.HomeAction
import com.cashbacks.app.ui.features.home.mvi.HomeEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.repository.ShareDataRepository
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val shareDataRepository: ShareDataRepository,
    private val messageHandler: MessageHandler
) : MviViewModel<HomeAction, HomeEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    override suspend fun actor(action: HomeAction) {
        when (action) {
            is HomeAction.ClickButtonExportData -> {
                state = ScreenState.Loading
                shareDataRepository.exportData()
                    .onSuccess(action.onSuccess)
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(HomeEvent.ShowSnackbar(it)) }
                    }
                state = ScreenState.Showing
            }

            HomeAction.ClickButtonOpenSettings -> push(HomeEvent.NavigateToSettings)

            is HomeAction.ShowMessage -> push(HomeEvent.ShowSnackbar(action.message))

            HomeAction.ClickButtonOpenDrawer -> push(HomeEvent.OpenDrawer)

            HomeAction.ClickButtonCloseDrawer -> push(HomeEvent.CloseDrawer)
        }
    }
}