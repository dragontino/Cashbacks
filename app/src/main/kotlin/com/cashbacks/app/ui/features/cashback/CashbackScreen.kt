package com.cashbacks.app.ui.features.cashback

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.util.LocalDate
import com.cashbacks.domain.util.epochMillis
import com.cashbacks.domain.util.parseToDate
import com.cashbacks.domain.util.parseToString
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CashbackScreen(
    viewModel: CashbackViewModel,
    navigateToCard: (args: BankCardArgs) -> Unit,
    popBackStack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showSnackbar = remember {
        fun (message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> {
                    when (event.args) {
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
                    viewModel.saveInfo(context).let {
                        if (it) viewModel.navigateTo(null)
                    }
                },
                onDismiss = { viewModel.navigateTo(null) },
                onClose = viewModel::closeDialog
            )
        }
        null -> {}
    }

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
                            viewModel.saveInfo(context).let {
                                if (it) viewModel.navigateTo(null)
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
                            && viewModel.cashback.value.bankCardErrorMessage.value.isNotBlank(),
                    errorMessage = viewModel.cashback.value.bankCardErrorMessage.value,
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

                ExposedDropdownMenu(
                    expanded = viewModel.showBankCardsSelection,
                    onDismissRequest = { viewModel.showBankCardsSelection = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.getAllBankCards().observeAsState().value.let { cards ->
                        when (cards) {
                            null -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Loading()
                            }

                            else -> CardsList(
                                cards = cards,
                                selectedCard = viewModel.cashback.value.bankCard,
                                onClick = {
                                    with(viewModel.cashback.value) {
                                        updateValue(
                                            property = ::bankCard,
                                            newValue = it
                                        )

                                        if (viewModel.showErrors) {
                                            updateBankCardError(context)
                                        }
                                    }
                                    viewModel.showBankCardsSelection = false
                                },
                                onAdd = {
                                    viewModel.navigateTo(
                                        args = BankCardArgs(
                                            id = null,
                                            isEditing = true
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            EditableTextField(
                text = viewModel.cashback.value.amount,
                onTextChange = {
                    with(viewModel.cashback.value) {
                        updateValue(::amount, it)

                        if (viewModel.showErrors) {
                            updateAmountError(context)
                        }
                    }
                },
                error = viewModel.showErrors
                        && viewModel.cashback.value.amountErrorMessage.value.isNotBlank(),
                errorMessage = viewModel.cashback.value.amountErrorMessage.value,
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
@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CardsList(
    cards: List<BankCard>,
    selectedCard: BankCard?,
    onClick: (BankCard) -> Unit,
    onAdd: () -> Unit
) {
    cards.forEach { card ->
        DropdownMenuItem(
            text = {
                Text(
                    text = card.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            onClick = { onClick(card) },
            leadingIcon = {
                card.paymentSystem?.let {
                    PaymentSystemMapper.PaymentSystemImage(
                        paymentSystem = it,
                        maxWidth = 30.dp,
                        drawBackground = false
                    )
                }
            },
            trailingIcon = {
                if (card.id == selectedCard?.id) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                }
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
        )
        HorizontalDivider()
    }

    TextButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.add_card),
            style = MaterialTheme.typography.bodySmall
        )
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