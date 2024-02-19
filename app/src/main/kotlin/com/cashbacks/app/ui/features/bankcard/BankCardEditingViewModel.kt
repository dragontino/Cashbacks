package com.cashbacks.app.ui.features.bankcard

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableBankCard
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.usecase.card.EditBankCardUseCase
import com.cashbacks.domain.usecase.card.GetBankCardUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BankCardEditingViewModel @AssistedInject constructor(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val editBankCardUseCase: EditBankCardUseCase,
    @Assisted private val bankCardId: Long?
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _bankCard = mutableStateOf(ComposableBankCard())
    val bankCard = derivedStateOf { _bankCard.value }

    var showPaymentSystemSelection by mutableStateOf(false)

    private val bankCardJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        if (bankCardId != null) {
            getBankCardUseCase.getBankCardById(bankCardId)
                ?.let { _bankCard.value = ComposableBankCard(it) }
        }
        _state.value = ViewModelState.Editing
    }

    override fun onCleared() {
        bankCardJob.cancel()
        super.onCleared()
    }

    fun saveCard() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            when (bankCardId) {
                null -> editBankCardUseCase.addBankCard(bankCard.value.mapToBankCard())
                else -> editBankCardUseCase.updateBankCard(bankCard.value.mapToBankCard())
            }
            _state.value = ViewModelState.Editing
        }
    }


    fun getPaymentSystemByNumber(number: String): PaymentSystem? {
        return PaymentSystem.entries.find { number.startsWith(it.prefix) }
    }

    @AssistedFactory
    interface Factory {
        fun create(bankCardId: Long?): BankCardEditingViewModel
    }
}