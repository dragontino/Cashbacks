package com.cashbacks.app.ui.features.bankcard

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.usecase.cards.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.cards.GetBankCardUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BankCardViewingViewModel @AssistedInject constructor(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val deleteBankCardUseCase: DeleteBankCardUseCase,
    @Assisted val cardId: Long
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Viewing)
    val state = derivedStateOf { _state.value }

    private val _bankCard = mutableStateOf(BankCard())
    val bankCard = derivedStateOf { _bankCard.value }


    suspend fun refreshCard() {
        _state.value = ViewModelState.Loading
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        getBankCardUseCase
            .getBankCardById(cardId)
            ?.let { _bankCard.value = it }
        _state.value = ViewModelState.Viewing
    }


    fun deleteCard() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteBankCardUseCase.deleteBankCard(bankCard.value)
            _state.value = ViewModelState.Viewing
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(cardId: Long): BankCardViewingViewModel
    }
}