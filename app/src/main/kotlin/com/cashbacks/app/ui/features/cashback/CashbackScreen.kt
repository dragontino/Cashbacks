package com.cashbacks.app.ui.features.cashback

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
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
import com.cashbacks.app.model.CashbackError
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.DropdownMenu
import com.cashbacks.app.ui.composables.DropdownMenuListContent
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.cashback.mvi.CashbackAction
import com.cashbacks.app.ui.features.cashback.mvi.CashbackEvent
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.ui.theme.VerdanaFont
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.PaymentSystemUtils
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.getDisplayableString
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.mix
import com.cashbacks.domain.R
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.util.LocalDate
import com.cashbacks.domain.util.LocalDateParceler
import com.cashbacks.domain.util.epochMillis
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CashbackScreen(
    viewModel: CashbackViewModel,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    navigateBack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val keyboardState = keyboardAsState()
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    OnLifecycleEvent(
        onDestroy = {
            viewModel.push(CashbackAction.SaveData())
        }
    )

    LaunchedEffect(viewModel.isCreatingCategory) {
        if (viewModel.isCreatingCategory) {
            viewModel.push(CashbackAction.HideAllSelections)
        }
    }

    LaunchedEffect(viewModel.showOwnersSelection) {
        if (viewModel.showOwnersSelection) {
            viewModel.push(CashbackAction.CancelCreatingCategory)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.onEach { event ->
            when (event) {
                is CashbackEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CashbackEvent.OpenDialog -> dialogType.value = event.type
                is CashbackEvent.CloseDialog -> dialogType.value = null
                is CashbackEvent.NavigateBack -> navigateBack()
                is CashbackEvent.NavigateToShop -> navigateToShop(event.args)
                is CashbackEvent.NavigateToBankCard -> navigateToCard(event.args)
            }
        }.launchIn(this)


        snapshotFlow { keyboardState.value }.onEach { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.push(CashbackAction.CancelCreatingCategory)
            }
        }.launchIn(this)
    }


    when (val type = dialogType.value) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = stringResource(R.string.confirm_cashback_deletion),
                onConfirm = {
                    viewModel.push(
                        CashbackAction.DeleteData {
                            viewModel.push(CashbackAction.ClickButtonBack)
                        }
                    )
                },
                onClose = { viewModel.push(CashbackAction.HideDialog) }
            )
        }

        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.push(
                        CashbackAction.SaveData {
                            viewModel.push(CashbackAction.ClickButtonBack)
                        }
                    )
                },
                onDismiss = { viewModel.push(CashbackAction.ClickButtonBack) },
                onClose = { viewModel.push(CashbackAction.HideDialog) }
            )
        }

        is DatePicker -> {
            DatePickerDialog(
                date = type.date,
                onConfirm = {
                    viewModel.cashback.apply { ::expirationDate updateTo it }
                },
                onClose = { viewModel.push(CashbackAction.HideDialog) },
                isDateSelectable = { date ->
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    when (type) {
                        is StartDatePicker -> date <= today
                        is EndDatePicker -> date >= today
                    }
                }
            )
        }

        is CashbackUnitSelection -> {
            UnitSelectionDialog(
                measureUnitsFlow = viewModel.measureUnitsStateFlow,
                selectedUnit = viewModel.cashback.measureUnit,
                onConfirm = {
                    viewModel.cashback.apply {
                        ::measureUnit updateTo it
                        if (
                            it is MeasureUnit.Percent &&
                            amount.toDoubleOrNull()?.let { it !in 0.0..100.0 } == true
                        ) {
                            ::amount updateTo ""
                        }
                    }
                },
                onClose = { viewModel.push(CashbackAction.HideDialog) }
            )
        }

        null -> {}
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = viewModel.state,
            label = "loading animation",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
        ) { state ->
            when (state) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Showing -> CashbackContent(viewModel, snackbarHostState)
            }
        }

        AnimatedVisibility(
            visible = viewModel.state != ScreenState.Loading && viewModel.isCreatingCategory,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                viewModel.push(CashbackAction.AddCategory(name))
            }
        }
    }


    BackHandler(enabled = viewModel.cashback.haveChanges) {
        viewModel.push(CashbackAction.ShowDialog(DialogType.Save))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbackContent(
    viewModel: CashbackViewModel,
    snackbarHostState: SnackbarHostState
) {
    val topBarState = rememberTopAppBarState()
    val contentState = rememberScrollState()

    CollapsingToolbarScaffold(
        topBarState = topBarState,
        contentState = contentState,
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
                                viewModel.cashback.haveChanges -> CashbackAction.ShowDialog(DialogType.Save)
                                else -> CashbackAction.ClickButtonBack
                            }
                            viewModel.push(action)
                        }
                    ) {
                        Icon(
                            imageVector = when {
                                viewModel.cashback.haveChanges -> Icons.Rounded.Close
                                else -> Icons.Rounded.ArrowBackIosNew
                            },
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    if (viewModel.cashback.id != null) {
                        IconButton(
                            onClick = {
                                viewModel.cashback.mapToCashback()?.let {
                                    viewModel.push(
                                        action = CashbackAction.ShowDialog(
                                            type = DialogType.ConfirmDeletion(it)
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "delete cashback",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.push(
                                CashbackAction.SaveData {
                                    viewModel.push(CashbackAction.ClickButtonBack)
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = "save",
                            modifier = Modifier.scale(1.3f)
                        )
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
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentState)
                .padding(vertical = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = viewModel.showOwnersSelection,
                onExpandedChange = { expanded ->
                    val action = when {
                        expanded -> CashbackAction.ShowOwnersSelection
                        else -> CashbackAction.HideOwnersSelection
                    }
                    viewModel.push(action)
                }
            ) {
                EditableTextField(
                    text = viewModel.cashback.owner?.name
                        ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = when (viewModel.ownerType) {
                        CashbackOwnerType.Category -> stringResource(R.string.category_title)
                        CashbackOwnerType.Shop -> stringResource(R.string.shop_title)
                    },
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = viewModel.showErrors
                            && viewModel.cashback.errors[CashbackError.Owner] != null,
                    errorMessage = viewModel.cashback.errors[CashbackError.Owner] ?: "",
                    trailingActions = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.showOwnersSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                DropdownMenu(
                    itemsFlow = viewModel.ownersStateFlow,
                    expanded = viewModel.showOwnersSelection,
                    onClose = { viewModel.push(CashbackAction.HideOwnersSelection) },
                ) { list ->
                    DropdownMenuListContent(
                        list = list,
                        selected = { viewModel.cashback.owner?.id == it.id },
                        title = { it.name },
                        onClick = {
                            viewModel.cashback.apply { ::owner updateTo it }
                            if (viewModel.showErrors) {
                                viewModel.push(
                                    CashbackAction.UpdateCashbackErrorMessage(
                                        CashbackError.Owner
                                    )
                                )
                            }
                            viewModel.push(CashbackAction.HideOwnersSelection)
                        },
                        addButton = {
                            TextButton(
                                onClick = {
                                    when (viewModel.ownerType) {
                                        CashbackOwnerType.Category -> {
                                            viewModel.push(CashbackAction.StartCreatingCategory)
                                        }

                                        CashbackOwnerType.Shop -> {
                                            viewModel.push(CashbackAction.CreateShop)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (viewModel.ownerType) {
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

            HorizontalDivider()

            ExposedDropdownMenuBox(
                expanded = viewModel.showBankCardsSelection,
                onExpandedChange = { expanded ->
                    val action = when {
                        expanded -> CashbackAction.ShowBankCardsSelection
                        else -> CashbackAction.HideBankCardsSelection
                    }
                    viewModel.push(action)
                },
            ) {
                EditableTextField(
                    text = viewModel.cashback.bankCard?.toString()
                        ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = stringResource(R.string.bank_card),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = viewModel.showErrors
                            && viewModel.cashback.errors[CashbackError.BankCard] != null,
                    errorMessage = viewModel.cashback.errors[CashbackError.BankCard] ?: "",
                    leadingIcon = {
                        viewModel.cashback.bankCard?.paymentSystem?.let {
                            PaymentSystemUtils.PaymentSystemImage(
                                paymentSystem = it,
                                maxWidth = 30.dp,
                                drawBackground = false
                            )
                        }
                    },
                    trailingActions = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.showBankCardsSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                DropdownMenu(
                    itemsFlow = viewModel.bankCardsStateFlow,
                    expanded = viewModel.showBankCardsSelection,
                    onClose = { viewModel.push(CashbackAction.HideBankCardsSelection) }
                ) { cards ->
                    DropdownMenuListContent(
                        list = cards,
                        selected = { viewModel.cashback.bankCard?.id == it.id },
                        leadingIcon = { card ->
                            card.paymentSystem?.let {
                                PaymentSystemUtils.PaymentSystemImage(
                                    paymentSystem = it,
                                    maxWidth = 30.dp,
                                    drawBackground = false
                                )
                            }
                        },
                        title = { it.name },
                        onClick = {
                            viewModel.cashback.apply { ::bankCard updateTo it }
                            if (viewModel.showErrors) {
                                viewModel.push(
                                    CashbackAction.UpdateCashbackErrorMessage(CashbackError.BankCard)
                                )
                            }
                            viewModel.push(CashbackAction.HideBankCardsSelection)
                        },
                        addButton = {
                            TextButton(
                                onClick = { viewModel.push(CashbackAction.CreateBankCard) },
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

            EditableTextField(
                text = viewModel.cashback.amount,
                onTextChange = { stringAmount ->
                    with(viewModel) {
                        val amount = stringAmount.toDoubleOrNull()
                        val isAmountCorrect = when {
                            amount == null -> true
                            cashback.measureUnit is MeasureUnit.Currency -> amount >= 0
                            amount in 0.0 .. 100.0 -> true
                            else -> false
                        }
                        if (isAmountCorrect) {
                            cashback.apply { ::amount updateTo stringAmount }
                            if (showErrors) {
                                push(CashbackAction.UpdateCashbackErrorMessage(CashbackError.Amount))
                            }
                        }
                    }
                },
                trailingActions = {
                    TextButton(
                        onClick = {
                            viewModel.push(CashbackAction.ShowDialog(CashbackUnitSelection))
                        }
                    ) {
                        Text(
                            text = viewModel.cashback.measureUnit.getDisplayableString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = EditableTextFieldDefaults.colors(
                    focusedTrailingActionsColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTrailingActionsColor = MaterialTheme.colorScheme.onBackground
                ),
                error = viewModel.showErrors
                        && viewModel.cashback.errors[CashbackError.Amount] != null,
                errorMessage = viewModel.cashback.errors[CashbackError.Amount] ?: "",
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(horizontal = 16.dp)
            )



            EditableTextField(
                text = viewModel.cashback.expirationDate?.getDisplayableString() ?: "",
                onTextChange = {},
                label = stringResource(R.string.validity_period),
                trailingActions = {
                    if (viewModel.cashback.expirationDate != null) {
                        IconButton(
                            onClick = {
                                viewModel.cashback.apply { ::expirationDate updateTo null }
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
                    .padding(horizontal = 16.dp)
                    .clickable(
                        interactionSource = remember(::MutableInteractionSource),
                        indication = null
                    ) {
                        viewModel.push(
                            CashbackAction.ShowDialog(
                                EndDatePicker(viewModel.cashback.expirationDate)
                            )
                        )
                    }
                    .fillMaxWidth()
            )

            EditableTextField(
                text = viewModel.cashback.comment,
                onTextChange = {
                    viewModel.cashback.apply { ::comment updateTo it }
                },
                label = stringResource(R.string.comment),
                singleLine = false,
                imeAction = ImeAction.Default,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}


private sealed interface DatePicker : DialogType {
    val date: LocalDate?
}


@Parcelize
private data class StartDatePicker(
    override val date: @WriteWith<LocalDateParceler> LocalDate?
) : DatePicker


@Parcelize
private data class EndDatePicker(
    override val date: @WriteWith<LocalDateParceler> LocalDate?
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




@Parcelize
private object CashbackUnitSelection : DialogType


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun UnitSelectionDialog(
    measureUnitsFlow: StateFlow<ListState<MeasureUnit>>,
    selectedUnit: MeasureUnit?,
    onConfirm: (MeasureUnit) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unitsState = measureUnitsFlow.collectAsStateWithLifecycle()
    val selectedUnit = rememberSaveable { mutableStateOf(selectedUnit) }
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
                text = stringResource(R.string.cashback_measure_unit),
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

                when (unitsState.value) {
                    is ListState.Loading -> item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Loading()
                        }
                    }

                    else -> items(
                        items = unitsState.value.data,
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