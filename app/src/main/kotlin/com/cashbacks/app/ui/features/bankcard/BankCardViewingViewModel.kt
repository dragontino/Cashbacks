package com.cashbacks.app.ui.features.bankcard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardViewingAction
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardViewingEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.cards.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.cards.GetBankCardUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BankCardViewingViewModel @AssistedInject constructor(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val deleteBankCardUseCase: DeleteBankCardUseCase,
    private val messageHandler: MessageHandler,
    @Assisted val cardId: Long
) : MviViewModel<BankCardViewingAction, BankCardViewingEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    var bankCard by mutableStateOf(FullBankCard())
        private set

    override suspend fun bootstrap() {
        state = ScreenState.Loading
        delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
        getBankCardUseCase.fetchBankCardById(
            cardId,
            onFailure = { throwable ->
                messageHandler.getExceptionMessage(throwable)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { push(BankCardViewingEvent.ShowSnackbar(it)) }
            }
        ).onEach {
            bankCard = it
            if (state == ScreenState.Loading) {
                state = ScreenState.Showing
            }
        }.launchIn(viewModelScope)
    }


    override suspend fun actor(action: BankCardViewingAction) {
        when (action) {
            is BankCardViewingAction.ClickButtonBack -> push(BankCardViewingEvent.NavigateBack)

            is BankCardViewingAction.ShowSnackbar -> {
                push(BankCardViewingEvent.ShowSnackbar(action.message))
            }

            is BankCardViewingAction.Delete -> {
                state = ScreenState.Loading
                delay(100)
                deleteCard(bankCard)
                    .onSuccess { action.onSuccess() }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(BankCardViewingEvent.ShowSnackbar(it)) }
                    }
                state = ScreenState.Showing
            }

            BankCardViewingAction.Edit -> {
                push(
                    event = BankCardViewingEvent.NavigateToEditingBankCard(
                        args = BankCardArgs(id = bankCard.id, isEditing = true),
                    ),
                )
            }

            is BankCardViewingAction.OpenDialog -> {
                push(BankCardViewingEvent.ChangeOpenedDialog(action.type))
            }

            is BankCardViewingAction.CloseDialog -> {
                push(BankCardViewingEvent.ChangeOpenedDialog(null))
            }

            is BankCardViewingAction.CopyText -> {
                push(BankCardViewingEvent.CopyText(action.text))
            }
        }
    }


    private suspend fun deleteCard(card: BasicBankCard): Result<Unit> {
        return deleteBankCardUseCase.deleteBankCard(card)
    }


    @AssistedFactory
    interface Factory {
        fun create(cardId: Long): BankCardViewingViewModel
    }
}