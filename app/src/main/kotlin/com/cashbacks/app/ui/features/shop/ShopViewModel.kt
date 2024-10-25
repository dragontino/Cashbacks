package com.cashbacks.app.ui.features.shop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.app.model.ShopError
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.shop.mvi.ShopAction
import com.cashbacks.app.ui.features.shop.mvi.ShopEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryNotSelectedException
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.FullCategory
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.GetShopUseCase
import com.cashbacks.domain.usecase.shops.UpdateShopUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ShopViewModel @AssistedInject constructor(
    private val fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getShopUseCase: GetShopUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val updateShopUseCase: UpdateShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val messageHandler: MessageHandler,
    @Assisted private val initialShopId: Long?,
    @Assisted private val initialIsEditing: Boolean
) : MviViewModel<ShopAction, ShopEvent>() {

    var state by mutableStateOf(ScreenState.Loading)
        private set

    var viewModelState by mutableStateOf(
        when {
            initialIsEditing || initialShopId == null -> ViewModelState.Editing
            else -> ViewModelState.Viewing
        }
    )

    internal val shop by lazy { ComposableShop(id = initialShopId) }

    var showCategoriesSelection by mutableStateOf(false)
        private set

    var isCreatingCategory by mutableStateOf(false)
        private set

    var selectedCashbackIndex: Int? by mutableStateOf(null)
        private set

    var showErrors by mutableStateOf(false)
        private set


    val categoriesStateFlow: StateFlow<List<Category>?> by lazy {
        fetchCategoriesUseCase.fetchAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
    }


    override suspend fun bootstrap() {
        if (initialShopId != null) {
            fetchCashbacksUseCase.fetchCashbacksFromShop(initialShopId) { throwable ->
                messageHandler.getExceptionMessage(throwable)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { push(ShopEvent.ShowSnackbar(it)) }
            }.onEach {
                shop.cashbacks = it
            }.launchIn(viewModelScope)


            state = ScreenState.Loading
            delay(250)
            getShopUseCase.getShopById(initialShopId)
                .onSuccess { shop.update(it) }
                .onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(ShopEvent.ShowSnackbar(it)) }
                }
            state = ScreenState.Showing
        }
    }


    override suspend fun actor(action: ShopAction) {
        when (action) {
            is ShopAction.ClickButtonBack -> push(ShopEvent.NavigateBack)

            is ShopAction.Edit -> viewModelState = ViewModelState.Editing

            is ShopAction.Save -> {
                if (shop.haveChanges) {
                    showErrors = true
                    shop.updateErrorMessages(messageHandler)

                    if (shop.haveErrors) {
                        shop.errorMessage?.let { push(ShopEvent.ShowSnackbar(it)) }
                    } else {
                        state = ScreenState.Loading
                        delay(300)

                        saveShop(shop)
                            .onSuccess { shop.id = it }
                            .onFailure { throwable ->
                                messageHandler.getExceptionMessage(throwable)
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { push(ShopEvent.ShowSnackbar(it)) }
                            }
                        state = ScreenState.Showing
                    }
                }

                viewModelState = ViewModelState.Viewing
            }

            is ShopAction.Delete -> {
                state = ScreenState.Loading
                delay(200)

                if (shop.id == null) {
                    return action.onSuccess()
                }

                val shop = shop.mapToShop()
                deleteShop(shop)
                    .onSuccess { action.onSuccess() }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(ShopEvent.ShowSnackbar(it)) }
                    }
                delay(100)
                state = ScreenState.Showing
            }

            is ShopAction.CreateCashback -> {
                push(
                    event = ShopEvent.NavigateToCashback(CashbackArgs.fromShop(shop.id))
                )
            }

            is ShopAction.NavigateToCashback -> {
                val event = when (val shopId = shop.id) {
                    null -> ShopEvent.ShowSnackbar("Необходимо сначала сохранить магазин!")
                    else -> ShopEvent.NavigateToCashback(
                        args = CashbackArgs.fromShop(action.cashbackId, shopId)
                    )
                }
                push(event)
            }

            is ShopAction.DeleteCashback -> {
                state = ScreenState.Loading
                delay(100)
                deleteCashback(action.cashback).onFailure { throwable ->
                    messageHandler.getExceptionMessage(throwable)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { push(ShopEvent.ShowSnackbar(it)) }
                }
                delay(100)
                state = ScreenState.Showing
            }

            is ShopAction.SwipeCashback -> {
                selectedCashbackIndex = action.position.takeIf { action.isOpened }
            }


            is ShopAction.StartCreatingCategory -> isCreatingCategory = true

            is ShopAction.CancelCreatingCategory -> isCreatingCategory = false

            is ShopAction.AddCategory -> {
                isCreatingCategory = false
                state = ScreenState.Loading
                delay(100)
                addCategory(action.name)
                    .onSuccess {
                        shop.apply {
                            ::parentCategory updateTo BasicCategory(id = it, name = action.name)
                        }
                    }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { ShopEvent.ShowSnackbar(it) }
                    }
                state = ScreenState.Showing
            }

            is ShopAction.ShowCategoriesSelection -> showCategoriesSelection = true

            is ShopAction.HideCategoriesSelection -> showCategoriesSelection = false

            is ShopAction.OpenDialog -> push(ShopEvent.ChangeOpenedDialog(action.type))

            is ShopAction.CloseDialog -> push(ShopEvent.ChangeOpenedDialog(null))
        }
    }


    internal fun updateShopErrorMessage(error: ShopError) {
        shop.updateErrorMessage(error, messageHandler)
    }


    private suspend fun addCategory(name: String): Result<Long> {
        return addCategoryUseCase.addCategory(FullCategory(name = name))
    }


    private suspend fun saveShop(shop: ComposableShop): Result<Long> {
        val categoryShop = shop.mapToCategoryShop()
            ?: return Result.failure(CategoryNotSelectedException)

        return when (shop.id) {
            null -> addShop(categoryShop)
            else -> updateShop(categoryShop).map { categoryShop.id }
        }
    }


    private suspend fun addShop(shop: CategoryShop): Result<Long> {
        return addShopUseCase.addShop(shop)
    }


    private suspend fun updateShop(shop: CategoryShop): Result<Unit> {
        return updateShopUseCase.updateShop(shop)
    }


    private suspend fun deleteShop(shop: Shop): Result<Unit> {
        return deleteShopUseCase.deleteShop(shop)
    }



    private suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        return deleteCashbacksUseCase.deleteCashback(cashback)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted shopId: Long?,
            @Assisted isEditing: Boolean
        ): ShopViewModel
    }
}