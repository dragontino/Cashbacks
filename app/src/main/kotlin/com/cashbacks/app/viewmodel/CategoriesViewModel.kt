package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    val isEditing = mutableStateOf(false)

    val swipedItemIndex = mutableIntStateOf(-1)

    private val allCategories = mutableStateOf(listOf<Category>())
    private val categoriesWithCashback = mutableStateOf(listOf<Category>())
    val categories = derivedStateOf {
        when {
            isEditing.value -> allCategories.value
            else -> categoriesWithCashback.value
        }
    }

    val addingCategoriesState = mutableStateOf(false)

    private val debounceOnClick: MutableSharedFlow<(() -> Unit)> = MutableSharedFlow()

    init {
        /*when {
            isEditing.value -> fetchCategoriesUseCase.fetchAllCategories()
            else -> fetchCategoriesUseCase.fetchCategoriesWithCashback()
        }.onEach {
            _state.value = if (it.isEmpty()) ListState.Empty else ListState.Stable
            _categories.value = it
        }.launchIn(viewModelScope)*/

        fetchCategoriesUseCase.fetchAllCategories()
            .onEach { allCategories.value = it }
            .launchIn(viewModelScope)

        fetchCategoriesUseCase.fetchCategoriesWithCashback()
            .onEach { categoriesWithCashback.value = it }
            .launchIn(viewModelScope)

        snapshotFlow { isEditing.value }
            .onEach {
                _state.value = ListState.Loading
                delay(300)
                _state.value = if (categories.value.isEmpty()) ListState.Empty else ListState.Stable
            }
            .launchIn(viewModelScope)

            debounceOnClick
                .debounce(50)
                .onEach { it.invoke() }
                .launchIn(viewModelScope)
    }

    fun onItemClick(onClick: () -> Unit) {
        viewModelScope.launch {
            debounceOnClick.emit(onClick)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            addCategoryUseCase.addCategory(
                Category(id = 0, name = name, maxCashback = null),
            )
            _state.value = ListState.Stable
        }
    }


    fun deleteCategory(category: Category, error: (message: String) -> Unit) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            deleteCategoryUseCase.deleteCategory(category, error)
            delay(100)
            _state.value = ListState.Stable
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