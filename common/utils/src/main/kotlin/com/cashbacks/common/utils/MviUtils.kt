package com.cashbacks.common.utils

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapperScope
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutorScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <Action : Any> CoroutineBootstrapperScope<Action>.dispatchFromAnotherThread(
    action: Action
) = withContext(Dispatchers.Main) {
    dispatch(action)
}

suspend fun <Message : Any> CoroutineExecutorScope<*, Message, *, *>.dispatchFromAnotherThread(
    message: Message
) = withContext(Dispatchers.Main) {
    dispatch(message)
}

suspend fun <Action : Any> CoroutineExecutorScope<*, *, Action, *>.forwardFromAnotherThread(
    action: Action
) = withContext(Dispatchers.Main) {
    forward(action)
}

suspend fun <Label : Any> CoroutineExecutorScope<*, *, *, Label>.publishFromAnotherThread(
    label: Label
) = withContext(Dispatchers.Main) {
    publish(label)
}