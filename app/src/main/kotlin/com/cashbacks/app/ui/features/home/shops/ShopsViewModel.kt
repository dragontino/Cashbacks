package com.cashbacks.app.ui.features.home.shops

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.shops.mvi.ShopsAction
import com.cashbacks.app.ui.features.home.shops.mvi.ShopsEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchCategoryShopsUseCase
import com.cashbacks.domain.usecase.shops.SearchShopsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ShopsViewModel @Inject constructor(
    fetchCategoryShopsUseCase: FetchCategoryShopsUseCase,
    searchShopsUseCase: SearchShopsUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val exceptionMessage: MessageHandler,
) : MviViewModel<ShopsAction, ShopsEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    var viewModelState by mutableStateOf(ViewModelState.Viewing)
        private set

    val shopsFlow: StateFlow<List<BasicCategoryShop>?> by lazy {
        combineTransform(
            flow = fetchCategoryShopsUseCase.fetchAllShops(),
            flow2 = fetchCategoryShopsUseCase.fetchShopsWithCashback(),
            flow3 = snapshotFlow {
                listOf(viewModelState, appBarState)
            }
        ) { allShops, shopsWithCashback, _ ->
            state = ScreenState.Loading
            emit(null)

            val appBarState = appBarState
            val resultShops = when {
                appBarState is HomeTopAppBarState.Search -> searchShopsUseCase.searchShops(
                    query = appBarState.query,
                    cashbacksRequired = viewModelState == ViewModelState.Viewing
                )

                viewModelState == ViewModelState.Editing -> allShops
                else -> shopsWithCashback
            }

            delay(200)
            emit(resultShops)
            state = ScreenState.Showing
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
    }

    var selectedShopIndex by mutableStateOf<Int?>(null)
        private set

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)
        private set


    override suspend fun actor(action: ShopsAction) {
        when (action) {
            is ShopsAction.ClickButtonBack -> push(ShopsEvent.NavigateBack)

            is ShopsAction.NavigateToShop -> push(ShopsEvent.NavigateToShop(action.args))

            is ShopsAction.UpdateAppBarState -> appBarState = action.state

            is ShopsAction.StartEdit -> viewModelState = ViewModelState.Editing

            is ShopsAction.SwitchEdit -> {
                viewModelState = when(viewModelState) {
                    ViewModelState.Editing -> ViewModelState.Viewing
                    ViewModelState.Viewing -> ViewModelState.Editing
                }
            }

    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteShopUseCase
                .deleteShop(shop)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
            is ShopsAction.FinishEdit -> viewModelState = ViewModelState.Viewing

            is ShopsAction.OpenDialog -> push(ShopsEvent.OpenDialog(action.type))

            is ShopsAction.CloseDialog -> push(ShopsEvent.CloseDialog)

            is ShopsAction.SwipeShop -> {
                selectedShopIndex = action.position.takeIf { action.isOpened }
            }

            is ShopsAction.DeleteShop -> {
                state = ScreenState.Loading
                delay(100)
                deleteShop(action.shop).onFailure { throwable ->
                    exceptionMessage.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(ShopsEvent.ShowSnackbar(it)) }
                }
                state = ScreenState.Showing
            }
        }
    }


    private suspend fun deleteShop(shop: Shop): Result<Unit> {
        return deleteShopUseCase.deleteShop(shop)
    }
}