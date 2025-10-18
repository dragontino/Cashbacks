package com.cashbacks.features.home.impl.screens.cashbacks

import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.mvi.IntentReceiverViewModel
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchAllCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.SearchCashbacksUseCase
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.CashbacksAction
import com.cashbacks.features.home.impl.mvi.CashbacksIntent
import com.cashbacks.features.home.impl.mvi.CashbacksLabel
import com.cashbacks.features.home.impl.mvi.CashbacksMessage
import com.cashbacks.features.home.impl.mvi.CashbacksState
import com.cashbacks.features.home.impl.mvi.HomeAction
import com.cashbacks.features.home.impl.utils.launchWithLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class CashbacksViewModel(
    fetchAllCashbacks: FetchAllCashbacksUseCase,
    searchCashbacks: SearchCashbacksUseCase,
    deleteCashback: DeleteCashbackUseCase,
    storeFactory: StoreFactory,
) : IntentReceiverViewModel<CashbacksIntent>() {

    private val store: Store<CashbacksIntent, CashbacksState, CashbacksLabel> by lazy {
        storeFactory.create(
            name = "CashbacksStore",
            autoInit = false,
            initialState = CashbacksState(),
            bootstrapper = coroutineBootstrapper<CashbacksAction>(Dispatchers.Default) {
                val cashbacksFlow = combineTransform(
                    flow = fetchAllCashbacks(),
                    flow2 = stateFlow.map { it.appBarState }.distinctUntilChanged()
                ) { allCashbacks, appBarState ->
                    dispatchFromAnotherThread(HomeAction.StartLoading)
                    emit(null)

                    val resultCashbacks = when (appBarState) {
                        is HomeTopAppBarState.Search -> searchCashbacks(appBarState.query)
                            .onFailure { throw it }
                            .getOrNull()

                        is HomeTopAppBarState.TopBar -> allCashbacks
                    }

                    emit(resultCashbacks)
                    dispatchFromAnotherThread(HomeAction.FinishLoading)
                }

                cashbacksFlow
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(HomeAction.DisplayMessage(it))
                        }
                    }
                    .onEach { dispatchFromAnotherThread(CashbacksAction.LoadCashbacks(it)) }
                    .launchIn(this)
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<HomeAction.StartLoading> {
                    dispatch(CashbacksMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<HomeAction.FinishLoading> {
                    dispatch(CashbacksMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<CashbacksAction.LoadCashbacks> {
                    dispatch(CashbacksMessage.UpdateCashbacks(it.cashbacks))
                }
                onAction<HomeAction.DisplayMessage> {
                    publish(CashbacksLabel.DisplayMessage(it.message))
                }

                onIntent<CashbacksIntent.ClickButtonBack> {
                    publish(CashbacksLabel.NavigateBack)
                }
                onIntent<CashbacksIntent.ClickNavigationButton> {
                    publish(CashbacksLabel.OpenNavigationDrawer)
                }
                onIntent<CashbacksIntent.NavigateToCashback> {
                    publish(CashbacksLabel.NavigateToCashback(it.args))
                }
                onIntent<CashbacksIntent.SwipeCashback> {
                    dispatch(CashbacksMessage.UpdateSwipedCashbackId(it.id))
                }
                onIntent<CashbacksIntent.DeleteCashback> { intent ->
                    launchWithLoading {
                        delay(100)
                        deleteCashback(intent.cashback).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forward(HomeAction.DisplayMessage(it))
                            }
                        }
                    }
                }
                onIntent<CashbacksIntent.OpenDialog> {
                    publish(CashbacksLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<CashbacksIntent.CloseDialog> {
                    publish(CashbacksLabel.ChangeOpenedDialog(null))
                }
                onIntent<CashbacksIntent.OpenBottomSheet> {
                    dispatch(CashbacksMessage.UpdateShowingBottomSheet(true))
                }
                onIntent<CashbacksIntent.CloseBottomSheet> {
                    dispatch(CashbacksMessage.UpdateShowingBottomSheet(false))
                }
                onIntent<CashbacksIntent.ChangeAppBarState> {
                    dispatch(CashbacksMessage.UpdateAppBarState(it.state))
                }
            },
            reducer = { msg: CashbacksMessage ->
                when (msg) {
                    is CashbacksMessage.UpdateAppBarState -> copy(appBarState = msg.state)
                    is CashbacksMessage.UpdateCashbacks -> copy(cashbacks = msg.cashbacks)
                    is CashbacksMessage.UpdateScreenState -> copy(screenState = msg.state)
                    is CashbacksMessage.UpdateSwipedCashbackId -> copy(swipedCashbackId = msg.id)
                    is CashbacksMessage.UpdateShowingBottomSheet -> copy(showBottomSheet = msg.showBottomSheet)
                }
            }
        )
    }

    internal val stateFlow: StateFlow<CashbacksState> = store.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
    )

    internal val labelFlow: Flow<CashbacksLabel> by lazy { store.labels }

    init {
        store.init()
    }


    override val scope: CoroutineScope get() = viewModelScope

    override fun acceptIntent(intent: CashbacksIntent) = store.accept(intent)


    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}