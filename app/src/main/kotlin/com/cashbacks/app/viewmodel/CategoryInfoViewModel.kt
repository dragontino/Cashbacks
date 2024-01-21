package com.cashbacks.app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.EditCategoryUseCase
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CategoryInfoViewModel(
    private val editCategoryUseCase: EditCategoryUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    fetchShopsUseCase: FetchShopsUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbackUseCase: CashbackCategoryUseCase,
    val categoryId: Long,
    isEditing: Boolean
    ) : ViewModel() {
    enum class ViewModelState {
        Loading,
        Ready
    }

    private val _categoryState = mutableStateOf(ViewModelState.Loading)
    val categoryState: State<ViewModelState> = derivedStateOf { _categoryState.value }

    private val _shopsState = mutableStateOf(ViewModelState.Loading)
    val shopsState = derivedStateOf { _shopsState.value }

    private val _cashbacksState = mutableStateOf(ViewModelState.Loading)
    val cashbacksState = derivedStateOf { _cashbacksState.value }

    private val _category = mutableStateOf(ComposableCategory())
    val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsUseCase
        .fetchShopsFromCategory(categoryId)
        .onEach { _shopsState.value = ViewModelState.Ready }
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .onEach { _cashbacksState.value = ViewModelState.Ready }
        .asLiveData()

    val isEditing = mutableStateOf(isEditing)

    val addingShopState = mutableStateOf(false)

    val showFab = derivedStateOf {
        this.isEditing.value && categoryState.value != ViewModelState.Loading && !addingShopState.value
    }

    init {
        viewModelScope.launch {
            delay(250)
            editCategoryUseCase
                .getCategoryById(categoryId)
                .getOrNull()
                ?.let { _category.value = ComposableCategory(it) }
            _categoryState.value = ViewModelState.Ready
        }
    }


    fun saveCategory() {
        viewModelScope.launch {
            val category = _category.value
            if (category.isChanged) {
                _categoryState.value = ViewModelState.Loading
                delay(100)
                editCategoryUseCase.updateCategory(category.mapToCategory())
                delay(100)
                _categoryState.value = ViewModelState.Ready
            }
        }
    }


    fun deleteCategory() {
        viewModelScope.launch {
            _categoryState.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase.deleteCategory(category.value.mapToCategory())
            delay(100)
            _categoryState.value = ViewModelState.Ready
        }
    }


    fun addShop(name: String) {
        viewModelScope.launch {
            _categoryState.value = ViewModelState.Loading
            delay(100)
            val shop = Shop(id = 0, name = name, maxCashback = null)
            addShopUseCase.addShopToCategory(categoryId, shop)
            delay(100)
            _categoryState.value = ViewModelState.Ready
        }
    }


    fun deleteShop(shop: Shop, error: (message: String) -> Unit) {
        viewModelScope.launch {
            _categoryState.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(
                categoryId = categoryId,
                shop = shop,
                errorMessage = error
            )
            delay(100)
            _categoryState.value = ViewModelState.Ready
        }
    }


    fun deleteCashback(cashback: Cashback, error: (message: String) -> Unit) {
        viewModelScope.launch {
            _categoryState.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromCategory(categoryId, cashback, error)
            delay(100)
            _categoryState.value = ViewModelState.Ready
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val editCategoryUseCase: EditCategoryUseCase,
        private val addShopUseCase: AddShopUseCase,
        private val deleteCategoryUseCase: DeleteCategoryUseCase,
        private val fetchShopsUseCase: FetchShopsUseCase,
        private val fetchCashbacksUseCase: FetchCashbacksUseCase,
        private val deleteShopUseCase: DeleteShopUseCase,
        private val deleteCashbackUseCase: CashbackCategoryUseCase,
        private val categoryId: Long,
        private val isEditing: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryInfoViewModel(
                editCategoryUseCase,
                addShopUseCase,
                deleteCategoryUseCase,
                fetchShopsUseCase,
                fetchCashbacksUseCase,
                deleteShopUseCase,
                deleteCashbackUseCase,
                categoryId,
                isEditing
            ) as T
        }
    }
}