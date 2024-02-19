package com.cashbacks.app.ui.features.category.viewing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CategoryViewingViewModel @AssistedInject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted val categoryId: Long
    ) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _category = mutableStateOf(Category())
    val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchShopsWithCashbackFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    private val categoryJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        getCategoryUseCase
            .getCategoryById(categoryId)
            .getOrNull()
            ?.let { _category.value = it }
    }


    init {
        viewModelScope.launch {
            delay(AnimationDefaults.ScreenDelayMillis + 40L)
            _state.value = ViewModelState.Editing
        }
    }

    var selectedShopIndex: Int? by mutableStateOf(null)
    var selectedCashbackIndex: Int? by mutableStateOf(null)

    val fabPaddingDp = mutableFloatStateOf(0f)

    override fun onCleared() {
        categoryJob.cancel()
        super.onCleared()
    }


    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShop(shop)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            _state.value = ViewModelState.Viewing
        }
    }


    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase.deleteCashback(cashback)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            _state.value = ViewModelState.Editing
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long): CategoryViewingViewModel
    }
}