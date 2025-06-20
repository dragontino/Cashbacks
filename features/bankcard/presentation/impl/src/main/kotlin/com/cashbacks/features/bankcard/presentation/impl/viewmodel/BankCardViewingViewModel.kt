package com.cashbacks.features.bankcard.presentation.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.utils.AnimationDefaults
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.usecase.DeleteBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardByIdUseCase
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.impl.mvi.BankCardAction
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingIntent
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingLabel
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingMessage
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BankCardViewingViewModel(
    private val fetchBankCardUseCase: FetchBankCardByIdUseCase,
    private val deleteBankCardUseCase: DeleteBankCardUseCase,
    private val storeFactory: StoreFactory,
    private val cardId: Long
) : ViewModel() {

    private val viewingStore: Store<ViewingIntent, ViewingState, ViewingLabel> by lazy {
        object : Store<ViewingIntent, ViewingState, ViewingLabel> by storeFactory.create(
            name = "BankCardViewingStore",
            initialState = ViewingState(),
            bootstrapper = coroutineBootstrapper<BankCardAction>(Dispatchers.Default) {
                launch {
                    dispatchFromAnotherThread(BankCardAction.LoadStarted)
                    delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
                    fetchBankCardUseCase.invoke(cardId)
                        .catch { throwable ->
                            throwable.message
                                ?.takeIf { it.isNotBlank() }
                                ?.let { dispatchFromAnotherThread(BankCardAction.DisplayMessage(it)) }
                        }
                        .onEach {
                            dispatchFromAnotherThread(BankCardAction.LoadBankCard(it))
                            dispatchFromAnotherThread(BankCardAction.LoadFinished)
                        }
                        .launchIn(this)
                }
            },
            executorFactory = coroutineExecutorFactory {
                onAction<BankCardAction.LoadStarted> {
                    dispatch(ViewingMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<BankCardAction.LoadFinished> {
                    dispatch(ViewingMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<BankCardAction.DisplayMessage> {
                    publish(ViewingLabel.DisplayMessage(it.message))
                }
                onAction<BankCardAction.LoadBankCard> {
                    dispatch(ViewingMessage.UpdateBankCard(it.card))
                }

                onIntent<ViewingIntent.ClickButtonBack> {
                    publish(ViewingLabel.NavigateBack)
                }
                onIntent<ViewingIntent.DisplayMessage> {
                    forward(BankCardAction.DisplayMessage(it.message))
                }
                onIntent<ViewingIntent.Delete> { intent ->
                    launch {
                        forward(BankCardAction.LoadStarted)
                        delay(100)
                        deleteBankCardUseCase(state().card)
                            .onSuccess { intent.onSuccess() }
                            .onFailure { throwable ->
                                throwable.message
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { publish(ViewingLabel.DisplayMessage(it)) }
                            }
                        forward(BankCardAction.LoadFinished)
                    }
                }
                onIntent<ViewingIntent.Edit> {
                    val bankCard = state().card
                    val bankCardArgs = BankCardArgs.Editing(bankCard.id)
                    publish(ViewingLabel.NavigateToEditingBankCard(bankCardArgs))
                }
                onIntent<ViewingIntent.OpenDialog> {
                    publish(ViewingLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<ViewingIntent.CloseDialog> {
                    publish(ViewingLabel.ChangeOpenedDialog(null))
                }
                onIntent<ViewingIntent.CopyText> {
                    publish(ViewingLabel.CopyText(it.text))
                }
            },
            reducer = { message: ViewingMessage ->
                when (message) {
                    is ViewingMessage.UpdateBankCard -> copy(card = message.bankCard)
                    is ViewingMessage.UpdateScreenState -> copy(screenState = message.state)
                }
            }
        ) {}
    }


    internal val stateFlow: StateFlow<ViewingState> = viewingStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelsFlow by lazy { viewingStore.labels }

    internal fun sendIntent(intent: ViewingIntent) {
        viewingStore.accept(intent)
    }
}