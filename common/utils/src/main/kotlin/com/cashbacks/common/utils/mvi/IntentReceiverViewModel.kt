package com.cashbacks.common.utils.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample

@OptIn(FlowPreview::class)
abstract class IntentReceiverViewModel<Intent : Any>() : ViewModel(), IntentReceiver<Intent> {

    abstract val scope: CoroutineScope

    protected abstract fun acceptIntent(intent: Intent)


    protected val intentSharedFlow = MutableSharedFlow<Sequence<Intent>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    init {
        intentSharedFlow
            .sample(DELAY_MILLIS)
            .flowOn(Dispatchers.Default)
            .onEach { intents -> intents.forEach(::acceptIntent) }
            .launchIn(scope)
    }

    final override fun sendIntent(intent: Intent, withDelay: Boolean) {
        sendIntent(sequenceOf(intent), withDelay)
    }

    final override fun sendIntent(intents: Sequence<Intent>, withDelay: Boolean) {
        when {
            withDelay -> intentSharedFlow.tryEmit(intents)
            else -> intents.forEach(::acceptIntent)
        }
    }

    protected companion object {
        const val DELAY_MILLIS = 50L
    }
}