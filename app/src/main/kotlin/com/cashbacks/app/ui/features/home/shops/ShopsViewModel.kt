package com.cashbacks.app.ui.features.home.shops

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchAllShopsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShopsViewModel @Inject constructor(
    fetchAllShopsUseCase: FetchAllShopsUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val exceptionMessage: AppExceptionMessage,
) : EventsViewModel() {

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    val isEditing = mutableStateOf(false)

    private val allShops = mutableStateOf(listOf<Pair<Category, Shop>>())
    private val shopsWithCashback = mutableStateOf(listOf<Pair<Category, Shop>>())
    var shops by mutableStateOf(listOf<Pair<Category, Shop>>())
        private set

    var selectedShopIndex by mutableStateOf<Int?>(null)

    init {
        fetchAllShopsUseCase.fetchAllShops()
            .onEach { allShops.value = it }
            .launchIn(viewModelScope)

        fetchAllShopsUseCase.fetchShopsWithCashback()
            .onEach { shopsWithCashback.value = it }
            .launchIn(viewModelScope)

        snapshotFlow {
            Triple(allShops.value, shopsWithCashback.value, isEditing.value)
        }.onEach {
            _state.value = ListState.Loading
            delay(200)
            shops = when {
                isEditing.value -> allShops.value
                else -> shopsWithCashback.value
            }
            _state.value = if (shops.isEmpty()) ListState.Empty else ListState.Stable
        }.launchIn(viewModelScope)
    }

    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteShopUseCase
                .deleteShop(shop)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }
}