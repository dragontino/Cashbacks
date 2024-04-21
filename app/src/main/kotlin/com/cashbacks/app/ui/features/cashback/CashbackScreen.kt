package com.cashbacks.app.ui.features.cashback

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.model.ComposableCategoryCashback
import com.cashbacks.app.model.ComposableShopCashback
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.DropdownMenu
import com.cashbacks.app.ui.composables.DropdownMenuListContent
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.domain.R
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopInterface
import com.cashbacks.domain.util.LocalDate
import com.cashbacks.domain.util.epochMillis
import com.cashbacks.domain.util.parseToDate
import com.cashbacks.domain.util.parseToString
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CashbackScreen(
    viewModel: CashbackViewModel,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    popBackStack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardState = keyboardAsState()

    OnLifecycleEvent(
        onDestroy = {
            if (viewModel.state.value == ViewModelState.Editing) {
                scope.launch { viewModel.saveCashback() }
            }
        }
    )

    val showSnackbar = remember {
        fun (message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    LaunchedEffect(viewModel.addingCategoryState) {
        if (viewModel.addingCategoryState) {
            viewModel.showOwnersSelection = false
        }
    }

    LaunchedEffect(viewModel.showOwnersSelection) {
        if (viewModel.showOwnersSelection) {
            viewModel.addingCategoryState = false
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardState.value }.collect { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.addingCategoryState = false
            }
        }
    }

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> {
                    when (event.args) {
                        is CategoryArgs -> navigateToCategory(event.args)
                        is ShopArgs -> navigateToShop(event.args)
                        is BankCardArgs -> navigateToCard(event.args)
                        null -> popBackStack()
                    }
                }
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
            }
        }
    }


    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = stringResource(R.string.confirm_cashback_deletion),
                onConfirm = {
                    viewModel.deleteCashback()
                    viewModel.navigateTo(null)
                },
                onClose = viewModel::closeDialog
            )
        }
        is DialogType.DatePicker -> {
            DatePickerDialog(
                date = type.date,
                onConfirm = {
                    with(viewModel.cashback.value) {
                        updateValue(
                            property = ::expirationDate,
                            newValue = it?.parseToString() ?: ""
                        )
                    }
                    viewModel.closeDialog()
                },
                onClose = viewModel::closeDialog
            )
        }
        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.saveInfo(context.resources) {
                        viewModel.navigateTo(null)
                    }
                },
                onDismiss = { viewModel.navigateTo(null) },
                onClose = viewModel::closeDialog
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
            targetState = viewModel.state.value,
            label = "loading animation",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> CashbackContent(viewModel, snackbarHostState)
            }
        }

        AnimatedVisibility(
            visible = viewModel.state.value != ViewModelState.Loading && viewModel.addingCategoryState,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                viewModel.addCategory(name)
                viewModel.addingCategoryState = false
            }
        }
    }


    BackHandler(enabled = viewModel.cashback.value.haveChanges) {
        viewModel.openDialog(DialogType.Save)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbackContent(
    viewModel: CashbackViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    CollapsingToolbarScaffold(
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
                            if (viewModel.cashback.value.haveChanges) {
                                viewModel.openDialog(DialogType.Save)
                            } else {
                                viewModel.navigateTo(null)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when {
                                viewModel.cashback.value.haveChanges -> Icons.Rounded.Close
                                else -> Icons.Rounded.ArrowBackIosNew
                            },
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    if (viewModel.cashbackId != null) {
                        IconButton(
                            onClick = {
                                viewModel.openDialog(
                                    type = DialogType.ConfirmDeletion(viewModel.cashback.value)
                                )
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
                            viewModel.saveInfo(context.resources) {
                                viewModel.navigateTo(null)
                            }
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
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        contentState = scrollState,
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
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = viewModel.showOwnersSelection,
                onExpandedChange = viewModel::showOwnersSelection::set
            ) {
                EditableTextField(
                    text = when (val cashback = viewModel.cashback.value) {
                        is ComposableCategoryCashback -> cashback.category?.name
                        is ComposableShopCashback -> cashback.shop?.name
                    } ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = viewModel.ownerType.getTitle(context.resources),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = viewModel.showErrors
                            && !viewModel.cashback.value.ownerErrorMessage.value.isNullOrBlank(),
                    errorMessage = viewModel.cashback.value.ownerErrorMessage.value ?: "",
                    trailingActions = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.showOwnersSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    listLiveData = when (viewModel.ownerType) {
                        CashbackOwner.Category -> viewModel.getAllCategories()
                        CashbackOwner.Shop -> viewModel.getAllShops()
                    },
                    expanded = viewModel.showOwnersSelection,
                    onClose = { viewModel.showOwnersSelection = false }
                ) { list ->
                    val cashback = viewModel.cashback.value
                    DropdownMenuListContent(
                        list = list,
                        selectedItem = when (cashback) {
                            is ComposableCategoryCashback -> cashback.category
                            is ComposableShopCashback -> cashback.shop
                        },
                        title = {
                            when (it) {
                                is Category -> it.name
                                is ShopInterface -> it.name
                                else -> it.toString()
                            }
                        },
                        onClick = {
                            when (cashback) {
                                is ComposableCategoryCashback -> cashback.updateValue(
                                    property = cashback::category,
                                    newValue = it as Category
                                )
                                is ComposableShopCashback -> cashback.updateValue(
                                    property = cashback::shop,
                                    newValue = it as Shop
                                )
                            }

                            if (viewModel.showErrors) {
                                cashback.updateOwnerError(context.resources)
                            }
                            viewModel.showOwnersSelection = false
                        },
                        addButton = {
                            TextButton(
                                onClick = {
                                    when (viewModel.ownerType) {
                                        CashbackOwner.Category -> viewModel.addingCategoryState = true
                                        CashbackOwner.Shop -> viewModel.navigateTo(ShopArgs.New)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (viewModel.ownerType) {
                                        CashbackOwner.Category -> stringResource(R.string.add_category)
                                        CashbackOwner.Shop -> stringResource(R.string.add_shop)
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
                onExpandedChange = viewModel::showBankCardsSelection::set,
            ) {
                EditableTextField(
                    text = viewModel.cashback.value.bankCard?.toString()
                        ?: stringResource(R.string.value_not_selected),
                    onTextChange = {},
                    label = stringResource(R.string.bank_card),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    error = viewModel.showErrors
                            && !viewModel.cashback.value.bankCardErrorMessage.value.isNullOrBlank(),
                    errorMessage = viewModel.cashback.value.bankCardErrorMessage.value ?: "",
                    leadingIcon = {
                        viewModel.cashback.value.bankCard?.paymentSystem?.let {
                            PaymentSystemMapper.PaymentSystemImage(
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
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    listLiveData = viewModel.getAllBankCards(),
                    expanded = viewModel.showBankCardsSelection,
                    onClose = { viewModel.showBankCardsSelection = false }
                ) { cards ->
                    DropdownMenuListContent(
                        list = cards,
                        selectedItem = viewModel.cashback.value.bankCard,
                        leadingIcon = { card ->
                            card.paymentSystem?.let {
                                PaymentSystemMapper.PaymentSystemImage(
                                    paymentSystem = it,
                                    maxWidth = 30.dp,
                                    drawBackground = false
                                )
                            }
                        },
                        onClick = {
                            with(viewModel.cashback.value) {
                                updateValue(
                                    property = ::bankCard,
                                    newValue = it
                                )

                                if (viewModel.showErrors) {
                                    updateBankCardError(context.resources)
                                }
                            }
                            viewModel.showBankCardsSelection = false
                        },
                        addButton = {
                            TextButton(
                                onClick = { viewModel.navigateTo(BankCardArgs.New) },
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
                text = viewModel.cashback.value.amount,
                onTextChange = {
                    with(viewModel.cashback.value) {
                        updateValue(::amount, it)

                        if (viewModel.showErrors) {
                            updateAmountError(context.resources)
                        }
                    }
                },
                error = viewModel.showErrors
                        && viewModel.cashback.value.amountErrorMessage.value?.isNotBlank() == true,
                errorMessage = viewModel.cashback.value.amountErrorMessage.value ?: "",
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Number
            )

            EditableTextField(
                text = viewModel.cashback.value.expirationDate,
                onTextChange = {},
                label = stringResource(R.string.validity_period),
                textStyle = MaterialTheme.typography.bodyMedium,
                enabled = false,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember(::MutableInteractionSource),
                        indication = null
                    ) {
                        val date = viewModel.cashback.value.expirationDate
                            .takeIf { it.isNotBlank() }
                            ?.parseToDate()
                        viewModel.openDialog(DialogType.DatePicker(date))
                    }
                    .fillMaxWidth()
            )

            EditableTextField(
                text = viewModel.cashback.value.comment,
                onTextChange = {
                    with(viewModel.cashback.value) {
                        updateValue(::comment, it)
                    }
                },
                label = stringResource(R.string.comment),
                singleLine = false,
                imeAction = ImeAction.Default,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    date: LocalDate?,
    onConfirm: (LocalDate?) -> Unit,
    onClose: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date?.epochMillis)

    DatePickerDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        datePickerState.selectedDateMillis?.let(::LocalDate)
                    )
                    datePickerState.selectedDateMillis?.let {
                        onConfirm(LocalDate(epochMillis = it))
                        onClose()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary.animate()
                ),
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.bodyMedium
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.animate(),
            titleContentColor = MaterialTheme.colorScheme.background.animate(),
        )
    ) {
        DatePicker(state = datePickerState)
    }
}




@Preview
@Composable
private fun DatePickerDialogPreview() {
    CashbacksTheme(isDarkTheme = false) {
        DatePickerDialog(
            date = LocalDate.of(2002, 10, 19),
            onConfirm = {},
            onClose = {}
        )
    }
}