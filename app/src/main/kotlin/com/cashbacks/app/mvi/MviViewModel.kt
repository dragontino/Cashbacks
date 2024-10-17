package com.cashbacks.app.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.util.OnClick
import com.cashbacks.app.util.supervisorHandler
import com.cashbacks.app.util.supervisorLaunch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
abstract class MviViewModel<Action : MviAction, Event : MviEvent> : ViewModel() {

    private val eventChannel: Channel<Event> = Channel(Channel.UNLIMITED)
    private val actionChannel: Channel<Action> = Channel(Channel.UNLIMITED)
    private val onClickChannel: Channel<OnClick> = Channel()

    abstract suspend fun actor(action: Action)

    open suspend fun bootstrap() {}


    val eventFlow: Flow<Event> by lazy {
        eventChannel.receiveAsFlow()
            .onStart {
                viewModelScope.launch(Dispatchers.Default + supervisorHandler()) {
                    actionChannel
                        .receiveAsFlow()
                        .onEach { launchActor(it, ::actor) }
                        .retryWhen { _, _ -> true }
                        .launchIn(this)

                    launchBootstrap(::bootstrap)

                    onClickChannel.receiveAsFlow()
                        .debounce(50)
                        .onEach(::launchOnClick)
                        .launchIn(this)

                }
            }
            .flowOn(Dispatchers.Default)
    }


    fun push(action: Action) {
        actionChannel.trySend(action)
    }


    protected fun push(event: Event) {
        eventChannel.trySend(event)
    }


    fun onItemClick(onClick: OnClick) {
        onClickChannel.trySend(onClick)
    }


    private suspend fun launchActor(
        action: Action,
        actor: suspend (Action) -> Unit
    ) = supervisorLaunch(onError = { Log.e("ACTION", it.message, it) }) {
        actor(action)
    }


    private suspend fun launchBootstrap(
        bootstrap: suspend () -> Unit
    ) = supervisorLaunch(onError = { Log.e("BOOTSTRAP", it.message, it) }) {
        bootstrap()
    }


    private suspend fun launchOnClick(onClick: OnClick) =
        supervisorLaunch(onError = { Log.e("ON_CLICK", it.message, it) }) {
            onClick.invoke()
        }
}



interface MviAction

interface MviEvent