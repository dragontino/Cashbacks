package com.cashbacks.app.ui.features.home.categories

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.Search
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.categories.SearchCategoriesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    searchCategoriesUseCase: SearchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val exceptionMessage: AppExceptionMessage
) : EventsViewModel(), Search {

    private val _state = mutableStateOf(ListState.Loading)
    val state = derivedStateOf { _state.value }

    val isEditing = mutableStateOf(false)

    internal var appBarState: HomeTopAppBarState by mutableStateOf(HomeTopAppBarState.TopBar)

    override val query = mutableStateOf("")

    private val allCategories = mutableStateOf(listOf<Category>())
    private val categoriesWithCashback = mutableStateOf(listOf<Category>())

    var categories by mutableStateOf(listOf<Category>())
        private set

    var addingCategoriesState by mutableStateOf(false)

    var selectedCategoryIndex by mutableStateOf<Int?>(null)

    val isSearch: Boolean get() = appBarState == HomeTopAppBarState.Search

    init {
        fetchCategoriesUseCase.fetchAllCategories()
            .onEach { allCategories.value = it }
            .cancellable()
            .launchIn(viewModelScope)

        fetchCategoriesUseCase.fetchCategoriesWithCashback()
            .onEach { categoriesWithCashback.value = it }
            .cancellable()
            .launchIn(viewModelScope)

        snapshotFlow {
            arrayOf(
                allCategories.value,
                categoriesWithCashback.value,
                isEditing.value,
                appBarState,
                query.value
            )
        }.onEach {
            _state.value = ListState.Loading
            delay(200)
            categories = when {
                isSearch -> searchCategoriesUseCase.searchCategories(
                    query = query.value,
                    cashbacksRequired = !isEditing.value
                )
                isEditing.value -> allCategories.value
                else -> categoriesWithCashback.value
            }
            _state.value = if (categories.isEmpty()) ListState.Empty else ListState.Stable
        }.launchIn(viewModelScope)
    }


    fun addCategory(name: String) {
        viewModelScope.launch {
            _state.value = ListState.Loading
            delay(100)
            addCategoryUseCase
                .addCategory(Category(name = name))
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
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
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            _state.value = ListState.Stable
        }
    }
}