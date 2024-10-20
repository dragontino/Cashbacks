package com.cashbacks.app.ui.features.category.viewing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.category.CategoryViewModel
import com.cashbacks.app.ui.features.category.mvi.CategoryAction
import com.cashbacks.app.ui.features.category.mvi.CategoryEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.FullCategory
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

class CategoryViewingViewModel @AssistedInject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    deleteShopUseCase: DeleteShopUseCase,
    deleteCashbacksUseCase: DeleteCashbacksUseCase,
    exceptionMessage: MessageHandler,
    @Assisted categoryId: Long
) : CategoryViewModel(
    deleteShopUseCase = deleteShopUseCase,
    deleteCashbacksUseCase = deleteCashbacksUseCase,
    exceptionMessage = exceptionMessage,
    categoryId = categoryId,
) {
    var category by mutableStateOf(FullCategory())

    override suspend fun bootstrap() {
        state = ScreenState.Loading
        delay(AnimationDefaults.SCREEN_DELAY_MILLIS.toLong())
        getCategoryUseCase
            .fetchCategoryById(categoryId) { throwable ->
                exceptionMessage.getExceptionMessage(throwable)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { push(CategoryEvent.ShowSnackbar(it)) }
            }.collect {
                category = it
                if (state == ScreenState.Loading) {
                    state = ScreenState.Showing
                }
            }
    }


    override suspend fun actor(action: CategoryAction) {
        if (action is CategoryAction.Viewing) {
            when (action) {
                is CategoryAction.Editing -> super.actor(action)
                is CategoryAction.NavigateToCategoryEditing -> {
                    val args = CategoryArgs(id = categoryId, startTab = action.startTab)
                    push(CategoryEvent.NavigateToCategoryEditingScreen(args))
                }
            }
        } else {
            super.actor(action)
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long): CategoryViewingViewModel
    }
}