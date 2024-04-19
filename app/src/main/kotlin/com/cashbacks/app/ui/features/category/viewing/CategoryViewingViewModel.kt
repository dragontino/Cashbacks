package com.cashbacks.app.ui.features.category.viewing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.features.category.CategoryViewModel
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
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
    deleteShopUseCase: DeleteShopUseCase,
    deleteCashbacksUseCase: DeleteCashbacksUseCase,
    exceptionMessage: AppExceptionMessage,
    @Assisted categoryId: Long
) : CategoryViewModel<Category>(
    deleteShopUseCase = deleteShopUseCase,
    deleteCashbacksUseCase = deleteCashbacksUseCase,
    exceptionMessage = exceptionMessage,
    categoryId = categoryId,
    defaultVMState = ViewModelState.Viewing
) {
    private val _category = mutableStateOf(Category())
    override val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchShopsWithCashbackFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    var selectedShopIndex: Int? by mutableStateOf(null)

    var selectedCashbackIndex: Int? by mutableStateOf(null)
    val fabPaddingDp = mutableFloatStateOf(0f)


    fun onScreenLoading() {
        viewModelScope.launch {
            innerState.value = ViewModelState.Loading
            loadCategoryFromDatabase()
            delay(AnimationDefaults.SCREEN_DELAY_MILLIS.toLong())
            innerState.value = defaultVMState
        }
    }

    private suspend fun loadCategoryFromDatabase() {
        val result = getCategoryUseCase.getCategoryById(categoryId)
        result.getOrNull()?.let { _category.value = it }
        result.exceptionOrNull()
            ?.let(exceptionMessage::getMessage)
            ?.let(::showSnackbar)
    }


    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long): CategoryViewingViewModel
    }
}