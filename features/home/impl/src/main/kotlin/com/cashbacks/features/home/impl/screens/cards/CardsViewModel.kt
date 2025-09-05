package com.cashbacks.features.home.impl.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.bankcard.domain.usecase.DeleteBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardsUseCase
import com.cashbacks.features.bankcard.domain.usecase.SearchBankCardsUseCase
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.api.emptyBankCardArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.BankCardsAction
import com.cashbacks.features.home.impl.mvi.BankCardsIntent
import com.cashbacks.features.home.impl.mvi.BankCardsLabel
import com.cashbacks.features.home.impl.mvi.BankCardsMessage
import com.cashbacks.features.home.impl.mvi.BankCardsState
import com.cashbacks.features.home.impl.mvi.HomeAction
import com.cashbacks.features.home.impl.utils.launchWithLoading
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
class CardsViewModel(
    fetchBankCardsUseCase: FetchBankCardsUseCase,
    searchBankCardsUseCase: SearchBankCardsUseCase,
    deleteBankCardUseCase: DeleteBankCardUseCase,
    storeFactory: StoreFactory,
) : ViewModel() {

    private val store: Store<BankCardsIntent, BankCardsState, BankCardsLabel> by lazy {
        storeFactory.create(
            name = "BankCardsStore",
            autoInit = false,
            initialState = BankCardsState(),
            bootstrapper = coroutineBootstrapper<BankCardsAction>(Dispatchers.Default) {
                val cardsFlow = combineTransform(
                    flow = fetchBankCardsUseCase.invoke(),
                    flow2 = stateFlow.map { it.appBarState }.distinctUntilChanged()
                ) { allCards, appBarState ->
                    dispatchFromAnotherThread(HomeAction.StartLoading)
                    emit(null)
                    val resultCards = when (appBarState) {
                        is HomeTopAppBarState.Search -> searchBankCardsUseCase(appBarState.query)
                            .onFailure { throw it }
                            .getOrNull()

                        is HomeTopAppBarState.TopBar -> allCards
                    }
                    delay(200)
                    emit(resultCards)
                    dispatchFromAnotherThread(HomeAction.FinishLoading)
                }

                cardsFlow
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(HomeAction.DisplayMessage(it))
                        }
                    }
                    .onEach { dispatchFromAnotherThread(BankCardsAction.LoadBankCards(it)) }
                    .launchIn(this)
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<HomeAction.StartLoading> {
                    dispatch(BankCardsMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<HomeAction.FinishLoading> {
                    dispatch(BankCardsMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<BankCardsAction.LoadBankCards> {
                    dispatch(BankCardsMessage.UpdateBankCards(it.cards))
                }
                onAction<HomeAction.DisplayMessage> {
                    publish(BankCardsLabel.DisplayMessage(it.message))
                }

                onIntent<BankCardsIntent.ClickNavigationButton> {
                    publish(BankCardsLabel.OpenNavigationDrawer)
                }
                onIntent<BankCardsIntent.CreateBankCard> {
                    publish(BankCardsLabel.NavigateToBankCard(emptyBankCardArgs()))
                }
                onIntent<BankCardsIntent.OpenBankCardDetails> {
                    val args = BankCardArgs.Viewing(it.cardId)
                    publish(BankCardsLabel.NavigateToBankCard(args))
                }
                onIntent<BankCardsIntent.EditBankCard> {
                    val args = BankCardArgs.Editing(it.cardId)
                    publish(BankCardsLabel.NavigateToBankCard(args))
                }
                onIntent<BankCardsIntent.DeleteBankCard> { intent ->
                    launchWithLoading {
                        delay(100)
                        deleteBankCardUseCase(intent.card).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(HomeAction.DisplayMessage(it))
                            }
                        }
                    }
                }
                onIntent<BankCardsIntent.SwipeCard> {
                    dispatch(BankCardsMessage.UpdateSwipedCardIndex(it.position))
                }
                onIntent<BankCardsIntent.ExpandCard> {
                    dispatch(BankCardsMessage.UpdateExpandedCardIndex(it.position))
                }
                onIntent<BankCardsIntent.OpenDialog> {
                    publish(BankCardsLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<BankCardsIntent.CloseDialog> {
                    publish(BankCardsLabel.ChangeOpenedDialog(null))
                }
                onIntent<BankCardsIntent.ChangeAppBarState> {
                    dispatch(BankCardsMessage.UpdateAppBarState(it.state))
                }
            },
            reducer = { msg: BankCardsMessage ->
                when (msg) {
                    is BankCardsMessage.UpdateScreenState -> copy(screenState = msg.state)
                    is BankCardsMessage.UpdateAppBarState -> copy(appBarState = msg.state)
                    is BankCardsMessage.UpdateBankCards -> copy(cards = msg.cards)
                    is BankCardsMessage.UpdateExpandedCardIndex -> copy(expandedCardIndex = msg.index)
                    is BankCardsMessage.UpdateSwipedCardIndex -> copy(swipedCardIndex = msg.index)
                }
            }
        )
    }


    internal val stateFlow: StateFlow<BankCardsState> = store.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<BankCardsLabel> = store.labels


    private val intentSharedFlow = MutableSharedFlow<BankCardsIntent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    init {
        store.init()
        intentSharedFlow
            .sample(DELAY_MILLIS)
            .onEach(store::accept)
            .launchIn(viewModelScope)
    }

    internal fun sendIntent(intent: BankCardsIntent, withDelay: Boolean = false) {
        when {
            withDelay -> intentSharedFlow.tryEmit(intent)
            else -> store.accept(intent)
        }
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }


    private companion object {
        const val DELAY_MILLIS = 50L
    }
}