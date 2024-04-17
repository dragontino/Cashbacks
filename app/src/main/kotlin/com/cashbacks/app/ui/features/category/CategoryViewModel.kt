package com.cashbacks.app.ui.features.category

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class CategoryViewModel <T : Any> internal constructor(
    fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    protected val exceptionMessage: AppExceptionMessage,
    val categoryId: Long,
    protected val defaultVMState: ViewModelState
) : EventsViewModel() {
    abstract val category: State<T>

    protected val innerState = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { innerState.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchShopsWithCashbackFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    init {
        viewModelScope.launch {
            delay(AnimationDefaults.ScreenDelayMillis + 40L)
        }
    }


    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShop(shop)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            innerState.value = defaultVMState
        }
    }


    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase.deleteCashback(cashback)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            innerState.value = defaultVMState
        }
    }
}