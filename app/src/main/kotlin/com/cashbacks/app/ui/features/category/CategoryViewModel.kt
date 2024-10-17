package com.cashbacks.app.ui.features.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.mvi.CategoryAction
import com.cashbacks.app.ui.features.category.mvi.CategoryEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import kotlinx.coroutines.delay

abstract class CategoryViewModel internal constructor(
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    protected val exceptionMessage: MessageHandler,
    val categoryId: Long
) : MviViewModel<CategoryAction, CategoryEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        protected set

    var selectedShopIndex: Int? by mutableStateOf(null)
        protected set

    var selectedCashbackIndex: Int? by mutableStateOf(null)
        protected set


    override suspend fun actor(action: CategoryAction) {
        when (action) {
            !is CategoryAction.Viewing -> return
            !is CategoryAction.Editing -> return

            is CategoryAction.ClickButtonBack -> push(CategoryEvent.NavigateBack)

            is CategoryAction.DeleteShop -> {
                loadContent {
                    delay(100)
                    deleteShop(action.shop)
                    delay(100)
                }
            }

            is CategoryAction.DeleteCashback -> {
                loadContent {
                    delay(100)
                    deleteCashback(action.cashback)
                    delay(100)
                }
            }

            is CategoryAction.OpenDialog -> push(CategoryEvent.OpenDialog(action.type))

            CategoryAction.CloseDialog -> push(CategoryEvent.CloseDialog)

            is CategoryAction.NavigateToCashback -> push(
                event = CategoryEvent.NavigateToCashbackScreen(
                    args = when (val cashbackId = action.cashbackId) {
                        null -> CashbackArgs.fromCategory(categoryId)
                        else -> CashbackArgs.fromCategory(cashbackId, categoryId)
                    }
                )
            )

            is CategoryAction.NavigateToShop -> push(CategoryEvent.NavigateToShopScreen(action.args))

            is CategoryAction.SwipeCashback -> {
                selectedCashbackIndex = action.position.takeIf { action.isOpened }
            }

            is CategoryAction.SwipeShop -> {
                selectedShopIndex = action.position.takeIf { action.isOpened }
            }

            else -> {}
        }
    }


    protected inline fun loadContent(onLoad: () -> Unit) {
        state = ScreenState.Loading
        onLoad()
        state = ScreenState.Showing
    }


    private suspend fun deleteShop(shop: BasicShop) {
        deleteShopUseCase.deleteShop(shop).onFailure { throwable ->
            exceptionMessage.getExceptionMessage(throwable)
                ?.takeIf { it.isNotBlank() }
                ?.let { push(CategoryEvent.ShowSnackbar(it)) }
        }
    }


    private suspend fun deleteCashback(cashback: BasicCashback) {
        deleteCashbacksUseCase.deleteCashback(cashback).onFailure { throwable ->
            exceptionMessage.getExceptionMessage(throwable)
                ?.takeIf { it.isNotBlank() }
                ?.let { push(CategoryEvent.ShowSnackbar(it)) }
        }
    }
}