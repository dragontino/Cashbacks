package com.cashbacks.features.home.impl.screens.shops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromShopUseCase
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.HomeAction
import com.cashbacks.features.home.impl.mvi.ShopWithCashback
import com.cashbacks.features.home.impl.mvi.ShopsAction
import com.cashbacks.features.home.impl.mvi.ShopsIntent
import com.cashbacks.features.home.impl.mvi.ShopsLabel
import com.cashbacks.features.home.impl.mvi.ShopsMessage
import com.cashbacks.features.home.impl.mvi.ShopsState
import com.cashbacks.features.home.impl.utils.launchWithLoading
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCase
import com.cashbacks.features.shop.domain.usecase.FetchAllShopsUseCase
import com.cashbacks.features.shop.domain.usecase.FetchShopsWithCashbackUseCase
import com.cashbacks.features.shop.domain.usecase.SearchShopsUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample

@OptIn(FlowPreview::class)
class ShopsViewModel(
    private val fetchAllShops: FetchAllShopsUseCase,
    private val fetchShopsWithCashback: FetchShopsWithCashbackUseCase,
    private val searchShops: SearchShopsUseCase,
    private val getMaxCashbacksFromShop: GetMaxCashbacksFromShopUseCase,
    private val deleteShop: DeleteShopUseCase,
    private val storeFactory: StoreFactory,
) : ViewModel() {

    private val shopsStore: Store<ShopsIntent, ShopsState, ShopsLabel> by lazy {
        storeFactory.create(
            name = "ShopsStore",
            autoInit = false,
            initialState = ShopsState(),
            bootstrapper = coroutineBootstrapper<ShopsAction>(Dispatchers.Default) {
                val shopsFlow = combineTransform(
                    flow = fetchAllShops(),
                    flow2 = fetchShopsWithCashback(),
                    flow3 = stateFlow.map { it.viewModelState }.distinctUntilChanged(),
                    flow4 = stateFlow.map { it.appBarState }.distinctUntilChanged()
                ) { allShops, shopsWithCashback, viewModelState, appBarState ->
                    dispatchFromAnotherThread(HomeAction.StartLoading)
                    delay(200)

                    val resultShops = when {
                        appBarState is HomeTopAppBarState.Search -> searchShops(
                            query = appBarState.query,
                            cashbacksRequired = viewModelState == ViewModelState.Viewing
                        ).onFailure { throw it }
                            .getOrNull()

                        viewModelState == ViewModelState.Editing -> allShops
                        else -> shopsWithCashback
                    }

                    resultShops
                        ?.flatMap { shop ->
                            val cashbacks = getMaxCashbacksFromShop(shop.id)
                                .onFailure { throw it }.getOrNull()
                            if (cashbacks.isNullOrEmpty()) {
                                listOf(ShopWithCashback(shop, maxCashback = null))
                            } else {
                                cashbacks.map { ShopWithCashback(shop, it) }
                            }
                        }
                        .let { emit(it) }
                    dispatchFromAnotherThread(HomeAction.FinishLoading)
                }

                shopsFlow
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(HomeAction.DisplayMessage(it))
                        }
                    }
                    .onEach { dispatchFromAnotherThread(ShopsAction.LoadShops(it)) }
                    .launchIn(this)
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<HomeAction.StartLoading> {
                    dispatch(ShopsMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<HomeAction.FinishLoading> {
                    dispatch(ShopsMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<HomeAction.DisplayMessage> {
                    publish(ShopsLabel.DisplayMessage(it.message))
                }
                onAction<ShopsAction.LoadShops> {
                    dispatch(ShopsMessage.UpdateShops(it.shops?.toImmutableList()))
                }

                onIntent<ShopsIntent.ClickButtonBack> {
                    publish(ShopsLabel.NavigateBack)
                }
                onIntent<ShopsIntent.ClickNavigationButton> {
                    publish(ShopsLabel.OpenNavigationDrawer)
                }
                onIntent<ShopsIntent.StartEdit> {
                    dispatch(ShopsMessage.UpdateViewModelState(ViewModelState.Editing))
                }
                onIntent<ShopsIntent.FinishEdit> {
                    dispatch(ShopsMessage.UpdateViewModelState(ViewModelState.Viewing))
                }
                onIntent<ShopsIntent.SwitchEdit> {
                    val newState = when (state().viewModelState) {
                        ViewModelState.Editing -> ViewModelState.Viewing
                        ViewModelState.Viewing -> ViewModelState.Editing
                    }
                    dispatch(ShopsMessage.UpdateViewModelState(newState))
                }
                onIntent<ShopsIntent.NavigateToShop> {
                    publish(ShopsLabel.NavigateToShop(it.args))
                }
                onIntent<ShopsIntent.NavigateToCashback> {
                    publish(ShopsLabel.NavigateToCashback(it.args))
                }

                onIntent<ShopsIntent.DeleteShop> { intent ->
                    launchWithLoading {
                        delay(100)
                        deleteShop(intent.shop).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(HomeAction.DisplayMessage(it))
                            }
                        }
                    }
                }

                onIntent<ShopsIntent.OpenDialog> {
                    publish(ShopsLabel.OpenDialog(it.type))
                }
                onIntent<ShopsIntent.CloseDialog> {
                    publish(ShopsLabel.CloseDialog)
                }
                onIntent<ShopsIntent.SwipeShop> {
                    dispatch(ShopsMessage.UpdateSwipedShopId(it.id))
                }
                onIntent<ShopsIntent.SelectShop> {
                    dispatch(ShopsMessage.UpdateSelectedShopId(it.id))
                }
                onIntent<ShopsIntent.ChangeAppBarState> {
                    dispatch(ShopsMessage.UpdateAppBarState(it.state))
                }
            },
            reducer = { msg: ShopsMessage ->
                when (msg) {
                    is ShopsMessage.UpdateScreenState -> copy(screenState = msg.state)
                    is ShopsMessage.UpdateViewModelState -> copy(viewModelState = msg.state)
                    is ShopsMessage.UpdateAppBarState -> copy(appBarState = msg.state)
                    is ShopsMessage.UpdateSwipedShopId -> copy(swipedShopId = msg.id)
                    is ShopsMessage.UpdateSelectedShopId -> copy(selectedShopId = msg.id)
                    is ShopsMessage.UpdateShops -> copy(shops = msg.shops)
                }
            }
        )
    }

    internal val stateFlow: StateFlow<ShopsState> = shopsStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L)
    )

    internal val labelFlow: Flow<ShopsLabel> = shopsStore.labels

    private val intentSharedFlow = MutableSharedFlow<ShopsIntent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    init {
        shopsStore.init()
        intentSharedFlow
            .sample(DELAY_MILLIS)
            .onEach { shopsStore.accept(it) }
            .launchIn(viewModelScope)
    }


    internal fun sendIntent(intent: ShopsIntent, withDelay: Boolean = false) {
        when {
            withDelay -> intentSharedFlow.tryEmit(intent)
            else -> shopsStore.accept(intent)
        }
    }


    override fun onCleared() {
        shopsStore.dispose()
        super.onCleared()
    }

    private companion object {
        const val DELAY_MILLIS = 50L
    }
}