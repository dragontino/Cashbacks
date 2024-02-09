package com.cashbacks.app.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCategory
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.categories.UpdateCategoryUseCase
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CategoryEditorViewModel @AssistedInject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    fetchShopsFromCategoryUseCase: FetchShopsFromCategoryUseCase,
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbackUseCase: CashbackCategoryUseCase,
    @Assisted application: Application,
    @Assisted val categoryId: Long,
) : AndroidViewModel(application), EventsFlow, DebounceOnClick {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _category = mutableStateOf(ComposableCategory())
    val category = derivedStateOf { _category.value }

    val shopsLiveData = fetchShopsFromCategoryUseCase
        .fetchAllShopsFromCategory(categoryId)
        .asLiveData()

    val cashbacksLiveData = fetchCashbacksUseCase
        .fetchCashbacksFromCategory(categoryId)
        .asLiveData()

    private val categoryJob = viewModelScope.launch {
        delay(AnimationDefaults.ScreenDelayMillis + 40L)
        getCategoryUseCase
            .getCategoryById(categoryId)
            .getOrNull()
            ?.let { _category.value = ComposableCategory(it) }
    }

    private val _debounceOnClick = MutableSharedFlow<OnClick>()
    override val debounceOnClick = _debounceOnClick.asSharedFlow()

    private val _eventsFlow = MutableSharedFlow<ScreenEvents>()
    override val eventsFlow = _eventsFlow.asSharedFlow()

    val addingShopState = mutableStateOf(false)

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

    fun save() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(500)
            if (category.value.isChanged) {
                updateCategoryUseCase.updateCategory(_category.value.mapToCategory())
            }
            _state.value = ViewModelState.Editing
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase
                .deleteCategory(category.value.mapToCategory())
                .exceptionOrNull()
                ?.message
                ?.let(::showSnackbar)
            delay(100)
            _state.value = ViewModelState.Editing
        }
    }

    fun addShop(name: String) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            val shop = Shop(id = 0, name = name, maxCashback = null)
            addShopUseCase.addShopToCategory(categoryId, shop)
            delay(100)
            _state.value = ViewModelState.Editing
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


    @AssistedFactory
    interface Factory {
        fun create(application: Application, categoryId: Long): CategoryEditorViewModel
    }
}