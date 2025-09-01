package com.cashbacks.features.category.presentation.impl.utils

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapperScope
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutorScope
import com.arkivanov.mvikotlin.extensions.coroutines.ExecutorBuilder
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.category.presentation.impl.mvi.CategoryAction
import com.cashbacks.features.category.presentation.impl.mvi.CategoryIntent
import com.cashbacks.features.category.presentation.impl.mvi.CategoryLabel
import com.cashbacks.features.category.presentation.impl.mvi.CategoryMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal fun ExecutorBuilder<*, in CategoryAction, *, in CategoryMessage, in CategoryLabel>.applyCategoryActions() {
    onAction<CategoryAction.StartLoading> {
        dispatch(CategoryMessage.UpdateScreenState(ScreenState.Loading))
    }
    onAction<CategoryAction.FinishLoading> {
        dispatch(CategoryMessage.UpdateScreenState(ScreenState.Stable))
    }
    onAction<CategoryAction.LoadCategory> {
        dispatch(CategoryMessage.UpdateCategory(it.category))
    }
    onAction<CategoryAction.LoadShops> {
        dispatch(CategoryMessage.UpdateShops(it.shops.toImmutableList()))
    }
    onAction<CategoryAction.LoadCashbacks> {
        dispatch(CategoryMessage.UpdateCashbacks(it.cashbacks.toImmutableList()))
    }
    onAction<CategoryAction.DisplayMessage> {
        publish(CategoryLabel.DisplayMessage(it.message))
    }
}

internal fun ExecutorBuilder<in CategoryIntent, *, *, in CategoryMessage, in CategoryLabel>.applyCommonCategoryIntents() {
    onIntent<CategoryIntent.ClickButtonBack> {
        publish(CategoryLabel.NavigateBack)
    }
    onIntent<CategoryIntent.OpenDialog> {
        publish(CategoryLabel.OpenDialog(it.type))
    }
    onIntent<CategoryIntent.CloseDialog> {
        publish(CategoryLabel.CloseDialog)
    }
    onIntent<CategoryIntent.SwipeShop> {
        dispatch(CategoryMessage.ChangeSelectedShopIndex(it.position))
    }
    onIntent<CategoryIntent.SwipeCashback> {
        dispatch(CategoryMessage.ChangeSelectedCashbackIndex(it.position))
    }
}


internal inline fun CoroutineExecutorScope<*, *, CategoryAction, *>.launchWithLoading(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit
) {
    launch(context) {
        forwardFromAnotherThread(CategoryAction.StartLoading)
        block()
        forwardFromAnotherThread(CategoryAction.FinishLoading)
    }
}


internal inline fun CoroutineBootstrapperScope<CategoryAction>.launchWithLoading(
    crossinline block: suspend () -> Unit
) {
    launch {
        dispatchFromAnotherThread(CategoryAction.StartLoading)
        block()
        dispatchFromAnotherThread(CategoryAction.FinishLoading)
    }
}