package com.cashbacks.common.utils.mvi

@Suppress("unused")
@JvmInline
value class IntentSender<Intent : Any>(
    private val lambda: (intents: Sequence<Intent>, withDelay: Boolean) -> Unit
) {
    fun send(vararg intents: Intent) {
        invoke(intents.asSequence(), false)
    }

    fun send(builder: SequenceScope<Intent>.() -> Unit) {
        val intents = sequence(builder)
        invoke(intents, false)
    }


    fun sendWithDelay(vararg intents: Intent) {
        invoke(intents.asSequence(), true)
    }


    fun sendWithDelay(builder: suspend SequenceScope<Intent>.() -> Unit) {
        invoke(sequence(builder), true)
    }


    operator fun invoke(intents: Sequence<Intent>, withDelay: Boolean) {
        lambda(intents, withDelay)
    }
}


fun <Intent : Any> IntentSender() = IntentSender<Intent> { _, _ -> }