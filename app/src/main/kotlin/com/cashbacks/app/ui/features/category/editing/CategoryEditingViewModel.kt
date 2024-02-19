package com.cashbacks.app.ui.features.category.editing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.categories.UpdateCategoryUseCase
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CategoryEditingViewModel @AssistedInject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted val categoryId: Long,
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _category = mutableStateOf(ComposableCategory())
    val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchAllShopsFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    private val categoryJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        getCategoryUseCase
            .getCategoryById(categoryId)
            .getOrNull()
            ?.let { _category.value = ComposableCategory(it) }
    }

    val addingShopState = mutableStateOf(false)

    var selectedShopIndex: Int? by mutableStateOf(null)
    var selectedCashbackIndex: Int? by mutableStateOf(null)


    init {
        viewModelScope.launch {
            delay(AnimationDefaults.ScreenDelayMillis + 40L)
            _state.value = ViewModelState.Editing
        }
    }


    override fun onCleared() {
        categoryJob.cancel()
        super.onCleared()
    }

    fun save() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(500)
            if (category.value.isChanged) {
                updateCategoryUseCase.updateCategory(
                    category = category.value.mapToCategory(),
                    exceptionMessage = {
                        exceptionMessage.getMessage(it)?.let(::showSnackbar)
                    }
                )
            }
            _state.value = ViewModelState.Editing
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase
                .deleteCategory(category.value.mapToCategory())
                .exceptionOrNull()
                ?.message
                ?.let(::showSnackbar)
            delay(100)
            _state.value = ViewModelState.Editing
        }
    }

    fun addShop(name: String) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            val shop = Shop(id = 0, name = name, maxCashback = null)
            addShopUseCase.addShopToCategory(categoryId, shop)
            delay(100)
            _state.value = ViewModelState.Editing
        }
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
        fun create(categoryId: Long): CategoryEditingViewModel
    }
}