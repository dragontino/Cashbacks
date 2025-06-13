package com.cashbacks.features.home.impl.utils

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutorScope
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.home.impl.mvi.HomeAction
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal inline fun CoroutineExecutorScope<*, *, HomeAction, *>.launchWithLoading(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit
) {
    launch(context) {
        forwardFromAnotherThread(HomeAction.StartLoading)
        block()
        forwardFromAnotherThread(HomeAction.FinishLoading)
    }
}