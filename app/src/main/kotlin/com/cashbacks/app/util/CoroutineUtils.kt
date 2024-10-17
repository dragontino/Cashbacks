package com.cashbacks.app.util

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun supervisorHandler(onError: (Throwable) -> Unit = {}) =
    CoroutineExceptionHandler { _, t -> onError(t) } + SupervisorJob()

suspend fun supervisorLaunch(
    onError: (Throwable) -> Unit = {},
    block: suspend () -> Unit
) = coroutineScope {
    launch(
        context = supervisorHandler(onError),
        block = { block() }
    )
}