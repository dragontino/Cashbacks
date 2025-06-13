package com.cashbacks.features.cashback.presentation.impl.utils

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapperScope
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutorScope
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackAction
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal inline fun CoroutineExecutorScope<*, *, CashbackAction, *>.launchWithLoading(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit
) {
    launch(context) {
        forwardFromAnotherThread(CashbackAction.StartLoading)
        block()
        forwardFromAnotherThread(CashbackAction.FinishLoading)
    }
}


internal inline fun CoroutineBootstrapperScope<CashbackAction>.launchWithLoading(
    crossinline block: suspend () -> Unit
) {
    launch {
        dispatchFromAnotherThread(CashbackAction.StartLoading)
        block()
        dispatchFromAnotherThread(CashbackAction.FinishLoading)
    }
}