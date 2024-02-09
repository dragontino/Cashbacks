package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCashback
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.card.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.EditCashbackUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CashbackViewModel @AssistedInject constructor(
    private val cashbackCategoryUseCase: CashbackCategoryUseCase,
    private val cashbackShopUseCase: CashbackShopUseCase,
    private val editCashbackUseCase: EditCashbackUseCase,
    private val fetchBankCardsUseCase: FetchBankCardsUseCase,
    @Assisted internal val cashbackId: Long?,
    @Assisted private val parentId: Long,
    @Assisted private val parentName: String
) : ViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cashback = mutableStateOf(ComposableCashback())
    val cashback = derivedStateOf { _cashback.value }

    var showConfirmDeletionDialog by mutableStateOf(false)
        private set

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

    fun openConfirmDeletionDialog() {
        showConfirmDeletionDialog = true
    }

    fun closeConfirmDeletionDialog() {
        showConfirmDeletionDialog = false
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
            Category::class.simpleName -> editCashbackUseCase.addCashbackToCategory(parentId, cashback)
            Shop::class.simpleName -> editCashbackUseCase.addCashbackToShop(parentId, cashback)
        }
    }


    private suspend fun updateCashback(cashback: Cashback) {
        when (parentName) {
            Category::class.simpleName -> editCashbackUseCase.updateCashbackInCategory(parentId, cashback)
            Shop::class.simpleName -> editCashbackUseCase.updateCashbackInShop(parentId, cashback)
        }
    }


    fun deleteCashback(errorMessage: (String) -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            when (parentName) {
                Category::class.simpleName -> cashbackCategoryUseCase.deleteCashbackFromCategory(
                    categoryId = parentId,
                    cashback = cashback.value.mapToCashback(),
                    errorMessage = errorMessage
                )
                Shop::class.simpleName -> cashbackShopUseCase.deleteCashbackFromShop(
                    shopId = parentId,
                    cashback = cashback.value.mapToCashback(),
                    errorMessage = errorMessage
                )
            }
            delay(100)
            _state.value = currentState
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(id: Long?, parentId: Long, parentName: String): CashbackViewModel
    }
}