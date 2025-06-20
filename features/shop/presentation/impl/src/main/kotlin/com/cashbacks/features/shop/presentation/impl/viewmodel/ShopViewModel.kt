package com.cashbacks.features.shop.presentation.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromShopUseCase
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.usecase.AddCategoryUseCase
import com.cashbacks.features.category.domain.usecase.FetchAllCategoriesUseCase
import com.cashbacks.features.category.presentation.api.resources.CategoryNotSelectedException
import com.cashbacks.features.shop.domain.usecase.AddShopUseCase
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCase
import com.cashbacks.features.shop.domain.usecase.GetShopUseCase
import com.cashbacks.features.shop.domain.usecase.UpdateShopUseCase
import com.cashbacks.features.shop.presentation.api.resources.ShopNameNotSelectedException
import com.cashbacks.features.shop.presentation.impl.mvi.ShopAction
import com.cashbacks.features.shop.presentation.impl.mvi.ShopError
import com.cashbacks.features.shop.presentation.impl.mvi.ShopIntent
import com.cashbacks.features.shop.presentation.impl.mvi.ShopLabel
import com.cashbacks.features.shop.presentation.impl.mvi.ShopMessage
import com.cashbacks.features.shop.presentation.impl.mvi.ShopState
import com.cashbacks.features.shop.presentation.impl.mvi.model.EditableShop
import com.cashbacks.features.shop.presentation.impl.utils.launchWithLoading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(
    private val fetchCashbacksFromShop: FetchCashbacksFromShopUseCase,
    private val fetchAllCategories: FetchAllCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val getShop: GetShopUseCase,
    private val addShop: AddShopUseCase,
    private val updateShop: UpdateShopUseCase,
    private val deleteShop: DeleteShopUseCase,
    private val deleteCashback: DeleteCashbackUseCase,
    private val messageHandler: MessageHandler,
    private val storeFactory: StoreFactory,
    private val initialShopId: Long?,
    private val initialIsEditing: Boolean
) : ViewModel() {

    private val shopStore : Store<ShopIntent, ShopState, ShopLabel> by lazy {
        storeFactory.create(
            name = "ShopStore",
            initialState = ShopState(
                viewModelState = when {
                    initialIsEditing || initialShopId == null -> ViewModelState.Editing
                    else -> ViewModelState.Viewing
                }
            ),
            bootstrapper = coroutineBootstrapper<ShopAction>(Dispatchers.Default) {
                if (initialShopId != null) {
                    fetchCashbacksFromShop(initialShopId)
                        .catch { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                dispatchFromAnotherThread(ShopAction.DisplayMessage(it))
                            }
                        }
                        .onEach { dispatchFromAnotherThread(ShopAction.LoadCashbacks(it)) }
                        .launchIn(this)

                    launchWithLoading {
                        delay(250)
                        getShop(initialShopId)
                            .onSuccess { dispatchFromAnotherThread(ShopAction.LoadShop(it)) }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    dispatchFromAnotherThread(ShopAction.DisplayMessage(it))
                                }
                            }
                    }
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<ShopAction.StartLoading> {
                    dispatch(ShopMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<ShopAction.FinishLoading> {
                    dispatch(ShopMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<ShopAction.DisplayMessage> {
                    publish(ShopLabel.DisplayMessage(it.message))
                }
                onAction<ShopAction.LoadShop> {
                    val editableShop = EditableShop(it.shop)
                    dispatch(ShopMessage.SetInitialShop(editableShop))
                    dispatch(ShopMessage.UpdateShop(editableShop))
                }
                onAction<ShopAction.LoadCashbacks> {
                    dispatch(ShopMessage.UpdateCashbacks(it.cashbacks))
                }

                onIntent<ShopIntent.ClickButtonBack> {
                    publish(ShopLabel.NavigateBack)
                }
                onIntent<ShopIntent.ClickEditButton> {
                    dispatch(ShopMessage.UpdateViewModelState(ViewModelState.Editing))
                }
                onIntent<ShopIntent.Save> { intent ->
                    val state = state()

                    if (state.isShopChanged().not() && state.shop.id != null) {
                        dispatch(ShopMessage.UpdateViewModelState(ViewModelState.Viewing))
                        return@onIntent
                    }

                    val errorMessages = ShopError.entries
                        .mapNotNull { error ->
                            state.shop.getErrorMessage(error)?.let { error to it }
                        }
                        .toMap()

                    when {
                        errorMessages.isNotEmpty() -> {
                            dispatch(ShopMessage.UpdateShowingErrors(true))
                            dispatch(ShopMessage.SetErrorMessages(errorMessages))
                            val message = ShopError.entries.firstNotNullOf { errorMessages[it] }
                            forward(ShopAction.DisplayMessage(message))
                        }

                        state.isShopChanged().not() -> {
                            intent.onSuccess()
                            return@onIntent
                        }

                        else -> launchWithLoading {
                            delay(300)
                            saveShop(state.shop)
                                .onSuccess { newId ->
                                    val newShop = state.shop.copy(id = newId)
                                    dispatchFromAnotherThread(ShopMessage.SetInitialShop(newShop))
                                    dispatchFromAnotherThread(ShopMessage.UpdateShop(newShop))
                                    dispatch(ShopMessage.UpdateViewModelState(ViewModelState.Viewing))
                                    intent.onSuccess()
                                }
                                .onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        forwardFromAnotherThread(ShopAction.DisplayMessage(it))
                                    }
                                }
                        }
                    }
                }
                onIntent<ShopIntent.Delete> { intent ->
                    launchWithLoading {
                        val state = state()
                        delay(200)

                        if (state.shop.id == null) {
                            intent.onSuccess()
                            return@launchWithLoading
                        }

                        val shop = state.shop.mapToShop()
                        deleteShop(shop)
                            .onSuccess { intent.onSuccess() }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forwardFromAnotherThread(ShopAction.DisplayMessage(it))
                                }
                            }
                        delay(100)
                    }
                }
                onIntent<ShopIntent.CreateCashback> {
                    val shopId = state().shop.id
                    publish(ShopLabel.NavigateToCashback(CashbackArgs.fromShop(shopId)))
                }
                onIntent<ShopIntent.NavigateToCashback> {
                    state().shop.id?.let { shopId ->
                        val args = CashbackArgs.fromShop(it.cashbackId, shopId)
                        publish(ShopLabel.NavigateToCashback(args))
                    }
                }

                onIntent<ShopIntent.DeleteCashback> {
                    launchWithLoading {
                        delay(100)
                        deleteCashback(it.cashback).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(ShopAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }

                onIntent<ShopIntent.SwipeCashback> {
                    dispatch(ShopMessage.ChangeSelectedCashbackIndex(it.position))
                }
                onIntent<ShopIntent.StartCreatingCategory> {
                    dispatch(ShopMessage.SetIsCreatingCategory(true))
                }
                onIntent<ShopIntent.CancelCreatingCategory> {
                    dispatch(ShopMessage.SetIsCreatingCategory(false))
                }
                onIntent<ShopIntent.AddCategory> {
                    dispatch(ShopMessage.SetIsCreatingCategory(false))
                    launchWithLoading {
                        delay(100)
                        val newCategory = Category(name = it.name)
                        addCategory(Category(name = it.name))
                            .onSuccess {
                                val shop = state().shop.copy(parentCategory = newCategory)
                                dispatchFromAnotherThread(ShopMessage.UpdateShop(shop))
                            }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forward(ShopAction.DisplayMessage(it))
                                }
                            }
                    }
                }
                var selectionCategoriesJob: Job? = null
                onIntent<ShopIntent.ShowCategoriesSelection> {
                    selectionCategoriesJob = categoriesStateFlow
                        .onEach { dispatchFromAnotherThread(ShopMessage.UpdateSelectionCategories(it)) }
                        .launchIn(this)

                    dispatch(ShopMessage.SetShowingCategoriesSelection(true))
                }
                onIntent<ShopIntent.HideCategoriesSelection> {
                    launch {
                        selectionCategoriesJob?.cancelAndJoin()
                        selectionCategoriesJob = null
                    }
                    dispatch(ShopMessage.SetShowingCategoriesSelection(false))
                }
                onIntent<ShopIntent.OpenDialog> {
                    publish(ShopLabel.ChangeOpenedDialog(it.type))
                }
                onIntent<ShopIntent.CloseDialog> {
                    publish(ShopLabel.ChangeOpenedDialog(null))
                }
                onIntent<ShopIntent.UpdateErrorMessage> {
                    launch {
                        val state = state()
                        if (state.showErrors) {
                            val message = state.shop.getErrorMessage(it.error)
                            dispatchFromAnotherThread(
                                ShopMessage.SetErrorMessage(it.error, message)
                            )
                        }
                    }
                }
            },
            reducer = { msg: ShopMessage ->
                when (msg) {
                    is ShopMessage.UpdateScreenState -> copy(screenState = msg.state)

                    is ShopMessage.ChangeSelectedCashbackIndex -> copy(
                        selectedCashbackIndex = msg.index
                    )

                    is ShopMessage.SetErrorMessage -> copy(
                        errors = msg.message
                            ?.let { errors.plus(msg.error to it) }
                            ?: errors.minus(msg.error)
                    )

                    is ShopMessage.SetErrorMessages -> copy(errors = msg.errors)

                    is ShopMessage.SetInitialShop -> copy(initialShop = msg.shop)

                    is ShopMessage.SetIsCreatingCategory -> copy(
                        isCreatingCategory = msg.isCreatingCategory
                    )

                    is ShopMessage.SetShowingCategoriesSelection -> copy(
                        showCategoriesSelection = msg.isShowing
                    )

                    is ShopMessage.UpdateSelectionCategories -> copy(
                        selectionCategories = msg.categories
                    )

                    is ShopMessage.UpdateCashbacks -> copy(cashbacks = msg.cashbacks)

                    is ShopMessage.UpdateShop -> copy(shop = msg.shop)

                    is ShopMessage.UpdateShowingErrors -> copy(showErrors = msg.showErrors)

                    is ShopMessage.UpdateViewModelState -> copy(viewModelState = msg.state)
                }
            }
        )
    }


    private val categoriesStateFlow: StateFlow<List<Category>?> by lazy {
        fetchAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
    }


    internal val stateFlow: StateFlow<ShopState> by lazy {
        shopStore.stateFlow(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    }


    internal val labelsFlow: Flow<ShopLabel> by lazy { shopStore.labels }


    internal fun sendIntent(intent: ShopIntent) {
        shopStore.accept(intent)
    }





    private fun EditableShop.getErrorMessage(error: ShopError): String? {
        return when (error) {
            ShopError.Parent -> CategoryNotSelectedException
                .takeIf { parentCategory == null }
                ?.getMessage(messageHandler)

            ShopError.Name -> ShopNameNotSelectedException
                .takeIf { name.isBlank() }
                ?.getMessage(messageHandler)
        }
    }


    private suspend fun saveShop(shop: EditableShop): Result<Long> {
        return when (shop.id) {
            null -> addShop(shop.mapToCategoryShop()!!)
            else -> updateShop(shop.mapToCategoryShop()!!).map { shop.id }
        }
    }
}