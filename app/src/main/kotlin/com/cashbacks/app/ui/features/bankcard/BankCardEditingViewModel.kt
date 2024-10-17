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

class BankCardEditingViewModel @AssistedInject constructor(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val editBankCardUseCase: EditBankCardUseCase,
    private val messageHandler: MessageHandler,
    @Assisted private val bankCardId: Long?
) : MviViewModel<BankCardEditingAction, BankCardEditingEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    val bankCard by lazy { ComposableBankCard(id = bankCardId) }

    var showPaymentSystemSelection by mutableStateOf(false)
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
                state = ScreenState.Loading
                delay(100)
                saveCard(bankCard)
                    .onSuccess {
                        bankCard.id = it
                        action.onSuccess()
                    }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(BankCardEditingEvent.ShowSnackbar(it)) }
                    }
                state = ScreenState.Showing
            }

            is BankCardEditingAction.OpenDialog -> {
                push(BankCardEditingEvent.ChangeOpenedDialog(action.type))
            }

            is BankCardEditingAction.CloseDialog -> {
                push(BankCardEditingEvent.ChangeOpenedDialog(null))
            }
            BankCardEditingAction.ShowPaymentSystemSelection -> showPaymentSystemSelection = true

            BankCardEditingAction.HidePaymentSystemSelection -> showPaymentSystemSelection = false

        }
    }

    private suspend fun saveCard(card: ComposableBankCard): Result<Long> {
        return when {
            !card.haveChanges -> Result.success(card.id!!)
            card.id == null -> editBankCardUseCase.addBankCard(bankCard.mapToBankCard())
            else -> editBankCardUseCase.updateBankCard(bankCard.mapToBankCard()).map { card.id!! }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(bankCardId: Long?): BankCardEditingViewModel
    }
}