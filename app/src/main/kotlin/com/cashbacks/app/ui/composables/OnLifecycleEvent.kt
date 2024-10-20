package com.cashbacks.app.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
inline fun OnLifecycleEvent(
    crossinline onStart: () -> Unit = {},
    crossinline onResume: () -> Unit = {},
    crossinline onStop: () -> Unit = {},
    crossinline onDestroy: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_DESTROY -> onDestroy()
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_STOP -> onStop()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}