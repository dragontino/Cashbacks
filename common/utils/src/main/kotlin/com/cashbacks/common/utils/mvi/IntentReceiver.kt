package com.cashbacks.common.utils.mvi

interface IntentReceiver<Intent : Any> {
    fun sendIntent(intent: Intent, withDelay: Boolean = false)
    fun sendIntent(intents: Sequence<Intent>, withDelay: Boolean = false)
}