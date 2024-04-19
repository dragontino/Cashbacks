package com.cashbacks.app.ui.features.category.editing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.ui.features.category.CategoryViewModel
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
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
    deleteShopUseCase: DeleteShopUseCase,
    deleteCashbacksUseCase: DeleteCashbacksUseCase,
    exceptionMessage: AppExceptionMessage,
    @Assisted categoryId: Long,
) : CategoryViewModel<ComposableCategory>(
    deleteShopUseCase = deleteShopUseCase,
    deleteCashbacksUseCase = deleteCashbacksUseCase,
    exceptionMessage = exceptionMessage,
    categoryId = categoryId,
    defaultVMState = ViewModelState.Editing
) {
    private val _category = mutableStateOf(ComposableCategory())
    override val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchAllShopsFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    val addingShopState = mutableStateOf(false)

    var selectedShopIndex: Int? by mutableStateOf(null)

    var selectedCashbackIndex: Int? by mutableStateOf(null)


    init {
        viewModelScope.launch {
            delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
            val result = getCategoryUseCase.getCategoryById(categoryId)
            result.exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)

            result.getOrNull()?.let {
                _category.value = ComposableCategory(it)
            }

            innerState.value = ViewModelState.Editing
        }
    }


    fun save(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            delay(300)
            val success = when {
                category.value.isChanged -> {
                    updateCategoryUseCase.updateCategory(
                        category = category.value.mapToCategory(),
                        exceptionMessage = {
                            exceptionMessage.getMessage(it)?.let(::showSnackbar)
                        }
                    ).isSuccess
                }

                else -> true
            }
            innerState.value = ViewModelState.Editing
            if (success) onSuccess()
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase
                .deleteCategory(category.value.mapToCategory())
                .exceptionOrNull()
                ?.message
                ?.let(::showSnackbar)
            delay(100)
            innerState.value = ViewModelState.Editing
        }
    }

    fun addShop(name: String) {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            delay(100)
            val shop = Shop(id = 0, name = name, maxCashback = null)
            addShopUseCase.addShopToCategory(categoryId, shop)
            delay(100)
            innerState.value = ViewModelState.Editing
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long): CategoryEditingViewModel
    }
}