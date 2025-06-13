package com.cashbacks.features.cashback.presentation.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.common.composables.DropdownMenuListContent
import com.cashbacks.common.composables.EditableTextField
import com.cashbacks.common.composables.EditableTextFieldDefaults
import com.cashbacks.common.composables.ListDropdownMenu
import com.cashbacks.common.composables.Loading
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.NewNameTextField
import com.cashbacks.common.composables.OnLifecycleEvent
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.composableLet
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.smoothScrollToItem
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.DateUtils
import com.cashbacks.common.utils.DateUtils.getDisplayableString
import com.cashbacks.common.utils.LocalDate
import com.cashbacks.common.utils.epochMillis
import com.cashbacks.common.utils.management.DialogType
import com.cashbacks.common.utils.management.ListState
import com.cashbacks.common.utils.management.ScreenState
import com.cashbacks.common.utils.management.toListState
import com.cashbacks.common.utils.today
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.api.utils.BankCardPresentationUtils.getDisplayableString
import com.cashbacks.features.bankcard.presentation.api.utils.PaymentSystemUtils
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.presentation.api.CashbackOwnerType
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackError
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackIntent
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackLabel
import com.cashbacks.features.cashback.presentation.impl.mvi.CashbackState
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun CashbackRoot(
    navigateToShop: (ShopArgs) -> Unit,
    navigateToCard: (BankCardArgs) -> Unit,
    navigateBack: () -> Unit,
    viewModel: CashbackViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val lazyListState = rememberLazyListState()
    
    val openedDialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is CashbackLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is CashbackLabel.NavigateBack -> navigateBack()
                is CashbackLabel.NavigateToBankCard -> navigateToCard(label.args)
                is CashbackLabel.NavigateToShop -> navigateToShop(label.args)
                
                is CashbackLabel.ScrollToEnd -> lazyListState.smoothScrollToItem(
                    lazyListState.layoutInfo.totalItemsCount
                )

                is CashbackLabel.UpdateOpenedDialog -> openedDialogType.value = label.type
            }
        }
    }


    when (val type = openedDialogType.value) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = stringResource(R.string.confirm_cashback_deletion),
                onConfirm = {
                    viewModel.sendIntent(
                        CashbackIntent.DeleteData {
                            viewModel.sendIntent(CashbackIntent.ClickButtonBack)
                        }
                    )
                },
                onClose = { viewModel.sendIntent(CashbackIntent.HideDialog) }
            )
        }

        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.sendIntent(
                        CashbackIntent.SaveData {
                            viewModel.sendIntent(CashbackIntent.ClickButtonBack)
                        }
                    )
                },
                onDismiss = { viewModel.sendIntent(CashbackIntent.ClickButtonBack) },
                onClose = { viewModel.sendIntent(CashbackIntent.HideDialog) }
            )
        }

        is DatePicker -> {
            DatePickerDialog(
                date = type.date,
                onConfirm = {
                    viewModel.sendIntent(
                        CashbackIntent.UpdateCashback {
                            when (type) {
                                is StartDatePicker -> updateStartDate(it)
                                is EndDatePicker -> copy(expirationDate = it)
                            }
                        }
                    )
                },
                onClose = { viewModel.sendIntent(CashbackIntent.HideDialog) },
                isDateSelectable = { date ->
                    val startDate = state.cashback.startDate ?: Clock.System.today()
                    type is StartDatePicker || date >= startDate
                }
            )
        }

        is CashbackUnitSelection -> {
            UnitSelectionDialog(
                measureUnits = state.selectionMeasureUnits.toListState(),
                selectedUnit = state.cashback.measureUnit,
                onConfirm = {
                    viewModel.sendIntent(
                        CashbackIntent.UpdateCashback { updateMeasureUnit(it) }
                    )
                },
                onClose = { viewModel.sendIntent(CashbackIntent.HideDialog) }
            )
        }
    }
    

    CashbackScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        listState = lazyListState,
        sendIntent = viewModel::sendIntent
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CashbackScreen(
    state: CashbackState,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
    sendIntent: (CashbackIntent) -> Unit,
) {
    val keyboardState = keyboardAsState()
    
    OnLifecycleEvent(
        onDestroy = { sendIntent(CashbackIntent.SaveData()) }
    )

    LaunchedEffect(state.isCreatingCategory) {
        if (state.isCreatingCategory) {
            sendIntent(CashbackIntent.HideAllSelections)
        }
    }

    LaunchedEffect(state.showOwnersSelection) {
        if (state.showOwnersSelection && state.isCreatingCategory) {
            sendIntent(CashbackIntent.CancelCreatingCategory)
        }
    }

    LaunchedEffect(state.showBankCardsSelection) {
        if (state.showBankCardsSelection && state.isCreatingCategory) {
            sendIntent(CashbackIntent.CancelCreatingCategory)
        }
    }

    LaunchedEffect(keyboardState.value) {
        when {
            keyboardState.value -> sendIntent(CashbackIntent.ShowKeyboard)
            else -> sendIntent(CashbackIntent.CancelCreatingCategory)
        }
    }


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {
        CashbackContent(
            state = state,
            lazyListState = listState,
            snackbarHostState = snackbarHostState,
            sendIntent = sendIntent,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = state.screenState != ScreenState.Loading && state.isCreatingCategory,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                sendIntent(CashbackIntent.AddCategory(name))
            }
        }
    }


    BackHandler(enabled = state.isCashbackChanged()) {
        sendIntent(CashbackIntent.ShowDialog(DialogType.Save))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbackContent(
    state: CashbackState,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (CashbackIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val topBarState = rememberTopAppBarState()

    CollapsingToolbarScaffold(
        topBarState = topBarState,
        contentState = lazyListState,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.cashback_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val action = when {
                                state.isCashbackChanged() -> CashbackIntent.ShowDialog(
                                    type = DialogType.Save
                                )

                                else -> CashbackIntent.ClickButtonBack
                            }
                            sendIntent(action)
                        }
                    ) {
                        Icon(
                            imageVector = when {
                                state.isCashbackChanged() -> Icons.Rounded.Close
                                else -> Icons.Rounded.ArrowBackIosNew
                            },
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.cashback.id != null &&
                                state.screenState != ScreenState.Loading
                    ) {
                        IconButton(
                            onClick = {
                                state.cashback.mapToCashback()?.let {
                                    sendIntent(
                                        CashbackIntent.ShowDialog(DialogType.ConfirmDeletion(it))
                                    )
                                }
                            },
                            enabled = state.screenState != ScreenState.Loading
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "delete cashback",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }

                    AnimatedVisibility(visible = state.screenState != ScreenState.Loading) {
                        IconButton(
                            onClick = {
                                sendIntent(
                                    CashbackIntent.SaveData {
                                        sendIntent(CashbackIntent.ClickButtonBack)
                                    }
                                )
                            },
                            enabled = state.screenState != ScreenState.Loading
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "save",
                                modifier = Modifier.scale(1.3f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topBarState.overlappedFraction),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        modifier = modifier
    ) {
        Crossfade(
            targetState = state.screenState,
            label = "loading animation",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Stable -> CashbackFields(state, lazyListState, sendIntent)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbackFields(
    state: CashbackState,
    listState: LazyListState,
    sendIntent: (CashbackIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember(::MutableInteractionSource)
    val verticalPadding = 16.dp
    val horizontalPadding = 16.dp

    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            ExposedDropdownMenuBox(
                expanded = state.showOwnersSelection,
                onExpandedChange = { expanded ->
                    val action = when {
                        expanded -> CashbackIntent.ShowOwnersSelection
                        else -> CashbackIntent.HideOwnersSelection
                    }
                    sendIntent(action)
                },
                modifier = Modifier.padding(horizontal = horizontalPadding)
            ) {
                EditableTextField(
                    text = state.cashback.owner?.name
                        ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = when (state.cashback.ownerType) {
                        CashbackOwnerType.Category -> stringResource(R.string.category_title)
                        CashbackOwnerType.Shop -> stringResource(R.string.shop_title)
                    },
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = state.showErrors && CashbackError.Owner in state.errors,
                    errorMessage = state.errors[CashbackError.Owner],
                    trailingActions = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.showOwnersSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .padding(bottom = verticalPadding)
                        .fillMaxWidth()
                )

                ListDropdownMenu(
                    state = state.selectionOwners.toListState(),
                    expanded = state.showOwnersSelection,
                    onClose = { sendIntent(CashbackIntent.HideOwnersSelection) },
                ) { list ->
                    DropdownMenuListContent(
                        list = list,
                        selected = { state.cashback.owner?.id == it.id },
                        title = { it.name },
                        onClick = {
                            sendIntent(
                                CashbackIntent.UpdateCashback {
                                    copy(owner = it)
                                }
                            )
                            sendIntent(CashbackIntent.UpdateErrorMessage(CashbackError.Owner))
                            sendIntent(CashbackIntent.HideOwnersSelection)
                        },
                        addButton = {
                            TextButton(
                                onClick = {
                                    when (state.cashback.ownerType) {
                                        CashbackOwnerType.Category -> {
                                            sendIntent(CashbackIntent.StartCreatingCategory)
                                        }

                                        CashbackOwnerType.Shop -> {
                                            sendIntent(CashbackIntent.CreateShop)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (state.cashback.ownerType) {
                                        CashbackOwnerType.Category -> stringResource(R.string.add_category)
                                        CashbackOwnerType.Shop -> stringResource(R.string.add_shop)
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            ExposedDropdownMenuBox(
                expanded = state.showBankCardsSelection,
                onExpandedChange = { expanded ->
                    val action = when {
                        expanded -> CashbackIntent.ShowBankCardsSelection
                        else -> CashbackIntent.HideBankCardsSelection
                    }
                    sendIntent(action)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                EditableTextField(
                    text = state.cashback.bankCard?.getDisplayableString()
                        ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = stringResource(R.string.bank_card),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = state.showErrors && CashbackError.BankCard in state.errors,
                    errorMessage = state.errors[CashbackError.BankCard],
                    leadingIcon = state.cashback.bankCard?.paymentSystem?.composableLet {
                        PaymentSystemUtils.PaymentSystemImage(
                            paymentSystem = it,
                            maxWidth = 30.dp,
                            drawBackground = false
                        )
                    },
                    trailingActions = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.showBankCardsSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .padding(vertical = verticalPadding)
                        .fillMaxWidth()
                )

                ListDropdownMenu(
                    state = state.selectionBankCards.toListState(),
                    expanded = state.showBankCardsSelection,
                    onClose = { sendIntent(CashbackIntent.HideBankCardsSelection) }
                ) { cards ->
                    DropdownMenuListContent(
                        list = cards,
                        selected = { state.cashback.bankCard?.id == it.id },
                        leadingIcon = { card ->
                            card.paymentSystem?.let {
                                PaymentSystemUtils.PaymentSystemImage(
                                    paymentSystem = it,
                                    maxWidth = 30.dp,
                                    drawBackground = false
                                )
                            }
                        },
                        title = { it.getDisplayableString() },
                        onClick = {
                            sendIntent(
                                CashbackIntent.UpdateCashback {
                                    copy(bankCard = it)
                                }
                            )
                            sendIntent(CashbackIntent.UpdateErrorMessage(CashbackError.BankCard))
                            sendIntent(CashbackIntent.HideBankCardsSelection)
                        },
                        addButton = {
                            TextButton(
                                onClick = { sendIntent(CashbackIntent.CreateBankCard) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.add_card),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                    )
                }
            }
        }

        item {
            EditableTextField(
                text = state.cashback.amount,
                onTextChange = {
                    sendIntent(
                        CashbackIntent.UpdateCashback { updateAmount(it) }
                    )
                    sendIntent(CashbackIntent.UpdateErrorMessage(CashbackError.Amount))
                },
                trailingActions = {
                    TextButton(
                        onClick = {
                            sendIntent(CashbackIntent.ShowMeasureUnitsSelection)
                            sendIntent(CashbackIntent.ShowDialog(CashbackUnitSelection))
                        }
                    ) {
                        Text(
                            text = state.cashback.measureUnit.getDisplayableString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = EditableTextFieldDefaults.colors(
                    focusedTrailingActionsColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTrailingActionsColor = MaterialTheme.colorScheme.onBackground
                ),
                error = state.showErrors && CashbackError.Amount in state.errors,
                errorMessage = state.errors[CashbackError.Amount],
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .padding(bottom = verticalPadding)
                    .padding(horizontal = horizontalPadding)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.cashback.startDate?.getDisplayableString() ?: "",
                onTextChange = {},
                label = buildString {
                    append(
                        stringResource(R.string.valid),
                        " ",
                        stringResource(R.string.valid_from).lowercase()
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                enabled = false,
                modifier = Modifier
                    .padding(bottom = verticalPadding)
                    .padding(horizontal = horizontalPadding)
                    .clickable(interactionSource, indication = null) {
                        sendIntent(
                            CashbackIntent.ShowDialog(
                                StartDatePicker(state.cashback.startDate)
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.cashback.expirationDate?.getDisplayableString() ?: "",
                onTextChange = {},
                label = buildString {
                    append(
                        stringResource(R.string.valid),
                        " ",
                        stringResource(R.string.valid_through).lowercase()
                    )
                },
                trailingActions = {
                    if (state.cashback.expirationDate != null) {
                        IconButton(
                            onClick = {
                                sendIntent(
                                    CashbackIntent.UpdateCashback { copy(expirationDate = null) }
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                enabled = false,
                modifier = Modifier
                    .padding(bottom = verticalPadding)
                    .padding(horizontal = horizontalPadding)
                    .clickable(interactionSource, indication = null) {
                        sendIntent(
                            CashbackIntent.ShowDialog(
                                EndDatePicker(state.cashback.expirationDate)
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.cashback.comment,
                onTextChange = {
                    sendIntent(
                        CashbackIntent.UpdateCashback { copy(comment = it) }
                    )
                },
                label = stringResource(R.string.comment),
                singleLine = false,
                imeAction = ImeAction.Default,
                modifier = Modifier
                    .padding(bottom = verticalPadding)
                    .padding(horizontal = horizontalPadding)
                    .fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(64.dp))
        }
    }
}


@Serializable
private sealed interface DatePicker : DialogType {
    val date: LocalDate?
}


private data class StartDatePicker(
    override val date: LocalDate?
) : DatePicker


private data class EndDatePicker(
    override val date: LocalDate?
) : DatePicker




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    date: LocalDate?,
    onConfirm: (LocalDate?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isDateSelectable: (LocalDate) -> Boolean = { true }
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date?.epochMillis(),
        yearRange = DateUtils.MinDate.year..DateUtils.MaxDate.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return isDateSelectable(LocalDate(utcTimeMillis))
            }
        }
    )

    val confirmEnabled = remember {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { isDateSelectable(LocalDate(it)) } == true
        }
    }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onConfirm(LocalDate(epochMillis = it))
                        onClose()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary.animate()
                ),
                enabled = confirmEnabled.value,
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily(VerdanaFont),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary.animate()
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily(VerdanaFont),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.animate(),
            titleContentColor = MaterialTheme.colorScheme.background.animate(),
            navigationContentColor = Color.Transparent
        )
    ) {
        DatePicker(state = datePickerState)
    }
}




private object CashbackUnitSelection : DialogType


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun UnitSelectionDialog(
    measureUnits: ListState<MeasureUnit>,
    selectedUnit: MeasureUnit?,
    onConfirm: (MeasureUnit) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedUnit = rememberSaveable(
        saver = Saver(
            save = { it.value?.toString() },
            restore = {
                when (it) {
                    "null" -> null
                    else -> mutableStateOf(MeasureUnit(it))
                }
            }
        )
    ) {
        mutableStateOf(selectedUnit)
    }
    val confirmEnabled = remember {
        derivedStateOf { selectedUnit.value != null }
    }
    val topPaddingPx = remember { mutableFloatStateOf(0f) }
    val bottomPaddingPx = remember { mutableFloatStateOf(0f) }

    BasicAlertDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        modifier = Modifier
            .then(modifier)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource( R.string.cashback_measure_unit),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .85f))
                    .onGloballyPositioned { topPaddingPx.floatValue = it.size.height.toFloat() }
                    .padding(horizontal = 16.dp)
                    .zIndex(1.2f)
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                item {
                    Spacer(
                        Modifier.height(
                            with(LocalDensity.current) { topPaddingPx.floatValue.toDp() }
                        )
                    )
                }

                when (measureUnits) {
                    is ListState.Loading -> item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Loading()
                        }
                    }
                    
                    is ListState.Empty -> return@LazyColumn

                    is ListState.Stable -> items(
                        items = measureUnits.data,
                        key = { it.toString() }
                    ) { calculationUnit ->
                        ListItem(
                            leadingContent = {
                                Text(
                                    text = buildString {
                                        calculationUnit.getDisplayableString().let {
                                            when (it.length) {
                                                1 -> append(' ', it, ' ')
                                                2 -> append(it, ' ')
                                                else -> append(it)
                                            }
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold.takeIf {
                                        calculationUnit == selectedUnit.value
                                    }
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = when (calculationUnit) {
                                        is MeasureUnit.Percent -> stringResource(R.string.percents)
                                        is MeasureUnit.Currency -> calculationUnit.currency
                                            .getDisplayName(Locale.getDefault())
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold.takeIf {
                                        calculationUnit == selectedUnit.value
                                    }
                                )
                            },
                            trailingContent = {
                                if (calculationUnit == selectedUnit.value) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                headlineColor = MaterialTheme.colorScheme.onBackground,
                                leadingIconColor = MaterialTheme.colorScheme.onBackground,
                                trailingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .clickable { selectedUnit.value = calculationUnit }
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }


                item {
                    Spacer(
                        modifier = Modifier.height(
                            with(LocalDensity.current) { bottomPaddingPx.floatValue.toDp() }
                        )
                    )
                }
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .85f))
                    .onGloballyPositioned { bottomPaddingPx.floatValue = it.size.height.toFloat() }
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                TextButton(onClose) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily(VerdanaFont),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        selectedUnit.value?.let {
                            onConfirm(it)
                            onClose()
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily(VerdanaFont),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}




@Preview
@Composable
private fun DatePickerDialogPreview() {
    CashbacksTheme(isDarkTheme = false) {
        DatePickerDialog(
            date = LocalDate(
                year = 2002,
                monthNumber = 10,
                dayOfMonth = 19
            ),
            onConfirm = {},
            onClose = {}
        )
    }
}


@Preview
@Composable
private fun CashbackScreenPreview() {
    CashbacksTheme {
        CashbackScreen(
            state = CashbackState(),
            snackbarHostState = remember { SnackbarHostState() },
            listState = rememberLazyListState(),
            sendIntent = {}
        )
    }
}