package com.cashbacks.features.home.impl.screens.categories

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromCategoryUseCase
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.usecase.AddCategoryUseCase
import com.cashbacks.features.category.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.features.category.domain.usecase.FetchAllCategoriesUseCase
import com.cashbacks.features.category.domain.usecase.FetchCategoriesWithCashbackUseCase
import com.cashbacks.features.category.domain.usecase.SearchCategoriesUseCase
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.CategoriesAction
import com.cashbacks.features.home.impl.mvi.CategoriesIntent
import com.cashbacks.features.home.impl.mvi.CategoriesLabel
import com.cashbacks.features.home.impl.mvi.CategoriesMessage
import com.cashbacks.features.home.impl.mvi.CategoriesState
import com.cashbacks.features.home.impl.mvi.HomeAction
import com.cashbacks.features.home.impl.utils.launchWithLoading
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Stable
@OptIn(FlowPreview::class)
class CategoriesViewModel(
    addCategory: AddCategoryUseCase,
    fetchAllCategories: FetchAllCategoriesUseCase,
    fetchCategoriesWithCashback: FetchCategoriesWithCashbackUseCase,
    getMaxCashbacksFromCategory: GetMaxCashbacksFromCategoryUseCase,
    searchCategories: SearchCategoriesUseCase,
    deleteCategory: DeleteCategoryUseCase,
    storeFactory: StoreFactory,
) : ViewModel() {

    private val store: Store<CategoriesIntent, CategoriesState, CategoriesLabel> by lazy {
        storeFactory.create(
            name = "CategoriesStore",
            autoInit = false,
            initialState = CategoriesState(),
            bootstrapper = coroutineBootstrapper<CategoriesAction>(Dispatchers.Default) {
                val categoriesFlow = combineTransform(
                    flow = fetchAllCategories(),
                    flow2 = fetchCategoriesWithCashback(),
                    flow3 = stateFlow.map { it.appBarState }.distinctUntilChanged(),
                    flow4 = stateFlow.map { it.viewModelState }.distinctUntilChanged()
                ) { allCategories, categoriesWithCashback, appBarState, viewModelState ->
                    dispatchFromAnotherThread(HomeAction.StartLoading)
                    emit(null)
                    delay(200)

                    val resultCategories = when {
                        appBarState is HomeTopAppBarState.Search -> searchCategories(
                            query = appBarState.query,
                            cashbacksRequired = viewModelState == ViewModelState.Viewing
                        ).onFailure { throw it }
                            .getOrNull()

                        viewModelState == ViewModelState.Editing -> allCategories
                        else -> categoriesWithCashback
                    }

                    val categoryCashbacksMap = resultCategories?.associateWith { category ->
                        getMaxCashbacksFromCategory(category.id)
                            .onFailure { throw it }
                            .getOrNull()
                            ?: emptySet()
                    }
                    emit(categoryCashbacksMap)
                    dispatchFromAnotherThread(HomeAction.FinishLoading)
                }

                categoriesFlow
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(HomeAction.DisplayMessage(it))
                        }
                    }
                    .onEach {
                        dispatchFromAnotherThread(
                            CategoriesAction.LoadCategories(it?.toImmutableMap())
                        )
                    }
                    .launchIn(this)
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<HomeAction.StartLoading> {
                    dispatch(CategoriesMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<HomeAction.FinishLoading> {
                    dispatch(CategoriesMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<CategoriesAction.LoadCategories> {
                    dispatch(CategoriesMessage.UpdateCategories(it.categories))
                }
                onAction<HomeAction.DisplayMessage> {
                    publish(CategoriesLabel.DisplayMessage(it.message))
                }

                onIntent<CategoriesIntent.ClickButtonBack> {
                    publish(CategoriesLabel.NavigateBack)
                }
                onIntent<CategoriesIntent.ClickNavigationButton> {
                    publish(CategoriesLabel.OpenNavigationDrawer)
                }
                onIntent<CategoriesIntent.StartEdit> {
                    dispatch(CategoriesMessage.UpdateViewModelState(ViewModelState.Editing))
                }
                onIntent<CategoriesIntent.FinishEdit> {
                    dispatch(CategoriesMessage.UpdateViewModelState(ViewModelState.Viewing))
                }
                onIntent<CategoriesIntent.SwitchEdit> {
                    val newState = when (state().viewModelState) {
                        ViewModelState.Editing -> ViewModelState.Viewing
                        ViewModelState.Viewing -> ViewModelState.Editing
                    }
                    dispatch(CategoriesMessage.UpdateViewModelState(newState))
                }
                onIntent<CategoriesIntent.StartCreatingCategory> {
                    dispatch(CategoriesMessage.UpdateIsCreatingCategory(true))
                }
                onIntent<CategoriesIntent.AddCategory> {
                    dispatch(CategoriesMessage.UpdateIsCreatingCategory(false))
                    launchWithLoading {
                        delay(100)
                        addCategory(Category(name = it.name)).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forward(HomeAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<CategoriesIntent.FinishCreatingCategory> {
                    dispatch(CategoriesMessage.UpdateIsCreatingCategory(false))
                }
                onIntent<CategoriesIntent.NavigateToCategory> {
                    publish(CategoriesLabel.NavigateToCategory(it.args))
                }
                onIntent<CategoriesIntent.NavigateToCashback> {
                    publish(CategoriesLabel.NavigateToCashback(it.args))
                }

                onIntent<CategoriesIntent.DeleteCategory> {
                    launchWithLoading {
                        delay(100)
                        deleteCategory(it.category).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forward(HomeAction.DisplayMessage(it))
                            }
                        }
                    }
                }

                onIntent<CategoriesIntent.OpenDialog> {
                    publish(CategoriesLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<CategoriesIntent.CloseDialog> {
                    publish(CategoriesLabel.ChangeOpenedDialog(null))
                }
                onIntent<CategoriesIntent.ScrollToEnd> {
                    publish(CategoriesLabel.ScrollToEnd)
                }
                onIntent<CategoriesIntent.SwipeCategory> {
                    dispatch(CategoriesMessage.UpdateSelectedCategoryIndex(it.position))
                }
                onIntent<CategoriesIntent.ChangeAppBarState> {
                    dispatch(CategoriesMessage.UpdateAppBarState(it.state))
                }
            },
            reducer = { msg: CategoriesMessage ->
                when (msg) {
                    is CategoriesMessage.UpdateScreenState -> copy(screenState = msg.state)
                    is CategoriesMessage.UpdateViewModelState -> copy(viewModelState = msg.state)
                    is CategoriesMessage.UpdateAppBarState -> copy(appBarState = msg.state)
                    is CategoriesMessage.UpdateCategories -> copy(categories = msg.categories)
                    is CategoriesMessage.UpdateIsCreatingCategory -> copy(isCreatingCategory = msg.isCreatingCategory)
                    is CategoriesMessage.UpdateSelectedCategoryIndex -> copy(selectedCategoryIndex = msg.index)
                }
            }
        )
    }


    private val intentChannel = Channel<CategoriesIntent>()


    internal val stateFlow: StateFlow<CategoriesState> = store.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
    )


    internal val labelFlow: Flow<CategoriesLabel> = store.labels


    init {
        store.init()

        intentChannel.receiveAsFlow()
            .debounce(DEBOUNCE_MILLIS)
            .onEach(store::accept)
            .launchIn(viewModelScope)
    }


    override fun onCleared() {
        intentChannel.close()
        store.dispose()
        super.onCleared()
    }


    internal fun sendIntent(intent: CategoriesIntent, withDebounce: Boolean = true) {
        when {
            withDebounce -> intentChannel.trySend(intent)
            else -> store.accept(intent)
        }
    }


    private companion object {
        const val DEBOUNCE_MILLIS = 50L
    }
}