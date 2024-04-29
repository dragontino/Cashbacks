package com.cashbacks.app.ui.features.shop

import android.app.Application
import android.content.res.Resources
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableShop
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.R
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryNotSelectedException
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
import kotlinx.coroutines.launch

class ShopViewModel @AssistedInject constructor(
    fetchCashbacksUseCase: FetchCashbacksUseCase,
    private val fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getShopUseCase: GetShopUseCase,
    private val addShopUseCase: AddShopUseCase,
    private val updateShopUseCase: UpdateShopUseCase,
    private val deleteShopUseCase: DeleteShopUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted shopId: Long?,
    @Assisted isEditing: Boolean,
    @Assisted application: Application,
) : EventsViewModel() {
    var shopId: Long? = shopId
        private set

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _shop = mutableStateOf(ComposableShop())
    val shop = derivedStateOf { _shop.value }

    var showCategoriesSelection by mutableStateOf(false)
    var addingCategoryState by mutableStateOf(false)

    private val defaultTitle = application.getString(R.string.shop)

    var title by mutableStateOf(defaultTitle)
        private set

    val cashbacksLiveData = shopId
        ?.let {
            fetchCashbacksUseCase.fetchCashbacksFromShop(it).asLiveData()
        }
        ?: liveData { emit(emptyList()) }

    val isLoading = derivedStateOf { state.value == ViewModelState.Loading }
    val isEditing = derivedStateOf { state.value == ViewModelState.Editing }
    var selectedCashbackIndex: Int? by mutableStateOf(null)

    var showErrors by mutableStateOf(false)

    val fabPaddingDp = mutableFloatStateOf(0f)

    init {
        viewModelScope.launch {
            delay(250)
            if (shopId != null) {
                getShopUseCase
                    .getShopWithCategoryById(shopId)
                    .getOrNull()
                    ?.let {
                        _shop.value = ComposableShop(it)
                        if (!isEditing) {
                            title = it.name
                        }
                    }
            }
            _state.value = when {
                isEditing || shopId == null -> ViewModelState.Editing
                else -> ViewModelState.Viewing
            }
        }
    }

    fun getAllCategories(): LiveData<List<Category>> {
        return fetchCategoriesUseCase.fetchAllCategories().asLiveData(timeoutInMs = 200)
    }


    fun edit() {
        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            _state.value = ViewModelState.Editing
            title = defaultTitle
        }
    }


    fun save(resources: Resources, onSuccess: () -> Unit = {}) {
        showErrors = true
        shop.value.updateErrors(resources)

        if (shop.value.haveErrors) {
            shop.value.errorMessage?.let(::showSnackbar)
            return
        }

        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            val result = saveShop()
            _state.value = ViewModelState.Viewing
            title = shop.value.name
            result.exceptionOrNull()?.let(exceptionMessage::getMessage)?.let(::showSnackbar)
            if (result.isSuccess) onSuccess()
        }
    }


    fun addCashback() {
        shopId?.let { navigateTo(CashbackArgs.Shop.New(it)) }
    }


    fun addCategory(name: String) {
        viewModelScope.launch {
            val currentState = state.value
            _state.value = ViewModelState.Loading
            delay(100)
            val result = addCategoryUseCase.addCategory(Category(name = name))
            result.exceptionOrNull()?.let(exceptionMessage::getMessage)?.let(::showSnackbar)
            result.getOrNull()?.let {
                with(shop.value) {
                    updateValue(::category, Category(id = it, name = name))
                }
            }
            _state.value = currentState
        }
    }


    suspend fun saveShop(): Result<Unit> {
        val shop = shop.value
        val categoryId = shop.category?.id
            ?: return Result.failure(CategoryNotSelectedException)

        if (!shop.haveChanges) return Result.success(Unit)

        return when (shopId) {
            null -> addShop(categoryId)
            else -> updateShopUseCase.updateShop(categoryId, shop.mapToShop())
        }
    }


    private suspend fun addShop(categoryId: Long): Result<Unit> {
        val resultId = addShopUseCase.addShopToCategory(categoryId, shop.value.mapToShop())
        resultId.getOrNull()?.let { shopId = it }
        return resultId.map {}
    }


    fun deleteShop(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(200)
            val result = deleteShopUseCase.deleteShop(shop = shop.value.mapToShop())
            result.exceptionOrNull()?.let(exceptionMessage::getMessage)?.let(::showSnackbar)
            delay(100)
            _state.value = currentState
            if (result.isSuccess) onSuccess()
        }
    }



    fun deleteCashback(cashback: Cashback) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase.deleteCashback(cashback)
                .exceptionOrNull()
                ?.let(exceptionMessage::getMessage)
                ?.let(::showSnackbar)
            delay(100)
            _state.value = currentState
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted shopId: Long?,
            isEditing: Boolean,
            application: Application
        ): ShopViewModel
    }
}