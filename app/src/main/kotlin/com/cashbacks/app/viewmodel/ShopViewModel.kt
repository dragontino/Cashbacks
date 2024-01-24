package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.app.ui.managment.ViewModelState
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
    val shopId: Long,
    isEditing: Boolean
) : ViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _shop = mutableStateOf(ComposableShop())
    val shop = derivedStateOf { _shop.value }

    val showFab = derivedStateOf { state.value == ViewModelState.Editing }

    private val shopJob = viewModelScope.launch {
        delay(250)
        editShopUseCase
            .getShopById(shopId)
            .getOrNull()
            ?.let { _shop.value = ComposableShop(it) }
        _state.value = if (isEditing) ViewModelState.Editing else ViewModelState.Viewing
    }

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromShop(shopId)
        .asLiveData()


    override fun onCleared() {
        shopJob.cancel()
        super.onCleared()
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
            saveShop()
            _state.value = ViewModelState.Viewing
        }
    }


    private suspend fun saveShop() {
        val shop = shop.value
        if (shop.isChanged) {
            editShopUseCase.updateShopInCategory(categoryId, shop.mapToShop())
        }
    }


    fun deleteShop() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(categoryId, shop.value.mapToShop())
            delay(100)
            _state.value = currentState
        }
    }


    fun deleteCashback(cashback: Cashback, errorMessage: (String) -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromShop(shopId, cashback, errorMessage)
            delay(100)
            _state.value = currentState
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