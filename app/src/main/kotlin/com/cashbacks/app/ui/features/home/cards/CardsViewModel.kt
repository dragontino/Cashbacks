package com.cashbacks.app.ui.features.home.cards

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.cards.mvi.CardsAction
import com.cashbacks.app.ui.features.home.cards.mvi.CardsEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.PrimaryBankCard
import com.cashbacks.domain.usecase.cards.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cards.SearchBankCardsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class CardsViewModel @Inject constructor(
    fetchBankCardsUseCase: FetchBankCardsUseCase,
    searchBankCardsUseCase: SearchBankCardsUseCase,
    private val deleteBankCardUseCase: DeleteBankCardUseCase,
    private val messageHandler: MessageHandler
) : MviViewModel<CardsAction, CardsEvent>() {
    var state by mutableStateOf(ScreenState.Showing)
        private set

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)
        private set

    var swipedCardId: Long? by mutableStateOf(null)
        private set

    var expandedCardId: Long? by mutableStateOf(null)
        private set

    val cardsFlow: StateFlow<List<PrimaryBankCard>?> by lazy {
        combineTransform(
            flow = fetchBankCardsUseCase.fetchAllBankCards(),
            flow2 = snapshotFlow { appBarState }
        ) { allCards, appBarState ->
            state = ScreenState.Loading
            emit(null)
            val resultCards = when (appBarState) {
                is HomeTopAppBarState.Search -> searchBankCardsUseCase.searchBankCards(appBarState.query)
                is HomeTopAppBarState.TopBar -> allCards
            }
            delay(200)
            emit(resultCards)
            state = ScreenState.Showing
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
    }


    override suspend fun actor(action: CardsAction) {
        when (action) {
            is CardsAction.ShowSnackbar -> push(CardsEvent.ShowSnackbar(action.message))

            is CardsAction.ShowDialog -> push(CardsEvent.ChangeOpenedDialog(action.type))

            is CardsAction.HideDialog -> push(CardsEvent.ChangeOpenedDialog(null))

            is CardsAction.ClickNavigationIcon -> push(CardsEvent.OpenNavigationDrawer)

            is CardsAction.UpdateAppBarState -> appBarState = action.state

            is CardsAction.CreateBankCard -> {
                push(CardsEvent.NavigateToBankCard(BankCardArgs()))
            }

            is CardsAction.OpenBankCardDetails -> {
                val args = BankCardArgs(id = action.cardId, isEditing = false)
                push(CardsEvent.NavigateToBankCard(args))
            }

            is CardsAction.EditBankCard -> {
                val args = BankCardArgs(id = action.cardId, isEditing = true)
                push(CardsEvent.NavigateToBankCard(args))
            }

            is CardsAction.DeleteBankCard -> {
                state = ScreenState.Loading
                delay(100)
                deleteBankCardUseCase.deleteBankCard(action.card).onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(CardsEvent.ShowSnackbar(it)) }
                }
                state = ScreenState.Showing
            }

            is CardsAction.SwipeCard -> swipedCardId = action.cardId.takeIf { action.isSwiped }

            is CardsAction.ExpandCard -> expandedCardId = action.cardId.takeIf { action.isExpanded }

        }
    }
}