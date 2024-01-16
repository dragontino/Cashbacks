package com.cashbacks.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.model.ComposableList
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.EditCategoryUseCase
import com.cashbacks.domain.usecase.ShopUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryInfoViewModel(
    private val categoryUseCase: EditCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val shopUseCase: ShopUseCase,
    private val cashbackUseCase: CashbackCategoryUseCase
    ) : ViewModel() {
    enum class ViewModelState {
        Loading,
        Ready
    }

    private val _state = mutableStateOf(ViewModelState.Ready)
    val state: State<ViewModelState> = derivedStateOf { _state.value }

    val idFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    private val _category: MutableState<ComposableCategory?> = mutableStateOf(null)
    val category: State<ComposableCategory?> = derivedStateOf { _category.value }

    val isEditing = mutableStateOf(false)

    val addingShopState = mutableStateOf(false)

    val showFab = derivedStateOf {
        isEditing.value && state.value != ViewModelState.Loading && !addingShopState.value
    }

    init {
        idFlow.map { id ->
            if (id != null) {
                _state.value = ViewModelState.Loading
                delay(250)

                categoryUseCase
                    .getCategoryById(id)
                    .getOrNull()
                    ?.let { _category.value = ComposableCategory(it) }
                    .also { _state.value = ViewModelState.Ready }
            }
        }.launchIn(viewModelScope)
    }


    fun addShop(name: String) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            // TODO: 16.01.2024 изменить на уникальный id
            val newId = category.value?.shops?.maxOf { it.id } ?: 0
            val shop = BasicShop(id = newId + 1, name = name, maxCashback = null)
            _category.value?.shops?.add(shop)
            _state.value = ViewModelState.Ready
        }
    }


    fun saveInfo() {
        viewModelScope.launch {
            val category = _category.value ?: return@launch
            _state.value = ViewModelState.Loading
            delay(100)
            // TODO: 15.01.2024 сделать обработку ошибок
            categoryUseCase.updateCategory(category.mapToCategory())
            saveShops(category.id, category.shops)
            saveCashbacks(category.id, category.cashbacks)
            _state.value = ViewModelState.Ready
        }
    }


    private suspend fun saveShops(categoryId: Long, shops: ComposableList<BasicShop>) {
        val newShops = mutableListOf<BasicShop>()
        val updatedShops = mutableListOf<BasicShop>()
        val deletedShops = mutableListOf<BasicShop>()

        println("info = ${shops.info}")

        shops.forEach {
            when (shops.info[it.id]) {
                ComposableList.ItemStatus.Created -> newShops.add(it)
                ComposableList.ItemStatus.Updated -> updatedShops.add(it)
                ComposableList.ItemStatus.Deleted -> deletedShops.add(it)
                null -> {}
            }
        }
        shopUseCase.addShopsInCategory(categoryId, newShops)
        shopUseCase.updateShopsInCategory(categoryId, updatedShops)
        shopUseCase.deleteShopsFromCategory(categoryId, deletedShops)
    }


    private suspend fun saveCashbacks(categoryId: Long, cashbacks: ComposableList<Cashback>) {
        val newCashbacks = cashbacks.filter { cashbacks.info[it.id] == ComposableList.ItemStatus.Created }
        val updatedCashbacks = cashbacks.filter { cashbacks.info[it.id] == ComposableList.ItemStatus.Updated }
        val deletedCashbacks = cashbacks.filter { cashbacks.info[it.id] == ComposableList.ItemStatus.Deleted }

        cashbackUseCase.addCashbacksToCategory(categoryId, newCashbacks)
        cashbackUseCase.updateCashbacksInCategory(categoryId, updatedCashbacks)
        cashbackUseCase.deleteCashbacksFromCategory(categoryId, deletedCashbacks)
    }


    fun deleteCategory() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            idFlow.value?.let { deleteCategoryUseCase.deleteCategory(it) }
            _state.value = ViewModelState.Ready
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val categoryUseCase: EditCategoryUseCase,
        private val deleteCategoryUseCase: DeleteCategoryUseCase,
        private val shopUseCase: ShopUseCase,
        private val cashbackUseCase: CashbackCategoryUseCase,
        private val id: Long,
        private val isEditing: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryInfoViewModel(
                categoryUseCase,
                deleteCategoryUseCase,
                shopUseCase,
                cashbackUseCase
            ).apply {
                idFlow.value = this@Factory.id
                isEditing.value = this@Factory.isEditing
            } as T
        }
    }
}