package com.cashbacks.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.BasicInfoCategory
import com.cashbacks.domain.usecase.AddCategoryUseCase
import com.cashbacks.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.FetchCategoriesUseCase
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

    var state by mutableStateOf(ViewModelState.Loading)
        private set

    var categories: List<BasicInfoCategory> by mutableStateOf(listOf())
        private set

    var addingCategoriesState by mutableStateOf(false)

    private val debounceOnClick: MutableStateFlow<(() -> Unit)?> = MutableStateFlow(null)

    init {
        fetchCategoriesUseCase.fetchCategories()
            .onEach {
                categories = it
                state = if (it.isEmpty()) ViewModelState.EmptyList else ViewModelState.Ready
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
            state = ViewModelState.Loading
            delay(100)
            addCategoryUseCase.addCategory(
                BasicCategory(
                    id = 1,
                    name = name,
                    maxCashback = null,
                ),
            )
            state = ViewModelState.Ready
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