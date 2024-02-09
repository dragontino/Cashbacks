package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.usecase.card.FetchBankCardsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CardsViewModel @Inject constructor(useCase: FetchBankCardsUseCase) : ViewModel() {

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cards = mutableStateOf(listOf<BankCard>())
    val cards = derivedStateOf { _cards.value }

    init {
        useCase
            .fetchBankCards()
            .onEach {
                _cards.value = it
                _state.value = if (it.isEmpty()) ListState.Empty else ListState.Stable
            }
            .launchIn(viewModelScope)
    }
}