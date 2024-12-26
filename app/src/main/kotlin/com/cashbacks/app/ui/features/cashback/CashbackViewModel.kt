package com.cashbacks.app.ui.features.cashback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cashbacks.app.model.ComposableCashback
import com.cashbacks.app.mvi.MviViewModel
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.cashback.mvi.CashbackAction
import com.cashbacks.app.ui.features.cashback.mvi.CashbackEvent
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackOwner
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cashbacks.DeleteCashbacksUseCase
import com.cashbacks.domain.usecase.cashbacks.EditCashbackUseCase
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.shops.FetchAllShopsUseCase
import com.cashbacks.domain.usecase.shops.GetShopUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Currency
import java.util.Locale

class CashbackViewModel @AssistedInject constructor(
    private val editCashbackUseCase: EditCashbackUseCase,
    private val deleteCashbacksUseCase: DeleteCashbacksUseCase,
    private val fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getShopUseCase: GetShopUseCase,
    private val fetchAllShopsUseCase: FetchAllShopsUseCase,
    private val fetchBankCardsUseCase: FetchBankCardsUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val messageHandler: MessageHandler,
    @Assisted("cashback") private val initialCashbackId: Long?,
    @Assisted val ownerType: CashbackOwnerType,
    @Assisted("owner") private val initialOwnerId: Long?
) : MviViewModel<CashbackAction, CashbackEvent>() {

    var state by mutableStateOf(ScreenState.Showing)
    internal val cashback = ComposableCashback(ownerType).apply { id = initialCashbackId }

    var showBankCardsSelection by mutableStateOf(false)
        private set

    var showOwnersSelection by mutableStateOf(false)
        private set

    var isCreatingCategory by mutableStateOf(false)
        private set

    var showErrors by mutableStateOf(false)
        private set


    val ownersStateFlow: StateFlow<List<CashbackOwner>?> by lazy {
        val baseFlow = when (ownerType) {
            CashbackOwnerType.Category -> fetchCategoriesUseCase.fetchAllCategories()
            CashbackOwnerType.Shop -> fetchAllShopsUseCase.fetchAllShops()
        }
        return@lazy baseFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = null
        )
    }


    val bankCardsStateFlow: StateFlow<List<BasicBankCard>?> by lazy {
        fetchBankCardsUseCase.fetchAllBankCards().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = null
        )
    }


    val measureUnitsStateFlow: StateFlow<ListState<MeasureUnit>> by lazy {
        flow {
            delay(200)
            val units = buildList {
                add(MeasureUnit.Percent)
                getLocalCurrencies().forEach {
                    add(MeasureUnit.Currency(it))
                }
            }
            emit(ListState.Stable(units))

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ListState.Loading
        )
    }


    private suspend fun getLocalCurrencies(locale: Locale = Locale.getDefault()): List<Currency> =
        suspendCancellableCoroutine { continuation ->
            val localeCurrency = Currency.getInstance(locale)
            val resultCurrencies = buildSet {
                listOf("EUR", "USD").forEach {
                    add(Currency.getInstance(it))
                }
                add(localeCurrency)
            }.sortedBy { it.getDisplayName(locale) }

            continuation.resumeWith(Result.success(resultCurrencies))
        }


    override suspend fun bootstrap() {
        state = ScreenState.Loading
        delay(100)
        when {
            initialCashbackId != null -> {
                getCashback(initialCashbackId)
                    .onSuccess { cashback.updateCashback(it) }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(CashbackEvent.ShowSnackbar(it)) }
                    }
            }

            initialOwnerId != null -> {
                getOwner(type = ownerType, ownerId = initialOwnerId)
                    .onSuccess { cashback.owner = it }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(CashbackEvent.ShowSnackbar(it)) }
                    }
            }
        }
        state = ScreenState.Showing
    }


    private suspend fun getCashback(id: Long): Result<FullCashback> {
        return editCashbackUseCase.getCashbackById(id)
    }


    private suspend fun getOwner(type: CashbackOwnerType, ownerId: Long): Result<CashbackOwner> {
        return when (type) {
            CashbackOwnerType.Category -> getCategoryUseCase.getCategoryById(ownerId)
            CashbackOwnerType.Shop -> getShopUseCase.getShopById(ownerId)
        }
    }


    override suspend fun actor(action: CashbackAction) {
        when (action) {
            is CashbackAction.ClickButtonBack -> push(CashbackEvent.NavigateBack)

            is CashbackAction.UpdateCashbackErrorMessage -> {
                cashback.updateErrorMessage(action.error, messageHandler)
            }

            is CashbackAction.StartCreatingCategory -> isCreatingCategory = true

            is CashbackAction.CancelCreatingCategory -> isCreatingCategory = false

            is CashbackAction.AddCategory -> {
                isCreatingCategory = false
                state = ScreenState.Loading
                delay(100)
                addCategory(action.name)
                    .onSuccess {
                        cashback.apply {
                            ::owner updateTo BasicCategory(id = it, name = action.name)
                        }
                    }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(CashbackEvent.ShowSnackbar(it)) }
                    }
                state = ScreenState.Showing
            }

            is CashbackAction.CreateShop -> push(CashbackEvent.NavigateToShop(ShopArgs()))

            is CashbackAction.CreateBankCard -> push(
                event = CashbackEvent.NavigateToBankCard(BankCardArgs())
            )

            is CashbackAction.SaveData -> {
                showErrors = true
                cashback.updateAllErrors(messageHandler)

                when {
                    cashback.haveErrors -> cashback.errorMessage?.let {
                        return push(CashbackEvent.ShowSnackbar(it))
                    }

                    cashback.haveChanges.not() -> return action.onSuccess()

                    else -> {
                        state = ScreenState.Loading
                        delay(300)
                        saveCashback(cashback = cashback.mapToCashback()!!)
                            .onSuccess { action.onSuccess() }
                            .onFailure { throwable ->
                                messageHandler.getExceptionMessage(throwable)
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { push(CashbackEvent.ShowSnackbar(it)) }
                            }
                        state = ScreenState.Showing
                    }
                }
            }

            is CashbackAction.DeleteData -> {
                state = ScreenState.Loading
                delay(100)
                val cashback = cashback.mapToCashback() ?: return action.onSuccess()

                deleteCashback(cashback)
                    .onSuccess { action.onSuccess() }
                    .onFailure { throwable ->
                        messageHandler.getExceptionMessage(throwable)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { push(CashbackEvent.ShowSnackbar(it)) }
                    }

                delay(100)
                state = ScreenState.Showing
            }

            is CashbackAction.ShowOwnersSelection -> showOwnersSelection = true

            is CashbackAction.HideOwnersSelection -> showOwnersSelection = false

            is CashbackAction.ShowBankCardsSelection -> showBankCardsSelection = true

            is CashbackAction.HideBankCardsSelection -> showBankCardsSelection = false

            is CashbackAction.HideAllSelections -> {
                showOwnersSelection = false
                showBankCardsSelection = false
            }

            is CashbackAction.ShowDialog -> push(CashbackEvent.OpenDialog(action.type))

            is CashbackAction.HideDialog -> push(CashbackEvent.CloseDialog)
        }
    }


    private suspend fun addCategory(name: String): Result<Long> {
        return addCategoryUseCase.addCategory(BasicCategory(name = name))
    }


    private suspend fun saveCashback(cashback: FullCashback): Result<Unit> {
        return when (cashback.owner) {
            is Category -> saveCashbackInCategory(cashback.owner.id, cashback)
            is Shop -> saveCashbackInShop(cashback.owner.id, cashback)
        }
    }


    private suspend fun saveCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        return when (initialCashbackId) {
            null -> editCashbackUseCase.addCashbackToCategory(categoryId, cashback)
            else -> editCashbackUseCase.updateCashbackInCategory(categoryId, cashback)
        }.map {}
    }


    private suspend fun saveCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return when (initialCashbackId) {
            null -> editCashbackUseCase.addCashbackToShop(shopId, cashback)
            else -> editCashbackUseCase.updateCashbackInShop(shopId, cashback)
        }.map {}
    }


    private suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        return deleteCashbacksUseCase.deleteCashback(cashback)
    }


    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("cashback") id: Long?,
            @Assisted ownerType: CashbackOwnerType,
            @Assisted("owner") ownerId: Long?,
        ): CashbackViewModel
    }
}