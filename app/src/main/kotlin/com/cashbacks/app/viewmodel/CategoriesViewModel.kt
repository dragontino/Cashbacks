package com.cashbacks.app.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ExceptionMapper.getMessage
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CategoriesViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    application: Application
) : AndroidViewModel(application), EventsFlow, DebounceOnClick {

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    val isEditing = mutableStateOf(false)

    private val allCategories = mutableStateOf(listOf<Category>())
    private val categoriesWithCashback = mutableStateOf(listOf<Category>())
    val categories = derivedStateOf {
        when {
            isEditing.value -> allCategories.value
            else -> categoriesWithCashback.value
        }
    }

    val addingCategoriesState = mutableStateOf(false)

    private val _eventsFlow = MutableSharedFlow<ScreenEvents>()
    override val eventsFlow: SharedFlow<ScreenEvents> = _eventsFlow.asSharedFlow()

    private val _debounceOnClick = MutableSharedFlow<OnClick>()
    override val debounceOnClick = _debounceOnClick.asSharedFlow()

    var selectedCategoryIndex by mutableStateOf<Int?>(null)

    init {
        fetchCategoriesUseCase.fetchAllCategories()
            .onEach { allCategories.value = it }
            .launchIn(viewModelScope)

        fetchCategoriesUseCase.fetchCategoriesWithCashback()
            .onEach { categoriesWithCashback.value = it }
            .launchIn(viewModelScope)

        snapshotFlow {
            Triple(allCategories.value, categoriesWithCashback.value, isEditing.value)
        }.onEach {
            _state.value = ListState.Loading
            delay(200)
            _state.value = if (categories.value.isEmpty()) ListState.Empty else ListState.Stable
        }.launchIn(viewModelScope)

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


    fun addCategory(name: String) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            addCategoryUseCase
                .addCategory(Category(id = 0, name = name, maxCashback = null))
                .exceptionOrNull()
                ?.getMessage(getApplication())
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }


    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteCategoryUseCase
                .deleteCategory(category)
                .exceptionOrNull()
                ?.getMessage(getApplication())
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val addCategoryUseCase: AddCategoryUseCase,
        private val fetchCategoriesUseCase: FetchCategoriesUseCase,
        private val deleteCategoryUseCase: DeleteCategoryUseCase,
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(
                addCategoryUseCase,
                fetchCategoriesUseCase,
                deleteCategoryUseCase,
                application
            ) as T
        }
    }
}