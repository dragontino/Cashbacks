package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.usecase.card.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.card.GetBankCardUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BankCardViewerViewModel(
    private val getBankCardUseCase: GetBankCardUseCase,
    private val deleteBankCardUseCase: DeleteBankCardUseCase,
    val cardId: Long
) : ViewModel() {

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


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val getBankCardUseCase: GetBankCardUseCase,
        private val deleteBankCardUseCase: DeleteBankCardUseCase,
        private val cardId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankCardViewerViewModel(getBankCardUseCase, deleteBankCardUseCase, cardId) as T
        }
    }
}