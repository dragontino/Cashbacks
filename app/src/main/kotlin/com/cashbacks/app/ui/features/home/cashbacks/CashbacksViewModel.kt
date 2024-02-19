package com.cashbacks.app.ui.features.home.cashbacks

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.cashback.DeleteCashbackUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CashbacksViewModel @Inject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteCashbackUseCase: DeleteCashbackUseCase,
    private val exceptionMessage: AppExceptionMessage
) : EventsViewModel() {
    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cashbacks = mutableStateOf(listOf<Pair<Pair<String, String>, Cashback>>())
    val cashbacks = derivedStateOf { _cashbacks.value }

    var selectedCashbackIndex: Int? by mutableStateOf(null)

    init {
        fetchCashbacksUseCase.fetchAllCashbacks()
            .onEach {
                _cashbacks.value = it
                _state.value = if (it.isEmpty()) ListState.Empty else ListState.Stable
            }
            .launchIn(viewModelScope)
    }

    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteCashbackUseCase
                .deleteCashback(cashback)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }
}