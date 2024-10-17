package com.cashbacks.app.ui.features.home.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.categories.mvi.CategoriesAction
import com.cashbacks.app.ui.features.home.categories.mvi.CategoriesEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.categories.SearchCategoriesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    searchCategoriesUseCase: SearchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val messageHandler: MessageHandler
) : MviViewModel<CategoriesAction, CategoriesEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
        private set

    var viewModelState by mutableStateOf(ViewModelState.Viewing)
        private set

    internal var appBarState by mutableStateOf<HomeTopAppBarState>(HomeTopAppBarState.TopBar)
        private set

    val categoriesFlow: StateFlow<List<BasicCategory>?> by lazy {
        combineTransform(
            flow = fetchCategoriesUseCase.fetchAllCategories(),
            flow2 = fetchCategoriesUseCase.fetchCategoriesWithCashback(),
            flow3 = snapshotFlow { listOf(viewModelState, appBarState) },
        ) { allCategories, categoriesWithCashback, _ ->
            state = ScreenState.Loading
            emit(null)
            delay(200)

            val appBarState = appBarState
            val resultCategories = when {
                appBarState is HomeTopAppBarState.Search -> searchCategoriesUseCase.searchCategories(
                    query = appBarState.query,
                    cashbacksRequired = viewModelState == ViewModelState.Viewing
                )

                viewModelState == ViewModelState.Editing -> allCategories
                else -> categoriesWithCashback
            }

            emit(resultCategories)
            state = ScreenState.Showing

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = null
        )
    }


    var isCreatingCategory by mutableStateOf(false)
        private set

    var selectedCategoryIndex by mutableStateOf<Int?>(null)
        private set


    override suspend fun actor(action: CategoriesAction) {
        when (action) {
            is CategoriesAction.ClickButtonBack -> push(CategoriesEvent.NavigateBack)

            is CategoriesAction.StartEdit -> viewModelState = ViewModelState.Editing


            is CategoriesAction.FinishEdit -> viewModelState = ViewModelState.Viewing

            is CategoriesAction.SwitchEdit -> {
                viewModelState = when (viewModelState) {
                    ViewModelState.Editing -> ViewModelState.Viewing
                    ViewModelState.Viewing -> ViewModelState.Editing
                }
            }

            is CategoriesAction.StartCreatingCategory -> isCreatingCategory = true

            is CategoriesAction.AddCategory -> {
                isCreatingCategory = false
                state = ScreenState.Loading
                delay(100)
                addCategory(action.name).onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(CategoriesEvent.ShowSnackbar(it)) }
                }
                delay(100)
                state = ScreenState.Showing
            }
            is CategoriesAction.FinishCreatingCategory -> isCreatingCategory = false

            is CategoriesAction.NavigateToCategory -> {
                push(CategoriesEvent.NavigateToCategory(action.args, action.isEditing))
            }

            is CategoriesAction.DeleteCategory -> {
                state = ScreenState.Loading
                delay(100)
                deleteCategory(action.category).onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(CategoriesEvent.ShowSnackbar(it)) }
                }
                state = ScreenState.Showing
            }

            is CategoriesAction.OpenDialog -> push(CategoriesEvent.OpenDialog(action.type))

            is CategoriesAction.CloseDialog -> push(CategoriesEvent.CloseDialog)

            is CategoriesAction.ScrollToEnd -> push(CategoriesEvent.ScrollToEnd)

            is CategoriesAction.SwipeCategory -> {
                selectedCategoryIndex = action.position.takeIf { action.isOpened }
            }

            is CategoriesAction.UpdateAppBarState -> {
                appBarState = action.state
            }
        }
    }


    private suspend fun addCategory(name: String): Result<Long> {
        return addCategoryUseCase.addCategory(BasicCategory(name = name))
    }


    private suspend fun deleteCategory(category: Category): Result<Unit> {
        return deleteCategoryUseCase.deleteCategory(category)
    }
}