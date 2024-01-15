package com.cashbacks.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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

    var state by mutableStateOf(ViewModelState.Ready)
        private set

    var id: Long? by mutableStateOf(null)

    var category: ComposableCategory? by mutableStateOf(null)
        private set

    var isEditing by mutableStateOf(false)

    var addingShopState by mutableStateOf(false)

    init {
        snapshotFlow { id }
            .map { id ->
                if (id != null) {
                    state = ViewModelState.Loading
                    delay(250)

                    categoryUseCase
                        .getCategoryById(id)
                        .getOrNull()
                        ?.let { category = ComposableCategory(it) }
                        .also { state = ViewModelState.Ready }
                }
            }
            .launchIn(viewModelScope)
    }


    fun addShop(name: String) {
        viewModelScope.launch {
            state = ViewModelState.Loading
            delay(100)
            val shop = BasicShop(id = 1, name = name, maxCashback = null)
            category?.shops?.add(shop)
            state = ViewModelState.Ready
        }
    }


    fun saveInfo() {
        viewModelScope.launch {
            val category = this@CategoryInfoViewModel.category ?: return@launch
            state = ViewModelState.Loading
            delay(100)
            // TODO: 15.01.2024 сделать обработку ошибок
            categoryUseCase.updateCategory(category.mapToCategory())
            saveShops(category.id, category.shops)
            saveCashbacks(category.id, category.cashbacks)
            state = ViewModelState.Ready
        }
    }


    private suspend fun saveShops(categoryId: Long, shops: ComposableList<BasicShop>) {
        val newShops = mutableListOf<BasicShop>()
        val updatedShops = mutableListOf<BasicShop>()
        val deletedShops = mutableListOf<BasicShop>()

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
            state = ViewModelState.Loading
            delay(100)

            state = ViewModelState.Ready
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
                id = this@Factory.id
                isEditing = this@Factory.isEditing
            } as T
        }
    }
}