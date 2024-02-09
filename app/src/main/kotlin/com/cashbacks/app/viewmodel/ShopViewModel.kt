package com.cashbacks.app.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.EditShopUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ShopViewModel @AssistedInject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val editShopUseCase: EditShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbackUseCase: CashbackShopUseCase,
    @Assisted("category") private val categoryId: Long,
    @Assisted("shop") val shopId: Long,
    @Assisted isEditing: Boolean,
    @Assisted application: Application
) : AndroidViewModel(application), EventsFlow, DebounceOnClick {

    private val _eventsFlow = MutableSharedFlow<ScreenEvents>()
    override val eventsFlow = _eventsFlow.asSharedFlow()

    private val _state = mutableStateOf(ViewModelState.Viewing)
    val state = derivedStateOf { _state.value }

    private val _shop = mutableStateOf(ComposableShop())
    val shop = derivedStateOf { _shop.value }

    var title by mutableStateOf(application.getString(AppScreens.Shop.titleRes!!))
        private set

    private val shopJob = viewModelScope.launch {
        delay(250)
        editShopUseCase
            .getShopById(shopId)
            .getOrNull()
            ?.let { _shop.value = ComposableShop(it) }
        _state.value = if (isEditing) ViewModelState.Editing else ViewModelState.Viewing
    }

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromShop(shopId)
        .asLiveData()

    val isLoading = derivedStateOf { state.value == ViewModelState.Loading }
    val isEditing = derivedStateOf { state.value == ViewModelState.Editing }


    private val _debounceOnClick = MutableSharedFlow<OnClick>()
    override val debounceOnClick = _debounceOnClick.asSharedFlow()

    override fun onItemClick(onClick: OnClick) {
        viewModelScope.launch {
            _debounceOnClick.emit(onClick)
        }
    }


    init {
        flow {
            emit(ViewModelState.Loading)
            delay(200)
            emit(if (isEditing) ViewModelState.Editing else ViewModelState.Viewing)
        }.onEach {
            _state.value = it
            when (it) {
                ViewModelState.Editing -> title = application.getString(AppScreens.Shop.titleRes!!)
                ViewModelState.Viewing -> title = shop.value.name
                ViewModelState.Loading -> {}
            }
        }.launchIn(viewModelScope)

        debounceOnClick
            .debounce(50)
            .onEach { it.invoke() }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        shopJob.cancel()
        super.onCleared()
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

    override fun navigateTo(route: String?) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.Navigate(route))
        }
    }


    override fun showSnackbar(message: String) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.ShowSnackbar(message))
        }
    }

    fun edit() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            _state.value = ViewModelState.Editing
            title = getApplication<Application>().getString(AppScreens.Shop.titleRes!!)
        }
    }


    fun save() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            saveShop()
            _state.value = ViewModelState.Viewing
            title = shop.value.name
        }
    }


    private suspend fun saveShop() {
        val shop = shop.value
        if (shop.isChanged) {
            editShopUseCase.updateShopInCategory(categoryId, shop.mapToShop())
        }
    }


    fun deleteShop() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(categoryId, shop.value.mapToShop())
            delay(100)
            _state.value = currentState
        }
    }



    fun deleteCashback(cashback: Cashback, errorMessage: (String) -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromShop(shopId, cashback, errorMessage)
            delay(100)
            _state.value = currentState
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("category") categoryId: Long,
            @Assisted("shop") shopId: Long,
            isEditing: Boolean,
            application: Application
        ): ShopViewModel
    }
}