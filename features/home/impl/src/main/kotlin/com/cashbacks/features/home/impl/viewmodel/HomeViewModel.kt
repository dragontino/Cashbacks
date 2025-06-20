package com.cashbacks.features.home.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.resources.AppInfo
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.home.impl.mvi.HomeAction
import com.cashbacks.features.home.impl.mvi.HomeIntent
import com.cashbacks.features.home.impl.mvi.HomeLabel
import com.cashbacks.features.home.impl.mvi.HomeMessage
import com.cashbacks.features.home.impl.mvi.HomeState
import com.cashbacks.features.home.impl.utils.launchWithLoading
import com.cashbacks.features.share.domain.usecase.ExportDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

internal class HomeViewModel(
    private val exportData: ExportDataUseCase,
    private val appInfo: AppInfo,
    private val storeFactory: StoreFactory,
) : ViewModel() {

    private val homeStore: Store<HomeIntent, HomeState, HomeLabel> by lazy {
        storeFactory.create(
            name = "HomeStore",
            initialState = HomeState(appInfo = appInfo),
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<HomeAction.StartLoading> {
                    dispatch(HomeMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<HomeAction.FinishLoading> {
                    dispatch(HomeMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<HomeAction.DisplayMessage> {
                    publish(HomeLabel.DisplayMessage(it.message, it.action))
                }

                onIntent<HomeIntent.ClickButtonOpenSettings> {
                    publish(HomeLabel.NavigateToSettings)
                }
                onIntent<HomeIntent.ClickButtonExportData> { intent ->
                    launchWithLoading {
                        exportData()
                            .onSuccess {
                                withContext(Dispatchers.Main) {
                                    intent.onSuccess(it)
                                }
                            }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forwardFromAnotherThread(HomeAction.DisplayMessage(it))
                                }
                            }
                    }
                }
                onIntent<HomeIntent.ShowMessage> {
                    forward(HomeAction.DisplayMessage(it.message, it.action))
                }
                onIntent<HomeIntent.ClickButtonOpenDrawer> {
                    publish(HomeLabel.OpenDrawer)
                }
                onIntent<HomeIntent.ClickButtonCloseDrawer> {
                    publish(HomeLabel.CloseDrawer)
                }
                onIntent<HomeIntent.NavigateToCategory> {
                    publish(HomeLabel.NavigateToCategory(it.args))
                }
                onIntent<HomeIntent.NavigateToShop> {
                    publish(HomeLabel.NavigateToShop(it.args))
                }
                onIntent<HomeIntent.NavigateToCashback> {
                    publish(HomeLabel.NavigateToCashback(it.args))
                }
                onIntent<HomeIntent.NavigateToBankCard> {
                    publish(HomeLabel.NavigateToBankCard(it.args))
                }
                onIntent<HomeIntent.OpenExternalFolder> {
                    publish(HomeLabel.OpenExternalFolder(it.path))
                }
            }
        )
    }


    internal val stateFlow: StateFlow<HomeState> by lazy {
        homeStore.stateFlow(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    }

    internal val labelFlow: Flow<HomeLabel> by lazy { homeStore.labels }

    internal fun sendIntent(intent: HomeIntent) {
        homeStore.accept(intent)
    }
}