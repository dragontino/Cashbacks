package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableBankCard
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.usecase.EditBankCardUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BankCardViewModel(
    private val useCase: EditBankCardUseCase,
    private val bankCardId: Long?
) : ViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _bankCard = mutableStateOf(ComposableBankCard())
    val bankCard = derivedStateOf { _bankCard.value }

    init {
        viewModelScope.launch {
            delay(100)
            if (bankCardId != null) {
                useCase.getBankCardById(bankCardId)
                    ?.let { _bankCard.value = ComposableBankCard(it) }
            }
            _state.value = when (bankCardId) {
                null -> ViewModelState.Editing
                else -> ViewModelState.Viewing
            }
        }
    }

    fun edit() {
        _state.value = ViewModelState.Editing
    }

    fun save() {
        _state.value = ViewModelState.Viewing
        viewModelScope.launch {
            when (bankCardId) {
                null -> useCase.addBankCard(bankCard.value.mapToBankCard())
                else -> useCase.updateBankCard(bankCard.value.mapToBankCard())
            }
        }
    }

    fun deleteCard() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            useCase.deleteBankCard(bankCard.value.mapToBankCard())
        }
    }

    fun getPaymentSystemByNumber(number: String): PaymentSystem? {
        return PaymentSystem.entries.find { number.startsWith(it.prefix) }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val bankCardUseCase: EditBankCardUseCase,
        private val id: Long?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankCardViewModel(bankCardUseCase, id) as T
        }
    }
}