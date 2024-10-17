package com.cashbacks.app.ui.features.home.cashbacks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.cashbacks.mvi.CashbacksAction
import com.cashbacks.app.ui.features.home.cashbacks.mvi.CashbacksEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.SearchCashbacksUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class CashbacksViewModel @Inject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    searchCashbacksUseCase: SearchCashbacksUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val messageHandler: MessageHandler
) : MviViewModel<CashbacksAction, CashbacksEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    var selectedCashbackIndex: Int? by mutableStateOf(null)
        private set

    var showBottomSheet by mutableStateOf(false)
        private set

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)
        private set

    val cashbacksFlow: StateFlow<List<FullCashback>?> by lazy {
        combineTransform(
            flow = fetchCashbacksUseCase.fetchAllCashbacks(),
            flow2 = snapshotFlow { appBarState }
        ) { allCashbacks, appBarState ->
            state = ScreenState.Loading
            emit(null)

            val resultCashbacks = when (appBarState) {
                is HomeTopAppBarState.Search -> searchCashbacksUseCase.searchCashbacks(appBarState.query)
                is HomeTopAppBarState.TopBar -> allCashbacks
            }

            emit(resultCashbacks)
            state = ScreenState.Showing
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
    }


    override suspend fun actor(action: CashbacksAction) {
        when (action) {
            is CashbacksAction.ClickButtonBack -> push(CashbacksEvent.NavigateBack)

            is CashbacksAction.ClickNavigationButton -> push(CashbacksEvent.OpenNavigationDrawer)

            is CashbacksAction.NavigateToCashback -> {
                push(CashbacksEvent.NavigateToCashback(action.args))
            }

            is CashbacksAction.UpdateAppBarState -> appBarState = action.state

            is CashbacksAction.OpenBottomSheet -> showBottomSheet = true

            is CashbacksAction.CloseBottomSheet -> showBottomSheet = false

            is CashbacksAction.OpenDialog -> push(CashbacksEvent.OpenDialog(action.type))

            is CashbacksAction.CloseDialog -> push(CashbacksEvent.CloseDialog)

            is CashbacksAction.DeleteCashback -> {
                state = ScreenState.Loading
                delay(100)
                deleteCashback(action.cashback).onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(CashbacksEvent.ShowSnackbar(it)) }
                }
                state = ScreenState.Showing
            }

            is CashbacksAction.SwipeCashback -> {
                selectedCashbackIndex = action.position.takeIf { action.isOpened }
            }
        }
    }

    private suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        return deleteCashbacksUseCase.deleteCashback(cashback)
    }
}