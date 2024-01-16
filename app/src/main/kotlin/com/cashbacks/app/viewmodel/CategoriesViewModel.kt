package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CategoriesViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    enum class ViewModelState {
        Loading,
        EmptyList,
        Ready
    }

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _categories = mutableStateOf(listOf<Category>())
    val categories = derivedStateOf { _categories.value }

    val swipedItemIndex = mutableIntStateOf(-1)

    val addingCategoriesState = mutableStateOf(false)

    private val debounceOnClick: MutableStateFlow<(() -> Unit)?> = MutableStateFlow(null)

    init {
        fetchCategoriesUseCase.fetchCategories()
            .onEach {
                _categories.value = it
                _state.value = if (it.isEmpty()) ViewModelState.EmptyList else ViewModelState.Ready
            }
            .launchIn(viewModelScope)

        debounceOnClick
            .debounce(50)
            .onEach { it?.invoke() }
            .launchIn(viewModelScope)
    }

    fun onItemClick(onClick: () -> Unit) {
        debounceOnClick.value = onClick
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            addCategoryUseCase.addCategory(
                Category(id = 0, name = name, maxCashback = null),
            )
            _state.value = ViewModelState.Ready
        }
    }


    fun deleteCategory(category: Category, error: (message: String) -> Unit) {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCategoryUseCase.deleteCategory(category, error)
            delay(100)
            _state.value = ViewModelState.Ready
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val addCategoryUseCase: AddCategoryUseCase,
        private val fetchCategoriesUseCase: FetchCategoriesUseCase,
        private val deleteCategoryUseCase: DeleteCategoryUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(
                addCategoryUseCase,
                fetchCategoriesUseCase,
                deleteCategoryUseCase
            ) as T
        }
    }
}