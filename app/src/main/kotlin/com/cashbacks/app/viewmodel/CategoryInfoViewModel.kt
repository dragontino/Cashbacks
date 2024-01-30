package com.cashbacks.app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
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
import kotlinx.coroutines.flow.launchIn
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

    private val _vmState = mutableStateOf(ViewModelState.Loading)
    val vmState: State<ViewModelState> = derivedStateOf { _vmState.value }

    private val _shopsState = mutableStateOf(ListState.Loading)
    val shopsState = derivedStateOf { _shopsState.value }

    private val _cashbacksState = mutableStateOf(ListState.Loading)
    val cashbacksState = derivedStateOf { _cashbacksState.value }

    private val _category = mutableStateOf(ComposableCategory())
    val category = derivedStateOf { _category.value }

    private val _shops = mutableStateOf(listOf<Shop>())
    val shops = derivedStateOf { _shops.value }

    private val allShops = mutableStateOf(listOf<Shop>())
    private val shopsWithCashback = mutableStateOf(listOf<Shop>())

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .onEach {
            _cashbacksState.value = if (it.isEmpty()) ListState.Empty else ListState.Stable
        }
        .asLiveData()

    private val categoryJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        editCategoryUseCase
            .getCategoryById(categoryId)
            .getOrNull()
            ?.let { _category.value = ComposableCategory(it) }
        _vmState.value = if (isEditing) ViewModelState.Editing else ViewModelState.Viewing
    }

    val addingShopState = mutableStateOf(false)

    val showFab = derivedStateOf {
        vmState.value == ViewModelState.Editing && !addingShopState.value
    }

    val isEditing get() = vmState.value == ViewModelState.Editing


    init {
        fetchShopsUseCase
            .fetchAllShopsFromCategory(categoryId)
            .onEach { allShops.value = it }
            .launchIn(viewModelScope)

        fetchShopsUseCase
            .fetchShopsWithCashbacksFromCategory(categoryId)
            .onEach { shopsWithCashback.value = it }
            .launchIn(viewModelScope)

        snapshotFlow { vmState.value }
            .onEach {
                _shops.value = when (it) {
                    ViewModelState.Loading -> listOf()
                    ViewModelState.Editing -> allShops.value
                    ViewModelState.Viewing -> shopsWithCashback.value
                }
                _shopsState.value = when {
                    _shops.value.isEmpty() -> ListState.Empty
                    else -> ListState.Stable
                }
            }
            .launchIn(viewModelScope)
    }


    override fun onCleared() {
        categoryJob.cancel()
        super.onCleared()
    }


    fun edit() {
        viewModelScope.launch {
            _vmState.value = ViewModelState.Loading
            delay(300)
            _vmState.value = ViewModelState.Editing
        }
    }

    fun save() {
        viewModelScope.launch {
            _vmState.value = ViewModelState.Loading
            delay(500)
            saveCategory()
            _vmState.value = ViewModelState.Viewing
        }
    }


    private suspend fun saveCategory() {
        val category = _category.value
        if (category.isChanged) {
            editCategoryUseCase.updateCategory(category.mapToCategory())
        }
    }


    fun deleteCategory() {
        viewModelScope.launch {
            val currentState = _vmState.value
            _vmState.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase.deleteCategory(category.value.mapToCategory())
            delay(100)
            _vmState.value = currentState
        }
    }


    fun addShop(name: String) {
        viewModelScope.launch {
            val currentState = _vmState.value
            _vmState.value = ViewModelState.Loading
            delay(100)
            val shop = Shop(id = 0, name = name, maxCashback = null)
            addShopUseCase.addShopToCategory(categoryId, shop)
            delay(100)
            _vmState.value = currentState
        }
    }


    fun deleteShop(shop: Shop, error: (message: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _vmState.value
            _vmState.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(
                categoryId = categoryId,
                shop = shop,
                errorMessage = error
            )
            delay(100)
            _vmState.value = currentState
        }
    }


    fun deleteCashback(cashback: Cashback, error: (message: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _vmState.value
            _vmState.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromCategory(categoryId, cashback, error)
            delay(100)
            _vmState.value = currentState
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