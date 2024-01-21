package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.EditShopUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShopViewModel(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val editShopUseCase: EditShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbackUseCase: CashbackShopUseCase,
    private val categoryId: Long,
    private val shopId: Long,
    isEditing: Boolean
) : ViewModel() {

    enum class ViewModelState {
        Loading,
        Ready
    }

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    val isEditing = mutableStateOf(isEditing)

    private val _shop = mutableStateOf(ComposableShop())
    val shop = derivedStateOf { _shop.value }

    val showFab = derivedStateOf {
        this.isEditing.value && state.value != ViewModelState.Loading
    }

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromShop(shopId)
        .asLiveData()


    init {
        viewModelScope.launch {
            delay(250)
            editShopUseCase
                .getShopById(shopId)
                .getOrNull()
                ?.let { _shop.value = ComposableShop(it) }
            _state.value = ViewModelState.Ready
        }
    }


    fun saveShop() {
        viewModelScope.launch {
            val shop = shop.value
            if (shop.isChanged) {
                _state.value = ViewModelState.Loading
                delay(100)
                editShopUseCase.updateShopInCategory(categoryId, shop.mapToShop())
                delay(100)
                _state.value = ViewModelState.Ready
            }
        }
    }


    fun deleteShop() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(categoryId, shop.value.mapToShop())
            delay(100)
            _state.value = ViewModelState.Ready
        }
    }


    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromShop(shopId, cashback)
            delay(100)
            _state.value = ViewModelState.Ready
        }
    }



    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val fetchCashbacksUseCase: FetchCashbacksUseCase,
        private val editShopUseCase: EditShopUseCase,
        private val deleteShopUseCase: DeleteShopUseCase,
        private val deleteCashbackUseCase: CashbackShopUseCase,
        private val categoryId: Long,
        private val shopId: Long,
        private val isEditing: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShopViewModel(
                fetchCashbacksUseCase,
                editShopUseCase,
                deleteShopUseCase,
                deleteCashbackUseCase,
                categoryId,
                shopId,
                isEditing
            ) as T
        }
    }
}