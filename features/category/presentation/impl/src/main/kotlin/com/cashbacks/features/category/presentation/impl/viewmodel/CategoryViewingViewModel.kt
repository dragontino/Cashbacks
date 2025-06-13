package com.cashbacks.features.category.presentation.impl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.cashbacks.common.utils.AnimationDefaults
import com.cashbacks.common.utils.dispatchFromAnotherThread
import com.cashbacks.common.utils.forwardFromAnotherThread
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromShopUseCase
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.usecase.FetchCategoryUseCase
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.api.CategoryTabItemType
import com.cashbacks.features.category.presentation.impl.mvi.CategoryAction
import com.cashbacks.features.category.presentation.impl.mvi.CategoryLabel
import com.cashbacks.features.category.presentation.impl.mvi.CategoryMessage
import com.cashbacks.features.category.presentation.impl.mvi.CategoryViewingState
import com.cashbacks.features.category.presentation.impl.mvi.ViewingIntent
import com.cashbacks.features.category.presentation.impl.mvi.ViewingLabel
import com.cashbacks.features.category.presentation.impl.mvi.ViewingMessage
import com.cashbacks.features.category.presentation.impl.utils.applyCategoryActions
import com.cashbacks.features.category.presentation.impl.utils.applyCommonCategoryIntents
import com.cashbacks.features.category.presentation.impl.utils.launchWithLoading
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCase
import com.cashbacks.features.shop.domain.usecase.FetchShopsFromCategoryUseCase
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CategoryViewingViewModel(
    private val fetchCategory: FetchCategoryUseCase,
    private val fetchShopsFromCategory: FetchShopsFromCategoryUseCase,
    private val fetchCashbacksFromCategory: FetchCashbacksFromCategoryUseCase,
    private val getMaxCashbacksFromShop: GetMaxCashbacksFromShopUseCase,
    private val deleteShop: DeleteShopUseCase,
    private val deleteCashback: DeleteCashbackUseCase,
    private val storeFactory: StoreFactory,
    private val categoryId: Long,
    val startTab: CategoryTabItemType,
) : ViewModel() {
    private val viewingStore: Store<ViewingIntent, CategoryViewingState, ViewingLabel> by lazy {
        storeFactory.create(
            name = "CategoryViewingStore",
            initialState = CategoryViewingState(),
            bootstrapper = coroutineBootstrapper<CategoryAction>(Dispatchers.Default) {
                launch {
                    dispatchFromAnotherThread(CategoryAction.StartLoading)
                    delay(AnimationDefaults.SCREEN_DELAY_MILLIS.toLong())
                    fetchCategory(categoryId)
                        .catch { throwable ->
                            throwable.message
                                ?.takeIf { it.isNotBlank() }
                                ?.let {
                                    dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                                }
                        }
                        .onEach {
                            dispatchFromAnotherThread(CategoryAction.LoadCategory(it))
                            dispatchFromAnotherThread(CategoryAction.FinishLoading)
                        }
                        .launchIn(this)

                    fetchShopsFromCategory(categoryId, cashbacksRequired = true)
                        .catch { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        .onEach { shops ->
                            val shopsWithMaxCashbacks = shops.associateWith {
                                getMaxCashbacksFromShop(it.id).onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                                    }
                                }.getOrNull() ?: emptySet()
                            }
                            dispatchFromAnotherThread(CategoryAction.LoadShops(shopsWithMaxCashbacks))
                        }
                        .launchIn(this)

                    fetchCashbacksFromCategory(categoryId)
                        .catch { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                dispatchFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        .onEach {
                            dispatchFromAnotherThread(CategoryAction.LoadCashbacks(it))
                        }
                        .launchIn(this)
                }
            },
            executorFactory = coroutineExecutorFactory {
                applyCategoryActions()
                applyCommonCategoryIntents()

                onIntent<ViewingIntent.NavigateToCategoryEditing> {
                    val args = CategoryArgs.Editing(id = categoryId, startTab = it.startTab)
                    publish(ViewingLabel.NavigateToCategoryEditingScreen(args))
                }
                onIntent<ViewingIntent.DeleteCashback> {
                    launchWithLoading(Dispatchers.Default) {
                        delay(100)
                        deleteCashback(it.cashback).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<ViewingIntent.DeleteShop> {
                    launchWithLoading(Dispatchers.Default) {
                        delay(100)
                        deleteShop(it.shop).onFailure { throwable ->
                            throwable.message?.takeIf { it.isNotBlank() }?.let {
                                forwardFromAnotherThread(CategoryAction.DisplayMessage(it))
                            }
                        }
                        delay(100)
                    }
                }
                onIntent<ViewingIntent.NavigateToShop> {
                    val args = ShopArgs(id = it.shopId, isEditing = false)
                    publish(CategoryLabel.NavigateToShopScreen(args))
                }
                onIntent<ViewingIntent.NavigateToCashback> {
                    val args = when (val cashbackId = it.cashbackId) {
                        null -> CashbackArgs.fromCategory(categoryId)
                        else -> CashbackArgs.fromCategory(cashbackId, categoryId)
                    }
                    publish(CategoryLabel.NavigateToCashbackScreen(args))
                }
            },
            reducer = { message: ViewingMessage ->
                when (message) {
                    is CategoryMessage.ChangeSelectedCashbackIndex -> copy(selectedCashbackIndex = message.index)
                    is CategoryMessage.ChangeSelectedShopIndex -> copy(selectedShopIndex = message.index)
                    is CategoryMessage.UpdateCashbacks -> copy(cashbacks = message.cashbacks)
                    is CategoryMessage.UpdateCategory -> copy(category = message.category)
                    is CategoryMessage.UpdateScreenState -> copy(screenState = message.state)
                    is CategoryMessage.UpdateShops -> copy(shops = message.shops)
                }
            }
        )
    }

    internal val stateFlow: StateFlow<CategoryViewingState> = viewingStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    internal val labelFlow: Flow<ViewingLabel> by lazy {
        viewingStore.labels
    }

    internal fun sendIntent(intent: ViewingIntent) {
        viewingStore.accept(intent)
    }
}