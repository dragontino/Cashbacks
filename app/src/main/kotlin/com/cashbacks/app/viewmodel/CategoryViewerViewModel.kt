package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CategoryViewerViewModel(
    private val getCategoryUseCase: GetCategoryUseCase,
    fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbackUseCase: CashbackCategoryUseCase,
    val categoryId: Long
    ) : ViewModel(), EventsFlow, DebounceOnClick {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _category = mutableStateOf(Category())
    val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchShopsWithCashbacksFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    private val categoryJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        getCategoryUseCase
            .getCategoryById(categoryId)
            .getOrNull()
            ?.let { _category.value = it }
    }

    private val _debounceOnClick = MutableSharedFlow<OnClick>()
    override val debounceOnClick = _debounceOnClick.asSharedFlow()

    private val _eventsFlow = MutableSharedFlow<ScreenEvents>()
    override val eventsFlow = _eventsFlow.asSharedFlow()


    init {
        viewModelScope.launch {
            delay(AnimationDefaults.ScreenDelayMillis + 40L)
            _state.value = ViewModelState.Editing
        }

        debounceOnClick
            .debounce(50)
            .onEach { it.invoke() }
            .launchIn(viewModelScope)
    }


    override fun onItemClick(onClick: () -> Unit) {
        viewModelScope.launch {
            _debounceOnClick.emit(onClick)
        }
    }


    override fun onCleared() {
        categoryJob.cancel()
        super.onCleared()
    }

    override fun navigateTo(route: String?) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.Navigate(route))
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

    override fun showSnackbar(message: String) {
        viewModelScope.launch {
            _eventsFlow.emit(ScreenEvents.ShowSnackbar(message))
        }
    }


    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteShopUseCase.deleteShopFromCategory(
                categoryId = categoryId,
                shop = shop,
                errorMessage = ::showSnackbar
            )
            delay(100)
            _state.value = ViewModelState.Viewing
        }
    }


    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbackUseCase.deleteCashbackFromCategory(categoryId, cashback, ::showSnackbar)
            delay(100)
            _state.value = ViewModelState.Editing
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val getCategoryUseCase: GetCategoryUseCase,
        private val fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
        private val fetchCashbacksUseCase: FetchCashbacksUseCase,
        private val deleteShopUseCase: DeleteShopUseCase,
        private val deleteCashbackUseCase: CashbackCategoryUseCase,
        private val categoryId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryViewerViewModel(
                getCategoryUseCase,
                fetchShopsFromCategoryUseCase,
                fetchCashbacksUseCase,
                deleteShopUseCase,
                deleteCashbackUseCase,
                categoryId
            ) as T
        }
    }
}