package com.cashbacks.app.ui.features.bankcard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.model.ComposableBankCard
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingAction
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.cards.EditBankCardUseCase
import com.cashbacks.domain.usecase.cards.GetBankCardUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlin.onSuccess

class BankCardEditingViewModel @AssistedInject constructor(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val editBankCardUseCase: EditBankCardUseCase,
    private val messageHandler: MessageHandler,
    @Assisted private val bankCardId: Long?
) : MviViewModel<BankCardEditingAction, BankCardEditingEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    internal val bankCard by lazy { ComposableBankCard(id = bankCardId) }

    var showPaymentSystemSelection by mutableStateOf(false)
        private set

    var showErrors by mutableStateOf(false)
        private set

    override suspend fun bootstrap() {
        state = ScreenState.Loading
        delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
        if (bankCardId != null) {
            getBankCardUseCase.getBankCardById(bankCardId)
                .onSuccess { bankCard.update(it) }
                .onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(BankCardEditingEvent.ShowSnackbar(it)) }
                }
        }
        state = ScreenState.Showing
    }


    override suspend fun actor(action: BankCardEditingAction) {
        when (action) {
            is BankCardEditingAction.ClickButtonBack -> push(BankCardEditingEvent.NavigateBack)

            is BankCardEditingAction.ShowSnackbar -> {
                push(BankCardEditingEvent.ShowSnackbar(action.message))
            }

            is BankCardEditingAction.Save -> {
                showErrors = true
                bankCard.updateAllErrors(messageHandler)

                when {
                    bankCard.haveErrors -> bankCard.errorMessage?.let {
                        return push(BankCardEditingEvent.ShowSnackbar(it))
                    }

                    !bankCard.haveChanges -> return action.onSuccess()

                    else -> {
                        state = ScreenState.Loading
                        delay(100)
                        saveCard().onSuccess {
                            action.onSuccess()
                        }.onFailure { throwable ->
                            messageHandler.getExceptionMessage(throwable)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { push(BankCardEditingEvent.ShowSnackbar(it)) }
                        }

                        state = ScreenState.Showing
                    }
                }
            }

            is BankCardEditingAction.ShowDialog -> {
                push(BankCardEditingEvent.ChangeOpenedDialog(action.type))
            }

            is BankCardEditingAction.HideDialog -> {
                push(BankCardEditingEvent.ChangeOpenedDialog(null))
            }

            is BankCardEditingAction.ShowBottomSheet -> {
                push(BankCardEditingEvent.OpenBottomSheet(action.type))
            }

            is BankCardEditingAction.HideBottomSheet -> push(BankCardEditingEvent.CloseBottomSheet)

            is BankCardEditingAction.ShowPaymentSystemSelection -> {
                showPaymentSystemSelection = true
            }

            is BankCardEditingAction.HidePaymentSystemSelection -> {
                showPaymentSystemSelection = false
            }

            is BankCardEditingAction.UpdateErrorMessage -> {
                bankCard.updateErrorMessage(action.error, messageHandler)
            }
        }
    }


    private suspend fun saveCard(): Result<Unit> {
        return when (bankCard.id) {
            null -> editBankCardUseCase.addBankCard(bankCard.mapToBankCard())
                .onSuccess { bankCard.id = it }
                .map {}

            else -> editBankCardUseCase.updateBankCard(bankCard.mapToBankCard())
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(bankCardId: Long?): BankCardEditingViewModel
    }
}