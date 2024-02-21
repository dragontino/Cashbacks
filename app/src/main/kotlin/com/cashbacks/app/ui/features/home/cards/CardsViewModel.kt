package com.cashbacks.app.ui.features.home.cards

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
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cards.SearchBankCardsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CardsViewModel @Inject constructor(
    fetchBankCardsUseCase: FetchBankCardsUseCase,
    searchBankCardsUseCase: SearchBankCardsUseCase
) : EventsViewModel(), Search {

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    private val allCards = mutableStateOf(listOf<BankCard>())
    var cards by mutableStateOf(listOf<BankCard>())

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)
    override val query = mutableStateOf("")

    val isSearch: Boolean get() = appBarState == HomeTopAppBarState.Search

    init {
        fetchBankCardsUseCase
            .fetchBankCards()
            .onEach { allCards.value = it }
            .launchIn(viewModelScope)

        snapshotFlow {
            arrayOf(
                allCards.value,
                appBarState,
                query.value
            )
        }
            .onEach {
                _state.value = ListState.Loading
                cards = when {
                    isSearch -> searchBankCardsUseCase.searchBankCards(query.value)
                    else -> allCards.value
                }
                delay(200)
                _state.value = if (cards.isEmpty()) ListState.Empty else ListState.Stable
            }
            .launchIn(viewModelScope)

    }
}