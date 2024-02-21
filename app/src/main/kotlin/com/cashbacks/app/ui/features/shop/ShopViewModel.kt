package com.cashbacks.app.ui.features.shop

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.R
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.EditShopUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShopViewModel @AssistedInject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val editShopUseCase: EditShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted val shopId: Long,
    @Assisted isEditing: Boolean,
    @Assisted application: Application,
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Viewing)
    val state = derivedStateOf { _state.value }

    private val _shop = mutableStateOf(ComposableShop())
    val shop = derivedStateOf { _shop.value }

    private val defaultTitle = application.getString(R.string.shop)

    var title by mutableStateOf(defaultTitle)
        private set

    private val shopJob = viewModelScope.launch {
        delay(250)
        editShopUseCase
            .getShopById(shopId)
            .getOrNull()
            ?.let {
                _shop.value = ComposableShop(it)
                if (!isEditing) {
                    title = it.name
                }
            }
        _state.value = if (isEditing) ViewModelState.Editing else ViewModelState.Viewing
    }

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromShop(shopId)
        .asLiveData()

    val isLoading = derivedStateOf { state.value == ViewModelState.Loading }
    val isEditing = derivedStateOf { state.value == ViewModelState.Editing }

    var selectedCashbackIndex: Int? by mutableStateOf(null)

    val fabPaddingDp = mutableFloatStateOf(0f)

    override fun onCleared() {
        shopJob.cancel()
        super.onCleared()
    }

    fun edit() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            _state.value = ViewModelState.Editing
            title = defaultTitle
        }
    }


    fun save() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            saveShop()
            _state.value = ViewModelState.Viewing
            title = shop.value.name
        }
    }


    suspend fun saveShop() {
        val shop = shop.value
        if (shop.isChanged) {
            editShopUseCase.updateShop(
                shop = shop.mapToShop(),
                errorMessage = ::showSnackbar
            )
        }
    }


    fun deleteShop() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShop(shop = shop.value.mapToShop())
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            _state.value = currentState
        }
    }



    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase.deleteCashback(cashback)
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
            @Assisted shopId: Long,
            isEditing: Boolean,
            application: Application
        ): ShopViewModel
    }
}