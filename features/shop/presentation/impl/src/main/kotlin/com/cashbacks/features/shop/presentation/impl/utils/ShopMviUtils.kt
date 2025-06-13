package com.cashbacks.features.shop.presentation.impl.utils

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapperScope
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutorScope
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.shop.presentation.impl.mvi.ShopAction
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal inline fun CoroutineExecutorScope<*, *, ShopAction, *>.launchWithLoading(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit
) {
    launch(context) {
        forwardFromAnotherThread(ShopAction.StartLoading)
        block()
        forwardFromAnotherThread(ShopAction.FinishLoading)
    }
}


internal inline fun CoroutineBootstrapperScope<ShopAction>.launchWithLoading(
    crossinline block: suspend () -> Unit
) {
    launch {
        dispatchFromAnotherThread(ShopAction.StartLoading)
        block()
        dispatchFromAnotherThread(ShopAction.FinishLoading)
    }
}