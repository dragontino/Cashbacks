package com.cashbacks.app.ui.features.home.cashbacks

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.Search
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.SearchCashbacksUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CashbacksViewModel @Inject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    searchCashbacksUseCase: SearchCashbacksUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val exceptionMessage: AppExceptionMessage
) : EventsViewModel(), Search {
    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    private val allCashbacks =
        mutableStateOf(listOf<Pair<Pair<String, String>, Cashback>>())
    var cashbacks by mutableStateOf(listOf<Pair<Pair<String, String>, Cashback>>())
        private set

    var selectedCashbackIndex: Int? by mutableStateOf(null)

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)
    override val query = mutableStateOf("")

    val isSearch: Boolean get() = appBarState == HomeTopAppBarState.Search

    init {
        fetchCashbacksUseCase.fetchAllCashbacks()
            .onEach { allCashbacks.value = it }
            .launchIn(viewModelScope)

        snapshotFlow {
            arrayOf(
                allCashbacks.value,
                appBarState,
                query.value
            )
        }.onEach {
            cashbacks = when {
                isSearch -> searchCashbacksUseCase.searchCashbacks(query.value)
                else -> allCashbacks.value
            }
            _state.value = if (cashbacks.isEmpty()) ListState.Empty else ListState.Stable
        }.launchIn(viewModelScope)
    }

    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteCashbacksUseCase
                .deleteCashback(cashback)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }
}