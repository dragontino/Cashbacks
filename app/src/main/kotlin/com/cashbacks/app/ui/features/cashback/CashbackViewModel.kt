package com.cashbacks.app.ui.features.cashback

import android.content.res.Resources
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCashback
import com.cashbacks.app.model.ComposableCategoryCashback
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.viewmodel.EventsViewModel
import com.cashbacks.domain.model.AppExceptionMessage
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithOwner
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryCashback
import com.cashbacks.domain.model.CategoryNotSelectedException
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopCashback
import com.cashbacks.domain.model.ShopNotSelectedException
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.EditCashbackUseCase
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.shops.FetchAllShopsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CashbackViewModel @AssistedInject constructor(
    private val editCashbackUseCase: EditCashbackUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val fetchAllShopsUseCase: FetchAllShopsUseCase,
    private val fetchBankCardsUseCase: FetchBankCardsUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val exceptionMessage: AppExceptionMessage,
    @Assisted("cashback") internal val cashbackId: Long?,
    @Assisted internal val ownerType: CashbackOwner,
    @Assisted("owner") internal val ownerId: Long?
) : EventsViewModel() {

    private val _state = mutableStateOf(ViewModelState.Loading)
    val state = derivedStateOf { _state.value }

    private val _cashback = mutableStateOf<CashbackWithOwner?>(null)
    val cashback = derivedStateOf {
        _cashback.value?.let(::ComposableCashback) ?: ComposableCashback(ownerType)
    }

    var showBankCardsSelection by mutableStateOf(false)
    var showOwnersSelection by mutableStateOf(false)

    var addingCategoryState by mutableStateOf(false)

    var showErrors by mutableStateOf(false)
        private set


    init {
        viewModelScope.launch {
            if (cashbackId != null) {
                delay(100)
                editCashbackUseCase
                    .getCashbackById(cashbackId)
                    .getOrNull()
                    ?.let { _cashback.value = it }
            }
            _state.value = ViewModelState.Viewing
        }
    }

    fun getAllCategories(): LiveData<List<Category>> {
        return fetchCategoriesUseCase.fetchAllCategories().asLiveData(timeoutInMs = 200)
    }

    fun getAllShops(): LiveData<List<Shop>> {
        return fetchAllShopsUseCase.fetchAllShops()
            .map { it.map(CategoryShop::asShop) }
            .asLiveData(timeoutInMs = 200)
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val currentState = state.value
            _state.value = ViewModelState.Loading
            delay(100)
            val result = addCategoryUseCase.addCategory(Category(name = name))
            result.exceptionOrNull()?.let(exceptionMessage::getMessage)?.let(::showSnackbar)
            result.getOrNull()?.let {
                val newCategory = Category(id = it, name = name)
                with(cashback.value as ComposableCategoryCashback) {
                    updateValue(::category, newCategory)
                }
            }
            _state.value = currentState
        }
    }

    fun getAllBankCards(): LiveData<List<BasicBankCard>> {
        return fetchBankCardsUseCase
            .fetchBankCards()
            .map { it.map(BankCard::getBasicInfo) }
            .asLiveData(timeoutInMs = 200)
    }


    fun saveInfo(resources: Resources, onSuccess: () -> Unit) {
        showErrors = true
        cashback.value.updateErrors(resources)

        if (cashback.value.haveErrors) {
            cashback.value.errorMessage?.let(::showSnackbar)
            return
        }

        viewModelScope.launch {
            _state.value = ViewModelState.Loading
            delay(300)
            val result = saveCashback()
            _state.value = ViewModelState.Viewing
            result.exceptionOrNull()?.let(exceptionMessage::getMessage)?.let(::showSnackbar)
            if (result.isSuccess) onSuccess()
        }
    }


    suspend fun saveCashback(): Result<Unit> {
        val cashback = cashback.value.mapToCashback()

        when (_cashback.value) {
            is CategoryCashback -> {
                val categoryId = ownerId ?: return Result.failure(CategoryNotSelectedException)
                return saveCashbackInCategory(categoryId, cashback)
            }
            is ShopCashback -> {
                val shopId = ownerId ?: return Result.failure(ShopNotSelectedException)
                return saveCashbackInShop(shopId, cashback)
            }
            null -> return Result.success(Unit)
        }
    }


    private suspend fun saveCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        return when (cashbackId) {
            null -> editCashbackUseCase.addCashbackToCategory(categoryId, cashback)
            else -> editCashbackUseCase.updateCashbackInCategory(categoryId, cashback)
        }.map {}
    }


    private suspend fun saveCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return when (cashbackId) {
            null -> editCashbackUseCase.addCashbackToShop(shopId, cashback)
            else -> editCashbackUseCase.updateCashbackInShop(shopId, cashback)
        }.map {}
    }


    fun deleteCashback() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = ViewModelState.Loading
            delay(100)
            deleteCashbacksUseCase
                .deleteCashback(cashback.value.mapToCashback())
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
            @Assisted("cashback") id: Long?,
            @Assisted ownerType: CashbackOwner,
            @Assisted("owner") ownerId: Long?,
        ): CashbackViewModel
    }
}