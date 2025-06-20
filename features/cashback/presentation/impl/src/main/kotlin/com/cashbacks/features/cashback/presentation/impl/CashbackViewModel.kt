package com.cashbacks.features.cashback.presentation.impl

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
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardsUseCase
import com.cashbacks.features.bankcard.presentation.api.emptyBankCardArgs
import com.cashbacks.features.bankcard.presentation.api.resources.BankCardNotSelectedException
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToShopUseCase
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.GetCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMeasureUnitsUseCase
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInShopUseCase
import com.cashbacks.features.cashback.domain.utils.asCashbackOwner
import com.cashbacks.features.cashback.presentation.api.CashbackOwnerType
import com.cashbacks.features.cashback.presentation.api.resources.IncorrectCashbackAmountException
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackAction
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackError
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackIntent
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackLabel
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackMessage
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackState
import com.cashbacks.features.cashback.presentation.impl.mvi.model.EditableCashback
import com.cashbacks.features.cashback.presentation.impl.utils.launchWithLoading
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.usecase.AddCategoryUseCase
import com.cashbacks.features.category.domain.usecase.FetchAllCategoriesUseCase
import com.cashbacks.features.category.domain.usecase.GetCategoryUseCase
import com.cashbacks.features.category.presentation.api.resources.CategoryNotSelectedException
import com.cashbacks.features.shop.domain.usecase.FetchAllShopsUseCase
import com.cashbacks.features.shop.domain.usecase.GetShopUseCase
import com.cashbacks.features.shop.presentation.api.ShopArgs
import com.cashbacks.features.shop.presentation.api.resources.ShopNotSelectedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CashbackViewModel(
    getCashback: GetCashbackUseCase,
    private val addCashbackToCategory: AddCashbackToCategoryUseCase,
    private val addCashbackToShop: AddCashbackToShopUseCase,
    private val updateCashbackInCategory: UpdateCashbackInCategoryUseCase,
    private val updateCashbackInShop: UpdateCashbackInShopUseCase,
    deleteCashback: DeleteCashbackUseCase,
    fetchAllCategories: FetchAllCategoriesUseCase,
    getCategory: GetCategoryUseCase,
    getShop: GetShopUseCase,
    fetchAllShops: FetchAllShopsUseCase,
    fetchBankCards: FetchBankCardsUseCase,
    private val getMeasureUnits: GetMeasureUnitsUseCase,
    private val addCategory: AddCategoryUseCase,
    private val messageHandler: MessageHandler,
    private val stateHandle: SavedStateHandle,
    private val storeFactory: StoreFactory,
    private val ownerType: CashbackOwnerType,
    private val initialOwnerId: Long?,
    private val initialCashbackId: Long?,
) : ViewModel() {

    private companion object {
        const val SAVED_CASHBACK_KEY = "Cashback"
    }

    init {
        stateHandle[SAVED_CASHBACK_KEY] = EditableCashback(ownerType = ownerType)
    }

    private val cashbackStore: Store<CashbackIntent, CashbackState, CashbackLabel> by lazy {
        object : Store<CashbackIntent, CashbackState, CashbackLabel> by storeFactory.create(
            name = "CashbackStore",
            initialState = CashbackState(
                cashback = stateHandle[SAVED_CASHBACK_KEY] ?: EditableCashback(ownerType = ownerType),
                screenState = ScreenState.Loading
            ),
            bootstrapper = coroutineBootstrapper<CashbackAction>(Dispatchers.Default) {
                stateHandle
                    .getStateFlow(
                        key = SAVED_CASHBACK_KEY,
                        initialValue = EditableCashback(ownerType = ownerType)
                    )
                    .onEach { dispatchFromAnotherThread(CashbackAction.UpdateEditableCashback(it)) }
                    .launchIn(this)

                launchWithLoading {
                    delay(AnimationDefaults.SCREEN_DELAY_MILLIS + 40L)
                    when {
                        initialCashbackId != null -> {
                            getCashback(initialCashbackId)
                                .onSuccess {
                                    dispatchFromAnotherThread(CashbackAction.LoadCashback(it))
                                }
                                .onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        dispatchFromAnotherThread(CashbackAction.DisplayMessage(it))
                                    }
                                }
                        }

                        initialOwnerId != null -> {
                            val owner = when (ownerType) {
                                CashbackOwnerType.Category -> getCategory(initialOwnerId).map {
                                    it.asCashbackOwner()
                                }

                                CashbackOwnerType.Shop -> getShop(initialOwnerId).map {
                                    it.asCashbackOwner()
                                }
                            }

                            owner
                                .onSuccess {
                                    dispatchFromAnotherThread(CashbackAction.LoadOwner(it))
                                }
                                .onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        dispatchFromAnotherThread(CashbackAction.DisplayMessage(it))
                                    }
                                }
                        }
                    }
                }
            },
            executorFactory = coroutineExecutorFactory(Dispatchers.Default) {
                onAction<CashbackAction.StartLoading> {
                    dispatch(CashbackMessage.UpdateScreenState(ScreenState.Loading))
                }
                onAction<CashbackAction.FinishLoading> {
                    dispatch(CashbackMessage.UpdateScreenState(ScreenState.Stable))
                }
                onAction<CashbackAction.UpdateEditableCashback> {
                    dispatch(CashbackMessage.UpdateCashback(it.cashback))
                }
                onAction<CashbackAction.LoadCashback> {
                    state().cashback.copyFromCashback(it.cashback)?.let { editableCashback ->
                        dispatch(CashbackMessage.SetInitialCashback(editableCashback))
                        stateHandle[SAVED_CASHBACK_KEY] = editableCashback
                    }
                }
                onAction<CashbackAction.LoadOwner> {
                    val newCashback = state().cashback.copy(owner = it.owner)
                    dispatch(CashbackMessage.SetInitialCashback(newCashback))
                    dispatch(CashbackMessage.UpdateCashback(newCashback))
                }
                onAction<CashbackAction.DisplayMessage> {
                    publish(CashbackLabel.DisplayMessage(it.message))
                }

                onIntent<CashbackIntent.ClickButtonBack> {
                    publish(CashbackLabel.NavigateBack)
                }
                onIntent<CashbackIntent.UpdateErrorMessage> {
                    launch {
                        val state = state()
                        if (state.showErrors) {
                            val message = state.cashback.getErrorMessage(it.error)
                            dispatchFromAnotherThread(
                                CashbackMessage.SetErrorMessage(it.error, message)
                            )
                        }
                    }
                }
                onIntent<CashbackIntent.UpdateCashback> {
                    val currentCashback = state().cashback
                    dispatch(CashbackMessage.UpdateCashback(it.block(currentCashback)))
                }
                onIntent<CashbackIntent.StartCreatingCategory> {
                    dispatch(CashbackMessage.UpdateIsCreatingCategory(true))
                }
                onIntent<CashbackIntent.CancelCreatingCategory> {
                    dispatch(CashbackMessage.UpdateIsCreatingCategory(false))
                }
                onIntent<CashbackIntent.AddCategory> { intent ->
                    dispatch(CashbackMessage.UpdateIsCreatingCategory(false))
                    launchWithLoading {
                        delay(100)
                        addCategory(Category(name = intent.name))
                            .onSuccess { id ->
                                val newCashback = state().cashback.copy(
                                    owner = CashbackOwner.Category(
                                        id = id,
                                        name = intent.name
                                    )
                                )
                                dispatchFromAnotherThread(CashbackMessage.UpdateCashback(newCashback))
                            }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forwardFromAnotherThread(CashbackAction.DisplayMessage(it))
                                }
                            }
                    }
                }
                onIntent<CashbackIntent.CreateShop> {
                    publish(CashbackLabel.NavigateToShop(ShopArgs()))
                }
                onIntent<CashbackIntent.CreateBankCard> {
                    publish(CashbackLabel.NavigateToBankCard(emptyBankCardArgs()))
                }
                onIntent<CashbackIntent.SaveData> { intent ->
                    val state = state()
                    val errorMessages = CashbackError.entries
                        .mapNotNull { error ->
                            state.cashback.getErrorMessage(error)?.let { error to it }
                        }
                        .toMap()

                    when {
                        errorMessages.isNotEmpty() -> {
                            dispatch(CashbackMessage.UpdateShowingErrors(true))
                            dispatch(CashbackMessage.SetErrorMessages(errorMessages))
                            val message = CashbackError.entries.firstNotNullOf { errorMessages[it] }
                            forward(CashbackAction.DisplayMessage(message))
                        }

                        state.isCashbackChanged().not() -> {
                            intent.onSuccess()
                            return@onIntent
                        }

                        else -> launchWithLoading {
                            delay(300)
                            saveCashback(cashback = state.cashback.mapToCashback()!!)
                                .onSuccess {
                                    withContext(Dispatchers.Main) { intent.onSuccess() }
                                }
                                .onFailure { throwable ->
                                    throwable.message?.takeIf { it.isNotBlank() }?.let {
                                        forwardFromAnotherThread(CashbackAction.DisplayMessage(it))
                                    }
                                }
                        }
                    }
                }
                onIntent<CashbackIntent.DeleteData> { intent ->
                    launchWithLoading {
                        delay(100)
                        val cashback = state().cashback.mapToCashback()
                            ?: return@launchWithLoading intent.onSuccess()

                        deleteCashback(cashback)
                            .onSuccess {
                                withContext(Dispatchers.Main) { intent.onSuccess() }
                            }
                            .onFailure { throwable ->
                                throwable.message?.takeIf { it.isNotBlank() }?.let {
                                    forwardFromAnotherThread(CashbackAction.DisplayMessage(it))
                                }
                            }
                        delay(100)
                    }
                }

                var ownersFlowJob: Job? = null
                onIntent<CashbackIntent.ShowOwnersSelection> {
                    dispatch(CashbackMessage.UpdateShowingOwnersSelection(true))
                    ownersFlowJob = ownersStateFlow
                        .onEach { dispatchFromAnotherThread(CashbackMessage.UpdateSelectionOwners(it)) }
                        .launchIn(this)
                }
                onIntent<CashbackIntent.HideOwnersSelection> {
                    dispatch(CashbackMessage.UpdateShowingOwnersSelection(false))
                    launch {
                        ownersFlowJob?.cancelAndJoin()
                        ownersFlowJob = null
                        dispatchFromAnotherThread(CashbackMessage.UpdateSelectionOwners(null))
                    }
                }

                var cardsFlowJob: Job? = null
                onIntent<CashbackIntent.ShowBankCardsSelection> {
                    dispatch(CashbackMessage.UpdateShowingBankCardsSelection(true))
                    cardsFlowJob = bankCardsStateFlow
                        .onEach { dispatchFromAnotherThread(CashbackMessage.UpdateSelectionCards(it)) }
                        .launchIn(this)
                }
                onIntent<CashbackIntent.HideBankCardsSelection> {
                    dispatch(CashbackMessage.UpdateShowingBankCardsSelection(false))
                    launch {
                        cardsFlowJob?.cancelAndJoin()
                        cardsFlowJob = null
                        dispatchFromAnotherThread(CashbackMessage.UpdateSelectionCards(null))
                    }
                }

                onIntent<CashbackIntent.HideAllSelections> {
                    dispatch(CashbackMessage.UpdateShowingOwnersSelection(false))
                    dispatch(CashbackMessage.UpdateShowingBankCardsSelection(false))
                    launch {
                        ownersFlowJob?.cancelAndJoin()
                        ownersFlowJob = null
                    }
                    launch {
                        cardsFlowJob?.cancelAndJoin()
                        cardsFlowJob = null
                    }
                }
                onIntent<CashbackIntent.ShowDialog> {
                    publish(CashbackLabel.UpdateOpenedDialog(it.type))
                }
                onIntent<CashbackIntent.HideDialog> {
                    publish(CashbackLabel.UpdateOpenedDialog(null))
                }
                onIntent<CashbackIntent.ShowKeyboard> {
                    publish(CashbackLabel.ScrollToEnd)
                }
                onIntent<CashbackIntent.ShowMeasureUnitsSelection> {
                    launch {
                        val measureUnits = getMeasureUnits()
                        dispatchFromAnotherThread(
                            CashbackMessage.UpdateSelectionMeasureUnits(measureUnits)
                        )
                    }
                }
            },
            reducer = { msg: CashbackMessage ->
                when (msg) {
                    is CashbackMessage.SetErrorMessage -> copy(
                        errors = msg.message
                            ?.let { errors.plus(msg.error to it) }
                            ?: errors.minus(msg.error)
                    )

                    is CashbackMessage.SetErrorMessages -> copy(errors = msg.errorMessages)

                    is CashbackMessage.SetInitialCashback -> copy(initialCashback = msg.cashback)

                    is CashbackMessage.UpdateCashback -> copy(cashback = msg.cashback)

                    is CashbackMessage.UpdateIsCreatingCategory -> copy(
                        isCreatingCategory = msg.isCreatingCategory
                    )

                    is CashbackMessage.UpdateScreenState -> copy(screenState = msg.state)

                    is CashbackMessage.UpdateShowingBankCardsSelection -> copy(
                        showBankCardsSelection = msg.showSelection
                    )

                    is CashbackMessage.UpdateShowingOwnersSelection -> copy(
                        showOwnersSelection = msg.showSelection
                    )

                    is CashbackMessage.UpdateShowingErrors -> copy(showErrors = msg.showErrors)

                    is CashbackMessage.UpdateSelectionOwners -> copy(selectionOwners = msg.owners)

                    is CashbackMessage.UpdateSelectionCards -> copy(selectionBankCards = msg.cards)

                    is CashbackMessage.UpdateSelectionMeasureUnits -> copy(
                        selectionMeasureUnits = msg.units
                    )
                }
            }
        ) {}
    }


    private val ownersStateFlow: StateFlow<List<CashbackOwner>?> by lazy {
        val baseFlow = when (ownerType) {
            CashbackOwnerType.Category -> fetchAllCategories().map { list ->
                list.map { it.asCashbackOwner() }
            }

            CashbackOwnerType.Shop -> fetchAllShops().map { list ->
                list.map { it.asCashbackOwner() }
            }
        }
        return@lazy baseFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }


    private val bankCardsStateFlow: StateFlow<List<BasicBankCard>?> by lazy {
        fetchBankCards().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }



    internal val stateFlow: StateFlow<CashbackState> = cashbackStore.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )


    internal val labelFlow: Flow<CashbackLabel> by lazy { cashbackStore.labels }

    internal fun sendIntent(intent: CashbackIntent) {
        cashbackStore.accept(intent)
    }



    private suspend fun saveCashback(cashback: FullCashback): Result<Unit> {
        return when (cashback.owner) {
            is CashbackOwner.Category -> saveCashbackInCategory(cashback.owner.id, cashback)
            is CashbackOwner.Shop -> saveCashbackInShop(cashback.owner.id, cashback)
        }
    }


    private suspend fun saveCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        return when (initialCashbackId) {
            null -> addCashbackToCategory(categoryId, cashback)
            else -> updateCashbackInCategory(categoryId, cashback)
        }.map {}
    }


    private suspend fun saveCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return when (initialCashbackId) {
            null -> addCashbackToShop(shopId, cashback)
            else -> updateCashbackInShop(shopId, cashback)
        }.map {}
    }


    private fun EditableCashback.getErrorMessage(error: CashbackError): String? {
        val message = when (error) {
            CashbackError.Owner -> {
                when (owner) {
                    null -> {
                        val exception = when (ownerType) {
                            CashbackOwnerType.Category -> CategoryNotSelectedException
                            CashbackOwnerType.Shop -> ShopNotSelectedException
                        }
                        exception.getMessage(messageHandler)
                    }
                    else -> null
                }
            }

            CashbackError.BankCard -> {
                val card = bankCard
                when {
                    card == null || card.id == 0L -> BankCardNotSelectedException.getMessage(messageHandler)

                    else -> null
                }
            }
            CashbackError.Amount -> {
                val doubleAmount = amount.toDoubleOrNull()
                IncorrectCashbackAmountException.getMessage(messageHandler).takeIf {
                    doubleAmount == null || doubleAmount < 0 ||
                            (doubleAmount > 100 && measureUnit is MeasureUnit.Percent)
                }
            }
        }

        return message
    }
}