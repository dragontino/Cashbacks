package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableBankCard
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.usecase.card.EditBankCardUseCase
import com.cashbacks.domain.usecase.card.GetBankCardUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BankCardEditorViewModel(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val editBankCardUseCase: EditBankCardUseCase,
    private val bankCardId: Long?
) : ViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _bankCard = mutableStateOf(ComposableBankCard())
    val bankCard = derivedStateOf { _bankCard.value }

    var showPaymentSystemSelection by mutableStateOf(false)

    init {
        viewModelScope.launch {
            delay(AnimationDefaults.ScreenDelayMillis + 40L)
            if (bankCardId != null) {
                getBankCardUseCase.getBankCardById(bankCardId)
                    ?.let { _bankCard.value = ComposableBankCard(it) }
            }
            _state.value = ViewModelState.Editing
        }
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


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val getBankCardUseCase: GetBankCardUseCase,
        private val editBankCardUseCase: EditBankCardUseCase,
        private val id: Long?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankCardEditorViewModel(getBankCardUseCase, editBankCardUseCase, id) as T
        }
    }
}