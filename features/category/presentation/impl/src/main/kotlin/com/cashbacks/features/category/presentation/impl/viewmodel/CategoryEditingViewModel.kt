package com.cashbacks.features.category.presentation.impl.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.utils.AnimationDefaults
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromShopUseCase
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.features.category.domain.usecase.GetCategoryUseCase
import com.cashbacks.features.category.domain.usecase.UpdateCategoryUseCase
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.api.CategoryTabItemType
import com.cashbacks.features.category.presentation.api.resources.IncorrectCategoryNameException
import com.cashbacks.features.category.presentation.impl.mvi.CategoryAction
import com.cashbacks.features.category.presentation.impl.mvi.CategoryEditingState
import com.cashbacks.features.category.presentation.impl.mvi.CategoryError
import com.cashbacks.features.category.presentation.impl.mvi.CategoryLabel
import com.cashbacks.features.category.presentation.impl.mvi.CategoryMessage
import com.cashbacks.features.category.presentation.impl.mvi.EditingAction
import com.cashbacks.features.category.presentation.impl.mvi.EditingIntent
import com.cashbacks.features.category.presentation.impl.mvi.EditingLabel
import com.cashbacks.features.category.presentation.impl.mvi.EditingMessage
import com.cashbacks.features.category.presentation.impl.utils.applyCategoryActions
import com.cashbacks.features.category.presentation.impl.utils.applyCommonCategoryIntents
import com.cashbacks.features.category.presentation.impl.utils.launchWithLoading
import com.cashbacks.features.shop.domain.model.BasicShop
import com.cashbacks.features.shop.domain.usecase.AddShopUseCase
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCase
import com.cashbacks.features.shop.domain.usecase.FetchShopsFromCategoryUseCase
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CategoryEditingViewModel(
    private val getCategory: GetCategoryUseCase,
    private val addShop: AddShopUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val fetchShopsFromCategory: FetchShopsFromCategoryUseCase,
    private val fetchCashbacksFromCategory: FetchCashbacksFromCategoryUseCase,
    private val getMaxCashbacksFromShop: GetMaxCashbacksFromShopUseCase,
    private val deleteShop: DeleteShopUseCase,
    private val deleteCashback: DeleteCashbackUseCase,
    private val storeFactory: StoreFactory,
    private val messageHandler: MessageHandler,
    private val categoryId: Long,
    private val stateHandle: SavedStateHandle,
    val startTab: CategoryTabItemType
) : ViewModel() {

    private companion object {
        const val CATEGORY_NAME_SAVED_KEY = "CategoryName"
    }

    private val editingStore: Store<EditingIntent, CategoryEditingState, EditingLabel> by lazy {
        storeFactory.create(
            name = "CategoryEditingStore",
            initialState = CategoryEditingState(),
            bootstrapper = coroutineBootstrapper<EditingAction>(Dispatchers.Default) {
                fetchShopsFromCategory(categoryId)
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                        }
                    }
                    .onEach { shops ->
                        val shopMaxCashbacksMap = shops.associateWith {
                            getMaxCashbacksFromShop(it.id).onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                                }
                            }.getOrNull() ?: emptySet()
                        }
                        dispatchFromAnotherThread(CategoryAction.LoadShops(shopMaxCashbacksMap))
                    }
                    .launchIn(this)

                fetchCashbacksFromCategory(categoryId)
                    .catch { throwable ->
                        throwable.message?.takeIf { it.isNotBlank() }?.let {
                            dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                        }
                    }
                    .onEach { dispatchFromAnotherThread(CategoryAction.LoadCashbacks(it)) }
                    .launchIn(this)

                stateHandle.getStateFlow(CATEGORY_NAME_SAVED_KEY, "")
                    .onEach {
                        val category = Category(categoryId, name = it)
                        dispatchFromAnotherThread(CategoryAction.LoadCategory(category))
                    }
                    .launchIn(this)

                launchWithLoading {
                    delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
                    getCategory(categoryId)
                        .onSuccess {
                            dispatchFromAnotherThread(EditingAction.LoadInitialCategory(it))
                            dispatchFromAnotherThread(CategoryAction.LoadCategory(it))
                        }
                        .onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                applyCategoryActions()

                onAction<EditingAction.LoadInitialCategory> {
                    dispatch(EditingMessage.SetInitialCategory(it.category))
                }

                applyCommonCategoryIntents()

                onIntent<EditingIntent.UpdateCategoryName> {
                    stateHandle[CATEGORY_NAME_SAVED_KEY] = it.name
                }
                onIntent<EditingIntent.NavigateToCategoryViewing> {
                    val args = CategoryArgs.Viewing(categoryId, it.startTab)
                    publish(EditingLabel.NavigateToCategoryViewingScreen(args))
                }
                onIntent<EditingIntent.DeleteCashback> {
                    launchWithLoading {
                        delay(100)
                        deleteCashback(it.cashback).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<EditingIntent.ClickToShop> {
                    val args = ShopArgs(id = it.shopId, isEditing = true)
                    publish(CategoryLabel.NavigateToShopScreen(args))
                }
                onIntent<EditingIntent.ClickToCashback> {
                    val args = CashbackArgs.fromCategory(cashbackId = it.cashbackId, categoryId = categoryId)
                    publish(CategoryLabel.NavigateToCashbackScreen(args))
                }
                onIntent<EditingIntent.CreateCashback> {
                    val args = CashbackArgs.fromCategory(categoryId = categoryId)
                    publish(CategoryLabel.NavigateToCashbackScreen(args))
                }
                onIntent<EditingIntent.DeleteShop> {
                    launchWithLoading {
                        delay(100)
                        deleteShop(it.shop).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<EditingIntent.SaveCategory> { intent ->
                    val state = state()
                    val errorMessages = CategoryError.entries
                        .mapNotNull { error ->
                            state.category.getErrorMessage(error)?.let { error to it }
                        }
                        .toMap()

                    when {
                        errorMessages.isNotEmpty() -> {
                            dispatch(EditingMessage.UpdateShowingErrors(true))
                            dispatch(EditingMessage.SetErrorMessages(errorMessages))
                            val message = CategoryError.entries
                                .firstNotNullOf { errorMessages[it] }
                            forward(CategoryAction.DisplayMessage(message))
                        }

                        state.isCategoryChanged().not() -> {
                            intent.onSuccess()
                            return@onIntent
                        }

                        else -> launchWithLoading {
                            delay(300)
                            updateCategory(state.category)
                                .onSuccess { intent.onSuccess() }
                                .onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        forwardFromAnotherThread(
                                            CategoryAction.DisplayMessage(it)
                                        )
                                    }
                                }
                        }
                    }
                }
                onIntent<EditingIntent.DeleteCategory> { intent ->
                    val state = state()
                    launchWithLoading {
                        delay(100)
                        deleteCategory(state.category)
                            .onSuccess { intent.onSuccess() }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                                }
                            }
                        delay(100)
                    }
                }
                onIntent<EditingIntent.SaveShop> { intent ->
                    launchWithLoading {
                        delay(100)
                        saveShop(name = intent.name).onSuccess {
                            dispatchFromAnotherThread(EditingMessage.FinishCreatingShop)
                        }.onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<EditingIntent.StartCreatingShop> {
                    dispatch(EditingMessage.StartCreatingShop)
                }
                onIntent<EditingIntent.FinishCreatingShop> {
                    dispatch(EditingMessage.FinishCreatingShop)
                }
                onIntent<EditingIntent.UpdateErrorMessage> {
                    val state = state()
                    if (state.showErrors) {
                        val message = state.category.getErrorMessage(it.error)
                        dispatch(EditingMessage.SetErrorMessage(it.error, message))
                    }
                }
            },
            reducer = { message: EditingMessage ->
                when (message) {
                    is CategoryMessage.ChangeSelectedCashbackIndex -> copy(
                        selectedCashbackIndex = message.index
                    )

                    is CategoryMessage.ChangeSelectedShopIndex -> copy(
                        selectedShopIndex = message.index
                    )

                    is CategoryMessage.UpdateScreenState -> copy(screenState = message.state)
                    is EditingMessage.SetInitialCategory -> copy(initialCategory = message.category)
                    is CategoryMessage.UpdateCategory -> copy(category = message.category)
                    is CategoryMessage.UpdateShops -> copy(shops = message.shops)
                    is CategoryMessage.UpdateCashbacks -> copy(cashbacks = message.cashbacks)
                    is EditingMessage.StartCreatingShop -> copy(isCreatingShop = true)
                    is EditingMessage.FinishCreatingShop -> copy(isCreatingShop = false)
                    is EditingMessage.SetErrorMessage -> copy(
                        errors = message.message
                            ?.let { errors.plus(message.error to it) }
                            ?: errors.minus(message.error)
                    )
                    is EditingMessage.SetErrorMessages -> copy(errors = message.errors)
                    is EditingMessage.UpdateShowingErrors -> copy(showErrors = message.showErrors)
                }
            }
        )
    }

    private suspend fun saveShop(name: String): Result<Long> {
        val shop = BasicShop(id = 0, name = name)
        return addShop(categoryId, shop)
    }


    private fun Category.getErrorMessage(error: CategoryError): String? {
        return when (error) {
            CategoryError.Name -> when {
                name.isBlank() -> IncorrectCategoryNameException.getMessage(messageHandler)
                else -> null
            }
        }
    }


    internal val stateFlow = editingStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<EditingLabel> by lazy {
        editingStore.labels
    }

    internal fun sendIntent(intent: EditingIntent) {
        editingStore.accept(intent)
    }
}