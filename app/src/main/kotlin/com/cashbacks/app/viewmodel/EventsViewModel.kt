package com.cashbacks.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
abstract class EventsViewModel : ViewModel(), EventsFlow, DebounceOnClick {
    private val _eventsFlow = MutableSharedFlow<ScreenEvents>()
    override val eventsFlow: SharedFlow<ScreenEvents> = _eventsFlow.asSharedFlow()

    private val _debounceOnClick: MutableStateFlow<OnClick?> = MutableStateFlow(null)
    final override val debounceOnClick = _debounceOnClick.asStateFlow()

    init {
        debounceOnClick
            .debounce(50)
            .onEach { it?.invoke() }
            .launchIn(viewModelScope)
    }


    override fun onItemClick(onClick: () -> Unit) {
        viewModelScope.launch {
            _debounceOnClick.emit(onClick)
        }
    }

    override fun openDialog(type: DialogType) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.OpenDialog(type))
        }
    }

    override fun closeDialog() {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.CloseDialog)
        }
    }

    override fun navigateTo(args: Any?) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.Navigate(args))
        }
    }

    override fun showSnackbar(message: String) {
        viewModelScope.launch {
            message
                .takeIf { it.isNotBlank() }
                ?.let { _eventsFlow.emit(ScreenEvents.ShowSnackbar(it)) }
        }
    }
}