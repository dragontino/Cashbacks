package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCashback
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.EditCashbackUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CashbackViewModel(
    private val cashbackCategoryUseCase: CashbackCategoryUseCase,
    private val cashbackShopUseCase: CashbackShopUseCase,
    private val editCashbackUseCase: EditCashbackUseCase,
    internal val cashbackId: Long?,
    private val parentId: Long,
    private val parentName: String,
    isEditing: Boolean
) : ViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cashback = mutableStateOf(ComposableCashback())
    val cashback = derivedStateOf { _cashback.value }


    init {
        viewModelScope.launch {
            if (cashbackId != null) {
                delay(100)
                editCashbackUseCase
                    .getCashbackById(cashbackId)
                    .getOrNull()
                    ?.let { _cashback.value = ComposableCashback(it) }
            }
            _state.value = when {
                isEditing || cashbackId == null -> ViewModelState.Editing
                else -> ViewModelState.Viewing
            }
        }
    }

    fun edit() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            _state.value = ViewModelState.Editing
        }
    }

    fun save() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            saveCashback()
            _state.value = ViewModelState.Viewing
        }
    }


    private suspend fun saveCashback() {
        if (cashback.value.bankCard.id == 0L) return

        when (cashbackId) {
            null -> addCashback(cashback.value.mapToCashback())
            else -> updateCashback(cashback.value.mapToCashback())
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


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val cashbackCategoryUseCase: CashbackCategoryUseCase,
        private val cashbackShopUseCase: CashbackShopUseCase,
        private val editCashbackUseCase: EditCashbackUseCase,
        private val id: Long?,
        private val parentId: Long,
        private val parentName: String,
        private val isEdit: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CashbackViewModel(
                cashbackCategoryUseCase,
                cashbackShopUseCase,
                editCashbackUseCase,
                id,
                parentId,
                parentName,
                isEdit
            ) as T
        }
    }
}