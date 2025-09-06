package com.cashbacks.features.bankcard.presentation.impl.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.common.composables.EditableTextField
import com.cashbacks.common.composables.EditableTextFieldColors
import com.cashbacks.common.composables.EditableTextFieldDefaults
import com.cashbacks.common.composables.ListDropdownMenu
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.ModalBottomSheet
import com.cashbacks.common.composables.management.BottomSheetType
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.theme.DarkerGray
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.DateUtils.getDisplayableString
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.common.utils.now
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import com.cashbacks.features.bankcard.presentation.api.utils.PaymentSystemUtils
import com.cashbacks.features.bankcard.presentation.api.utils.PaymentSystemUtils.title
import com.cashbacks.features.bankcard.presentation.impl.mvi.BankCardError
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingIntent
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingLabel
import com.cashbacks.features.bankcard.presentation.impl.mvi.EditingState
import com.cashbacks.features.bankcard.presentation.impl.mvi.model.EditableBankCard
import com.cashbacks.features.bankcard.presentation.impl.viewmodel.BankCardEditingViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun BankCardEditingRoot(
    navigateBack: () -> Unit,
    viewModel: BankCardEditingViewModel = koinViewModel()
) {
    val snackbarState = remember(::SnackbarHostState)
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    var bottomSheetType: BottomSheetType? by rememberSaveable { mutableStateOf(null) }
    val editingState by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is EditingLabel.DisplayMessage -> launch {
                    snackbarState.showSnackbar(label.message)
                }
                is EditingLabel.ChangeOpenedDialog -> dialogType = label.openedDialogType
                is EditingLabel.ChangeOpenedBottomSheet -> bottomSheetType = label.type
                is EditingLabel.NavigateBack -> navigateBack()
            }
        }
    }


    if (dialogType == DialogType.Save) {
        ConfirmExitWithSaveDataDialog(
            onConfirm = {
                viewModel.sendIntent(
                    EditingIntent.Save {
                        viewModel.sendIntent(EditingIntent.ClickButtonBack)
                    }
                )
            },
            onDismiss = {
                viewModel.sendIntent(EditingIntent.ClickButtonBack)
            },
            onClose = {
                viewModel.sendIntent(EditingIntent.HideDialog)
            }
        )
    }

    bottomSheetType?.let { bottomSheetType ->
        when (bottomSheetType) {
            is ValidityPeriodSelectionBottomSheet -> ValidityPeriodSelectionBottomSheet(
                initialValidityPeriod = bottomSheetType.validityPeriod,
                onConfirm = {
                    viewModel.sendIntent(
                        EditingIntent.UpdateBankCard(
                            card = editingState.card.copy(validityPeriod = it)
                        )
                    )
                    viewModel.sendIntent(
                        EditingIntent.UpdateErrorMessage(BankCardError.ValidityPeriod)
                    )
                },
                onClose = { viewModel.sendIntent(EditingIntent.HideBottomSheet) }
            )

            is MaxCashbacksNumberSelectionBottomSheet -> MaxCashbacksNumberSelectionBottomSheet(
                initialMaxCashbacksNumber = bottomSheetType.maxCashbacksNumber,
                onConfirm = {
                    viewModel.sendIntent(
                        EditingIntent.UpdateBankCard(
                            card = editingState.card.copy(maxCashbacksNumber = it)
                        )
                    )
                },
                onClose = { viewModel.sendIntent(EditingIntent.HideBottomSheet) }
            )
        }
    }


    BankCardEditingScreen(
        state = editingState,
        snackbarHostState = snackbarState,
        intentSender = IntentSender(viewModel::sendIntent)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankCardEditingScreen(
    state: EditingState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<EditingIntent>
) {
    BackHandler {
        when {
            state.isChanged() -> intentSender.sendWithDelay(EditingIntent.ShowDialog(DialogType.Save))
            else -> intentSender.sendWithDelay(EditingIntent.ClickButtonBack)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.bank_card),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Crossfade(
                        targetState = state.isChanged(),
                        label = "bank card nav icon anim",
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                intentSender.sendWithDelay {
                                    when {
                                        isChanged -> yield(EditingIntent.ShowDialog(DialogType.Save))
                                        else -> yield(EditingIntent.ClickButtonBack)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when {
                                    isChanged -> Icons.Rounded.Clear
                                    else -> Icons.Rounded.ArrowBackIosNew
                                },
                                contentDescription = "return to previous screen",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(visible = state.screenState != ScreenState.Loading) {
                        IconButton(
                            onClick = {
                                intentSender.sendWithDelay(
                                    EditingIntent.Save {
                                        intentSender.send(EditingIntent.ClickButtonBack)
                                    }
                                )
                            },
                            enabled = state.screenState != ScreenState.Loading
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "save card",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.background.reversed,
                    contentColor = MaterialTheme.colorScheme.onBackground.reversed,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) { contentPadding ->
        Crossfade(
            targetState = state.screenState,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            label = "state animation"
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Stable -> BankCardEditingContent(
                    state = state,
                    intentSender = intentSender,
                    modifier = Modifier
                        .imePadding()
                        .padding(contentPadding)
                        .fillMaxSize()
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun BankCardEditingContent(
    state: EditingState,
    intentSender: IntentSender<EditingIntent>,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember(::MutableInteractionSource)

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background.animate())
            .fillMaxSize()
    ) {
        stickyHeader {
            Text(
                text = stringResource(R.string.main_info),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DarkerGray,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 16.dp, bottom = 10.dp)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                value = state.card.number,
                onValueChange = {
                    intentSender.send(
                        EditingIntent.UpdateBankCard(state.card.updateNumber(it))
                    )
                    intentSender.sendWithDelay(
                        EditingIntent.UpdateErrorMessage(BankCardError.Number)
                    )
                },
                label = stringResource(R.string.card_number),
                keyboardType = KeyboardType.Decimal,
                error = state.showErrors && BankCardError.Number in state.errors,
                errorMessage = state.errors[BankCardError.Number],
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = state.showPaymentSystemSelection,
                onExpandedChange = { isExpanded ->
                    val intent = when {
                        isExpanded -> EditingIntent.ShowPaymentSystemSelection
                        else -> EditingIntent.HidePaymentSystemSelection
                    }
                    intentSender.sendWithDelay(intent)
                }
            ) {
                EditableTextField(
                    text = state.card.paymentSystem.title,
                    onTextChange = {},
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    label = stringResource(R.string.payment_system),
                    leadingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.showPaymentSystemSelection
                        )
                    },
                    trailingActions = {
                        state.card.paymentSystem?.let {
                            PaymentSystemUtils.PaymentSystemImage(
                                paymentSystem = it,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                ListDropdownMenu(
                    state = ListState.Stable {
                        add(null)
                        addAll(PaymentSystem.entries)
                    },
                    expanded = state.showPaymentSystemSelection,
                    onClose = { intentSender.sendWithDelay(EditingIntent.HidePaymentSystemSelection) }
                ) { paymentSystems ->
                    paymentSystems.forEach { paymentSystem ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = paymentSystem.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                if (state.card.paymentSystem == paymentSystem) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            trailingIcon = {
                                if (paymentSystem != null) {
                                    PaymentSystemUtils.PaymentSystemImage(
                                        paymentSystem = paymentSystem,
                                        maxWidth = 30.dp,
                                        drawBackground = false
                                    )
                                }
                            },
                            onClick = {
                                intentSender.sendWithDelay(
                                    EditingIntent.UpdateBankCard(
                                        state.card.copy(paymentSystem = paymentSystem)
                                    ),
                                    EditingIntent.HidePaymentSystemSelection
                                )
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onBackground,
                                leadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        item {
            EditableTextField(
                text = state.card.holder,
                onTextChange = {
                    intentSender.send(
                        EditingIntent.UpdateBankCard(
                            state.card.copy(holder = it.uppercase())
                        )
                    )
                },
                label = stringResource(R.string.card_holder),
                keyboardCapitalization = KeyboardCapitalization.Characters,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.card.validityPeriod
                    ?.getDisplayableString(
                        displayablePattern = EditableBankCard.VALIDITY_PERIOD_DATE_PATTERN
                    ) ?: "",
                onTextChange = {},
                enabled = false,
                label = stringResource(R.string.validity_period),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable(interactionSource, indication = null) {
                        intentSender.sendWithDelay(
                            EditingIntent.ShowBottomSheet(
                                ValidityPeriodSelectionBottomSheet(state.card.validityPeriod)
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            var isCvvVisible by rememberSaveable { mutableStateOf(false) }
            EditableTextField(
                text = state.card.cvv,
                onTextChange = {
                    if (it.length <= 3) {
                        intentSender.send(
                            EditingIntent.UpdateBankCard(
                                state.card.copy(cvv = it)
                            )
                        )
                        intentSender.sendWithDelay(EditingIntent.UpdateErrorMessage(BankCardError.Cvv))
                    }
                },
                label = stringResource(R.string.cvv),
                keyboardType = KeyboardType.Number,
                visualTransformation = EditableTextFieldDefaults
                    .passwordVisualTransformation(isVisible = isCvvVisible),
                error = state.showErrors && BankCardError.Cvv in state.errors,
                errorMessage = state.errors[BankCardError.Cvv],
                trailingActions = {
                    IconButton(onClick = { isCvvVisible = !isCvvVisible }) {
                        Icon(
                            imageVector = when {
                                isCvvVisible -> Icons.Outlined.VisibilityOff
                                else -> Icons.Outlined.Visibility
                            },
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            var isPinVisible by rememberSaveable { mutableStateOf(false) }
            EditableTextField(
                text = state.card.pin,
                onTextChange = {
                    if (it.length <= 4) {
                        intentSender.send(
                            EditingIntent.UpdateBankCard(
                                state.card.copy(pin = it)
                            )
                        )
                        intentSender.sendWithDelay(
                            EditingIntent.UpdateErrorMessage(BankCardError.Pin)
                        )
                    }
                },
                label = stringResource(R.string.pin),
                keyboardType = KeyboardType.Number,
                visualTransformation = EditableTextFieldDefaults
                    .passwordVisualTransformation(isVisible = isPinVisible),
                error = state.showErrors && BankCardError.Pin in state.errors,
                errorMessage = state.errors[BankCardError.Pin],
                trailingActions = {
                    IconButton(onClick = { isPinVisible = !isPinVisible }) {
                        Icon(
                            imageVector = when {
                                isPinVisible -> Icons.Outlined.VisibilityOff
                                else -> Icons.Outlined.Visibility
                            },
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        stickyHeader {
            Text(
                text = stringResource(R.string.additional_info),
                style = MaterialTheme.typography.labelSmall,
                color = DarkerGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.card.name,
                onTextChange = {
                    intentSender.send(
                        EditingIntent.UpdateBankCard(
                            state.card.copy(name = it)
                        )
                    )
                },
                label = stringResource(R.string.card_name),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.card.maxCashbacksNumber?.toString()
                    ?: stringResource(R.string.value_not_selected),
                onTextChange = {},
                enabled = false,
                label = stringResource(R.string.cashbacks_month_limit),
                trailingActions = {
                    if (state.card.maxCashbacksNumber != null) {
                        IconButton(
                            onClick = {
                                intentSender.sendWithDelay(
                                    EditingIntent.UpdateBankCard(
                                        state.card.copy(maxCashbacksNumber = null)
                                    )
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
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable(interactionSource, indication = null) {
                        intentSender.sendWithDelay(
                            EditingIntent.ShowBottomSheet(
                                type = MaxCashbacksNumberSelectionBottomSheet(
                                    maxCashbacksNumber = state.card.maxCashbacksNumber
                                )
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = state.card.comment,
                onTextChange = {
                    intentSender.send(
                        EditingIntent.UpdateBankCard(
                            state.card.copy(comment = it)
                        )
                    )
                },
                label = stringResource(R.string.comment),
                singleLine = false,
                imeAction = ImeAction.Default,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(64.dp))
        }
    }
}




@Serializable
private data class ValidityPeriodSelectionBottomSheet(
    val validityPeriod: LocalDate?
) : BottomSheetType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValidityPeriodSelectionBottomSheet(
    initialValidityPeriod: LocalDate?,
    onConfirm: (LocalDate?) -> Unit,
    onClose: () -> Unit
) {
    val (month, year) = rememberSaveable {
        val date = initialValidityPeriod ?: LocalDate.now()
        val month = date.month.number.toString().padStart(2, '0')
        val year = date.year.toString()
        mutableStateOf(month) to mutableStateOf(year)
    }

    ModalBottomSheet(
        onClose = onClose,
        title = stringResource(R.string.validity_period)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            NumberControlPanel(
                value = month.value,
                onValueChange = {
                    if (it.isEmpty() || (it.toIntOrNull() != null && it.length < 4)) month.value = it
                },
                incrementValue = {
                    val value = month.value.toIntOrNull() ?: 1
                    (value % 12 + 1).toString().padStart(2, '0')
                },
                decrementValue = {
                    val value = month.value.toIntOrNull() ?: 2
                    ((value + 10) % 12 + 1).toString().padStart(2, '0')
                },
                onImeActionClick = {
                    val value = month.value.toIntOrNull() ?: 1
                    month.value = value.coerceIn(1..12).toString().padStart(2, '0')
                    defaultKeyboardAction(ImeAction.Next)
                },
                inputFieldModifier = Modifier.width(IntrinsicSize.Min)
            )

            Text(
                text = "/",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            NumberControlPanel(
                value = year.value,
                onValueChange = {
                    if (it.isEmpty() || (it.toIntOrNull() != null && it.length < 8)) year.value = it
                },
                incrementValue = {
                    val value = year.value.toIntOrNull() ?: 1
                    ((value - 1999) % 100 + 2000).toString()
                },
                decrementValue = {
                    val value = year.value.toIntOrNull() ?: 2
                    ((value - 1901) % 100 + 2000).toString()
                },
                imeAction = ImeAction.Done,
                onImeActionClick = {
                    val value = year.value.toIntOrNull() ?: 1
                    year.value = value.coerceIn(2000..<2100).toString()
                    defaultKeyboardAction(ImeAction.Done)
                },
                inputFieldModifier = Modifier.width(IntrinsicSize.Min)
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.width(16.dp))

            TextButton(
                onClick = {
                    val resultYear = year.value.toIntOrNull()?.coerceIn(2000..<2100)
                        ?: return@TextButton onClose()
                    val resultMonth =
                        month.value.toIntOrNull()?.coerceIn(1..12) ?: return@TextButton onClose()
                    val resultDate = LocalDate(
                        year = resultYear,
                        month = Month(resultMonth),
                        day = 1
                    )
                    onConfirm(resultDate)
                    onClose()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Serializable
private data class MaxCashbacksNumberSelectionBottomSheet(
    val maxCashbacksNumber: Int?
) : BottomSheetType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaxCashbacksNumberSelectionBottomSheet(
    initialMaxCashbacksNumber: Int?,
    onConfirm: (Int?) -> Unit,
    onClose: () -> Unit
) {
    val maxCashbacksNumber = rememberSaveable {
        mutableStateOf(initialMaxCashbacksNumber?.toString() ?: "")
    }

    ModalBottomSheet(
        onClose = onClose,
        title = stringResource(R.string.cashbacks_month_limit),
        actions = {
            if (initialMaxCashbacksNumber != null) {
                IconButton(
                    onClick = {
                        onConfirm(null)
                        onClose()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = "clear value",
                        modifier = Modifier.scale(1.4f)
                    )
                }
            }
        }
    ) {
        Spacer(Modifier.height(16.dp))

        NumberControlPanel(
            value = maxCashbacksNumber.value,
            onValueChange = {
                if (it.isEmpty() || (it.toIntOrNull() != null && it.length < 10)) {
                    maxCashbacksNumber.value = it
                }
            },
            controlButtonsOrientation = Orientation.Horizontal,
            incrementValue = {
                maxCashbacksNumber.value.toIntOrNull()?.plus(1)?.toString() ?: "1"
            },
            decrementValue = {
                maxCashbacksNumber.value
                    .toIntOrNull()?.takeIf { it > 1 }
                    ?.minus(1)?.toString()
                    ?: "1"
            },
            imeAction = ImeAction.Done,
            textFieldColors = EditableTextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.Companion.align(Alignment.CenterHorizontally),
            inputFieldModifier = Modifier.width(IntrinsicSize.Min)
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.width(16.dp))

            TextButton(
                onClick = {
                    onConfirm(maxCashbacksNumber.value.toIntOrNull())
                    onClose()
                },
                enabled = maxCashbacksNumber.value.toIntOrNull() != null,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberControlPanel(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    inputFieldModifier: Modifier = Modifier,
    controlButtonsOrientation: Orientation = Orientation.Vertical,
    incrementValue: () -> String = {
        value.toIntOrNull()?.plus(1)?.toString() ?: value
    },
    decrementValue: () -> String = {
        value.toIntOrNull()?.minus(1)?.toString() ?: value
    },
    imeAction: ImeAction = ImeAction.Next,
    onImeActionClick: KeyboardActionScope.() -> Unit = { defaultKeyboardAction(imeAction) },
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textFieldColors: EditableTextFieldColors = EditableTextFieldDefaults.colors(
        enabledBorderColors = emptyList()
    )
) {
    val iconContentColor = MaterialTheme.colorScheme.primary

    @Composable
    fun InputTextField() {
        EditableTextField(
            text = value,
            onTextChange = onValueChange,
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
            onImeActionClick = { onImeActionClick() },
            textStyle = MaterialTheme.typography.labelMedium.copy(color = contentColor),
            singleLine = true,
            colors = textFieldColors,
            modifier = inputFieldModifier
        )
    }

    @Composable
    fun IncrementButton() {
        when (controlButtonsOrientation) {
            Orientation.Vertical -> IconButton(
                onClick = { onValueChange(incrementValue()) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = containerColor,
                    contentColor = iconContentColor
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Increment value",
                    modifier = Modifier.scale(1.8f)
                )
            }

            Orientation.Horizontal -> TextButton(
                onClick = { onValueChange(incrementValue()) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = containerColor,
                    contentColor = iconContentColor
                )
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }

    @Composable
    fun DecrementButton() {
        when (controlButtonsOrientation) {
            Orientation.Vertical -> IconButton(
                onClick = { onValueChange(decrementValue()) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = containerColor,
                    contentColor = iconContentColor
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Decrement value",
                    modifier = Modifier.scale(1.8f)
                )
            }

            Orientation.Horizontal -> TextButton(
                onClick = { onValueChange(decrementValue()) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = containerColor,
                    contentColor = iconContentColor
                )
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }





    when (controlButtonsOrientation) {
        Orientation.Vertical -> Column(
            modifier = modifier.width(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IncrementButton()
            InputTextField()
            DecrementButton()
        }
        Orientation.Horizontal -> Row(
            modifier = modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DecrementButton()
            InputTextField()
            IncrementButton()
        }
    }
}