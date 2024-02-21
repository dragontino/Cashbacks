package com.cashbacks.app.ui.features.cashback

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCashback
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.EditCashbackUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CashbackViewModel @AssistedInject constructor(
    private val editCashbackUseCase: EditCashbackUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val fetchBankCardsUseCase: FetchBankCardsUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted("cashback") internal val cashbackId: Long?,
    @Assisted("parent") private val parentId: Long?,
    @Assisted private val parentName: String?
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cashback = mutableStateOf(ComposableCashback())
    val cashback = derivedStateOf { _cashback.value }

    var showBankCardsSelection by mutableStateOf(false)


    init {
        viewModelScope.launch {
            if (cashbackId != null) {
                delay(100)
                editCashbackUseCase
                    .getCashbackById(cashbackId)
                    .getOrNull()
                    ?.let { _cashback.value = ComposableCashback(it) }
            }
            _state.value = ViewModelState.Viewing
        }
    }

    fun getAllBankCards(): LiveData<List<BankCard>> {
        return fetchBankCardsUseCase.fetchBankCards().asLiveData(timeoutInMs = 200)
    }


    fun saveCashback() {
        if (cashback.value.bankCard == null) return

        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            when (cashbackId) {
                null -> addCashback(cashback.value.mapToCashback())
                else -> updateCashback(cashback.value.mapToCashback())
            }
            _state.value = ViewModelState.Viewing
        }
    }


    private suspend fun addCashback(cashback: Cashback) {
        when (parentName) {
            Category::class.simpleName -> editCashbackUseCase.addCashbackToCategory(parentId!!, cashback)
            Shop::class.simpleName -> editCashbackUseCase.addCashbackToShop(parentId!!, cashback)
        }
    }


    private suspend fun updateCashback(cashback: Cashback) {
        val result = editCashbackUseCase.updateCashback(cashback)
        result.exceptionOrNull()
            ?.let(exceptionMessage::getMessage)
            ?.let(::showSnackbar)
    }


    fun deleteCashback() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase
                .deleteCashback(cashback.value.mapToCashback())
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            _state.value = currentState
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("cashback") id: Long?,
            @Assisted("parent") parentId: Long?,
            parentName: String?
        ): CashbackViewModel
    }
}