package com.cashbacks.features.bankcard.presentation.impl.viewmodel

import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.utils.AnimationDefaults
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.common.utils.mvi.IntentReceiverViewModel
import com.cashbacks.common.utils.publishFromAnotherThread
import com.cashbacks.features.bankcard.domain.usecase.AddBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.GetBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.UpdateBankCardUseCase
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils
import com.cashbacks.features.bankcard.presentation.api.resources.EmptyCardValidityPeriodException
import com.cashbacks.features.bankcard.presentation.api.resources.EmptyPinCodeException
import com.cashbacks.features.bankcard.presentation.api.resources.IncorrectCardCvvException
import com.cashbacks.features.bankcard.presentation.api.resources.IncorrectCardNumberException
import com.cashbacks.features.bankcard.presentation.impl.mvi.BankCardAction
import com.cashbacks.features.bankcard.presentation.impl.mvi.BankCardError
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingIntent
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingLabel
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingMessage
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingState
import com.cashbacks.features.bankcard.presentation.impl.mvi.model.EditableBankCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class BankCardEditingViewModel(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val addBankCardUseCase: AddBankCardUseCase,
    private val updateBankCardUseCase: UpdateBankCardUseCase,
    private val messageHandler: MessageHandler,
    private val storeFactory: StoreFactory,
    private val bankCardId: Long?,
) : IntentReceiverViewModel<EditingIntent>() {

    private val editingStore: Store<EditingIntent, EditingState, EditingLabel> by lazy {
        object : Store<EditingIntent, EditingState, EditingLabel> by storeFactory.create(
            name = "BankCardEditingStore",
            initialState = EditingState(
                screenState = ScreenState.Loading
            ),
            bootstrapper = coroutineBootstrapper<BankCardAction>(Dispatchers.Default) {
                launch {
                    dispatchFromAnotherThread(BankCardAction.LoadStarted)
                    delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
                    if (bankCardId != null) {
                        getBankCardUseCase(bankCardId)
                            .onSuccess {
                                dispatchFromAnotherThread(BankCardAction.LoadBankCard(it))
                            }
                            .onFailure { throwable ->
                                throwable.message
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let {
                                        dispatchFromAnotherThread(BankCardAction.DisplayMessage(it))
                                    }
                            }
                    }
                    dispatchFromAnotherThread(BankCardAction.LoadFinished)
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<BankCardAction.LoadStarted> {
                    dispatch(EditingMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<BankCardAction.LoadFinished> {
                    dispatch(EditingMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<BankCardAction.DisplayMessage> {
                    publish(EditingLabel.DisplayMessage(it.message))
                }
                onAction<BankCardAction.LoadBankCard> {
                    dispatch(EditingMessage.SetInitialBankCard(it.card))
                    dispatch(EditingMessage.UpdateBankCard(EditableBankCard(it.card)))
                }

                onIntent<EditingIntent.DisplayMessage> {
                    forward(BankCardAction.DisplayMessage(it.message))
                }
                onIntent<EditingIntent.ClickButtonBack> {
                    publish(EditingLabel.NavigateBack)
                }
                onIntent<EditingIntent.Save> { intent ->
                    val state = state()
                    val errorMessages = BankCardError.entries
                        .mapNotNull { error ->
                            state.card.getErrorMessage(error)?.let { error to it }
                        }
                        .toMap()

                    when {
                        errorMessages.isNotEmpty() -> {
                            dispatch(EditingMessage.UpdateShowingErrors(true))
                            dispatch(EditingMessage.SetErrorMessages(errorMessages))
                            val message = BankCardError.entries.firstNotNullOf { errorMessages[it] }
                            publish(EditingLabel.DisplayMessage(message))
                        }

                        state.isChanged().not() -> {
                            intent.onSuccess()
                            return@onIntent
                        }

                        else -> launch {
                            forwardFromAnotherThread(BankCardAction.LoadStarted)
                            delay(100)
                            saveCard(state.card)
                                .onSuccess {
                                    withContext(Dispatchers.Main) { intent.onSuccess() }
                                }
                                .onFailure { throwable ->
                                    throwable.message
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let {
                                            publishFromAnotherThread(EditingLabel.DisplayMessage(it))
                                        }
                                }
                            forwardFromAnotherThread(BankCardAction.LoadFinished)
                        }
                    }
                }
                onIntent<EditingIntent.ShowDialog> {
                    publish(EditingLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<EditingIntent.HideDialog> {
                    publish(EditingLabel.ChangeOpenedDialog(null))
                }
                onIntent<EditingIntent.ShowBottomSheet> {
                    publish(EditingLabel.ChangeOpenedBottomSheet(it.type))
                }
                onIntent<EditingIntent.HideBottomSheet> {
                    publish(EditingLabel.ChangeOpenedBottomSheet(null))
                }
                onIntent<EditingIntent.ShowPaymentSystemSelection> {
                    dispatch(EditingMessage.UpdateShowingPaymentSystemSelection(true))
                }
                onIntent<EditingIntent.HidePaymentSystemSelection> {
                    dispatch(EditingMessage.UpdateShowingPaymentSystemSelection(false))
                }
                onIntent<EditingIntent.UpdateBankCard> {
                    dispatch(EditingMessage.UpdateBankCard(it.card))
                }
                onIntent<EditingIntent.UpdateErrorMessage> {
                    launch {
                        val state = state()
                        if (state.showErrors) {
                            val message = state.card.getErrorMessage(it.error)
                            dispatchFromAnotherThread(
                                EditingMessage.SetErrorMessage(it.error, message)
                            )
                        }
                    }
                }
            },
            reducer = { message: EditingMessage ->
                when (message) {
                    is EditingMessage.SetInitialBankCard -> copy(initialCard = message.bankCard)
                    is EditingMessage.UpdateBankCard -> copy(card = message.bankCard)
                    is EditingMessage.UpdateScreenState -> copy(screenState = message.state)
                    is EditingMessage.UpdateShowingErrors -> copy(showErrors = message.showErrors)
                    is EditingMessage.UpdateShowingPaymentSystemSelection -> copy(
                        showPaymentSystemSelection = message.show
                    )
                    is EditingMessage.SetErrorMessage -> copy(
                        errors = message.message
                            ?.let { errors.plus(message.error to it) }
                            ?: errors.minus(message.error)
                    )
                    is EditingMessage.SetErrorMessages -> copy(errors = message.messages)
                }
            }
        ) {}
    }


    internal val stateFlow: StateFlow<EditingState> = editingStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<EditingLabel> by lazy { editingStore.labels }

    override val scope: CoroutineScope get() = viewModelScope

    override fun acceptIntent(intent: EditingIntent) = editingStore.accept(intent)



    private fun EditableBankCard.getErrorMessage(error: BankCardError): String? {
        return when (error) {
            BankCardError.Number -> BankCardUtils
                .removeSpacesFromNumber(number.text)
                .takeIf { it.length < 16 }
                ?.let { IncorrectCardNumberException.getMessage(messageHandler) }

            BankCardError.ValidityPeriod -> when (validityPeriod) {
                null -> EmptyCardValidityPeriodException.getMessage(messageHandler)
                else -> null
            }

            BankCardError.Cvv -> cvv
                .takeIf { it.length < 3 }
                ?.let { IncorrectCardCvvException.getMessage(messageHandler) }

            BankCardError.Pin -> pin
                .takeIf { it.isBlank() }
                ?.let { EmptyPinCodeException.getMessage(messageHandler) }
        }
    }


    private suspend fun saveCard(bankCard: EditableBankCard): Result<Long> {
        return when (bankCard.id) {
            null -> addBankCardUseCase(bankCard.mapToBankCard())
            else -> updateBankCardUseCase(bankCard.mapToBankCard()).map { bankCard.id }
        }
    }
}