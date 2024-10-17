package com.cashbacks.app.ui.features.category.editing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.category.CategoryViewModel
import com.cashbacks.app.ui.features.category.mvi.CategoryAction
import com.cashbacks.app.ui.features.category.mvi.CategoryEvent
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.MessageHandler
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

class CategoryEditingViewModel @AssistedInject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    private val fetchCashbacksUseCase: FetchCashbacksUseCase,
    deleteShopUseCase: DeleteShopUseCase,
    deleteCashbacksUseCase: DeleteCashbacksUseCase,
    exceptionMessage: MessageHandler,
    @Assisted categoryId: Long,
) : CategoryViewModel(
    deleteShopUseCase = deleteShopUseCase,
    deleteCashbacksUseCase = deleteCashbacksUseCase,
    exceptionMessage = exceptionMessage,
    categoryId = categoryId
) {
    val category by lazy { ComposableCategory(id = categoryId) }

    private val _isCreatingShop = mutableStateOf(false)
    val isCreatingShop = derivedStateOf { _isCreatingShop.value }

    override suspend fun bootstrap() {
        fetchShopsFromCategoryUseCase.fetchAllShopsFromCategory(categoryId)
            .onEach { category.shops = it }
            .launchIn(viewModelScope)

        fetchCashbacksUseCase.fetchCashbacksFromCategory(categoryId)
            .onEach { category.cashbacks = it }
            .launchIn(viewModelScope)

        loadContent {
            delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
            getCategoryUseCase.getCategoryById(categoryId)
                .onSuccess { category.update(it) }
                .onFailure { throwable ->
                exceptionMessage.getExceptionMessage(throwable)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { push(CategoryEvent.ShowSnackbar(it)) }
            }
        }
    }


    override suspend fun actor(action: CategoryAction) {
        when (action) {
            is CategoryAction.NavigateToCategoryViewing -> {
                push(
                    event = CategoryEvent.NavigateToCategoryViewingScreen(
                        args = CategoryArgs(id = categoryId, startTab = action.startTab)
                    )
                )
            }

            is CategoryAction.SaveCategory -> {
                loadContent {
                    delay(300)
                    saveCategory(category)
                        .onSuccess { action.onSuccess() }
                        .onFailure { throwable ->
                            exceptionMessage.getExceptionMessage(throwable)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { push(CategoryEvent.ShowSnackbar(it)) }
                        }
                }
            }

            is CategoryAction.DeleteCategory -> {
                loadContent {
                    delay(100)
                    deleteCategory(category.mapToCategory())
                        .onSuccess { action.onSuccess() }
                        .onFailure { throwable ->
                            exceptionMessage.getExceptionMessage(throwable)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { push(CategoryEvent.ShowSnackbar(it)) }
                        }
                    delay(100)
                }
            }

            is CategoryAction.SaveShop -> {
                loadContent {
                    delay(100)
                    saveShop(name = action.name)
                        .onSuccess { push(CategoryAction.FinishCreateShop) }
                        .onFailure { throwable ->
                            exceptionMessage.getExceptionMessage(throwable)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { push(CategoryEvent.ShowSnackbar(it)) }
                        }
                    delay(100)
                }
            }

            is CategoryAction.StartCreateShop -> _isCreatingShop.value = true

            is CategoryAction.FinishCreateShop -> _isCreatingShop.value = false

            else -> super.actor(action)
        }
    }


    private suspend fun saveCategory(category: ComposableCategory): Result<Unit> {
        return when {
            category.haveChanges -> updateCategory(category.mapToCategory())
            else -> Result.success(Unit)
        }
    }

    private suspend fun updateCategory(category: Category): Result<Unit> {
        return updateCategoryUseCase.updateCategory(category)
    }


    private suspend fun deleteCategory(category: Category): Result<Unit> {
         return deleteCategoryUseCase.deleteCategory(category)
    }


    private suspend fun saveShop(name: String): Result<Long> {
        val shop = BasicShop(id = Random.nextLong(), name = name)
        return addShopUseCase.addShop(categoryId, shop)
    }


    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long): CategoryEditingViewModel
    }
}