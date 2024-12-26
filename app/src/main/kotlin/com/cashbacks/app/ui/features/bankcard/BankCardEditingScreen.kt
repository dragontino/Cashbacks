package com.cashbacks.app.ui.features.bankcard

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
import com.cashbacks.app.model.BankCardError
import com.cashbacks.app.model.ComposableBankCard
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EditableTextFieldColors
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.composables.ListDropdownMenu
import com.cashbacks.app.ui.composables.ModalBottomSheet
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingAction
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingEvent
import com.cashbacks.app.ui.managment.BottomSheetType
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.DateUtils.getDisplayableString
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.PaymentSystemUtils
import com.cashbacks.app.util.PaymentSystemUtils.title
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.R
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.util.LocalDateParceler
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankCardEditingScreen(
    viewModel: BankCardEditingViewModel,
    navigateBack: () -> Unit
) {
    val snackbarState = remember(::SnackbarHostState)
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    var bottomSheetType: BottomSheetType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BankCardEditingEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is BankCardEditingEvent.ChangeOpenedDialog -> dialogType = event.openedDialogType
                is BankCardEditingEvent.OpenBottomSheet -> bottomSheetType = event.type
                is BankCardEditingEvent.CloseBottomSheet -> bottomSheetType = null
                is BankCardEditingEvent.NavigateBack -> navigateBack()
            }
        }
    }

    BackHandler {
        when {
            viewModel.bankCard.haveChanges -> viewModel.push(
                BankCardEditingAction.ShowDialog(
                    DialogType.Save
                )
            )

            else -> viewModel.push(BankCardEditingAction.ClickButtonBack)
        }
    }

    if (dialogType == DialogType.Save) {
        ConfirmExitWithSaveDataDialog(
            onConfirm = {
                viewModel.push(
                    BankCardEditingAction.Save {
                        viewModel.push(BankCardEditingAction.ClickButtonBack)
                    }
                )
            },
            onDismiss = {
                viewModel.push(BankCardEditingAction.ClickButtonBack)
            },
            onClose = {
                viewModel.push(BankCardEditingAction.HideDialog)
            }
        )
    }

    bottomSheetType?.let { bottomSheetType ->
        when (bottomSheetType) {
            is ValidityPeriodSelectionBottomSheet -> ValidityPeriodSelectionBottomSheet(
                initialValidityPeriod = bottomSheetType.validityPeriod,
                onConfirm = {
                    viewModel.bankCard.apply { ::validityPeriod updateTo it }
                    viewModel.push(
                        BankCardEditingAction.UpdateErrorMessage(BankCardError.ValidityPeriod)
                    )
                },
                onClose = { viewModel.push(BankCardEditingAction.HideBottomSheet) }
            )

            is MaxCashbacksNumberSelectionBottomSheet -> MaxCashbacksNumberSelectionBottomSheet(
                initialMaxCashbacksNumber = bottomSheetType.maxCashbacksNumber,
                onConfirm = {
                    viewModel.bankCard.apply { ::maxCashbacksNumber updateTo it }
                },
                onClose = { viewModel.push(BankCardEditingAction.HideBottomSheet) }
            )
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
                        targetState = viewModel.bankCard.haveChanges,
                        label = "bank card nav icon anim",
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                when {
                                    isChanged -> viewModel.push(
                                        BankCardEditingAction.ShowDialog(DialogType.Save)
                                    )

                                    else -> viewModel.push(BankCardEditingAction.ClickButtonBack)
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
                    AnimatedVisibility(visible = viewModel.state != ScreenState.Loading) {
                        IconButton(
                            onClick = {
                                viewModel.push(
                                    BankCardEditingAction.Save {
                                        viewModel.push(BankCardEditingAction.ClickButtonBack)
                                    }
                                )
                            },
                            enabled = viewModel.state != ScreenState.Loading
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
            SnackbarHost(snackbarState) {
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
            targetState = viewModel.state,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            label = "state animation"
        ) { state ->
            when (state) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Showing -> BankCardEditingContent(
                    viewModel = viewModel,
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
    viewModel: BankCardEditingViewModel,
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
                value = viewModel.bankCard.number,
                onValueChange = {
                    viewModel.bankCard.updateNumber(it)
                    viewModel.push(BankCardEditingAction.UpdateErrorMessage(BankCardError.Number))
                },
                label = stringResource(R.string.card_number),
                keyboardType = KeyboardType.Decimal,
                error = viewModel.showErrors && BankCardError.Number in viewModel.bankCard.errors,
                errorMessage = viewModel.bankCard.errors[BankCardError.Number],
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = viewModel.showPaymentSystemSelection,
                onExpandedChange = { isExpanded ->
                    val action = when {
                        isExpanded -> BankCardEditingAction.ShowPaymentSystemSelection
                        else -> BankCardEditingAction.HidePaymentSystemSelection
                    }
                    viewModel.push(action)
                }
            ) {
                EditableTextField(
                    text = viewModel.bankCard.paymentSystem.title,
                    onTextChange = {},
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    label = stringResource(R.string.payment_system),
                    leadingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.showPaymentSystemSelection
                        )
                    },
                    trailingActions = {
                        viewModel.bankCard.paymentSystem?.let {
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
                    expanded = viewModel.showPaymentSystemSelection,
                    onClose = { viewModel.push(BankCardEditingAction.HidePaymentSystemSelection) }
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
                                if (viewModel.bankCard.paymentSystem == paymentSystem) {
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
                                viewModel.bankCard.apply { ::paymentSystem updateTo paymentSystem }
                                viewModel.push(BankCardEditingAction.HidePaymentSystemSelection)
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
                text = viewModel.bankCard.holder,
                onTextChange = {
                    viewModel.bankCard.apply { ::holder updateTo it.uppercase() }
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
                text = viewModel.bankCard.validityPeriod
                    ?.getDisplayableString(
                        displayablePattern = ComposableBankCard.VALIDITY_PERIOD_DATE_PATTERN
                    ) ?: "",
                onTextChange = {},
                enabled = false,
                label = stringResource(R.string.validity_period),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable(interactionSource, indication = null) {
                        viewModel.push(
                            BankCardEditingAction.ShowBottomSheet(
                                ValidityPeriodSelectionBottomSheet(viewModel.bankCard.validityPeriod)
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            var isCvvVisible by rememberSaveable { mutableStateOf(false) }
            EditableTextField(
                text = viewModel.bankCard.cvv,
                onTextChange = {
                    if (it.length <= 3) {
                        viewModel.bankCard.apply { ::cvv updateTo it }
                        viewModel.push(
                            BankCardEditingAction.UpdateErrorMessage(BankCardError.Cvv)
                        )
                    }
                },
                label = stringResource(R.string.cvv),
                keyboardType = KeyboardType.Number,
                visualTransformation = EditableTextFieldDefaults
                    .passwordVisualTransformation(isVisible = isCvvVisible),
                error = viewModel.showErrors && BankCardError.Cvv in viewModel.bankCard.errors,
                errorMessage = viewModel.bankCard.errors[BankCardError.Cvv],
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
                text = viewModel.bankCard.pin,
                onTextChange = {
                    if (it.length <= 4) {
                        viewModel.bankCard.apply { ::pin updateTo it }
                        viewModel.push(
                            BankCardEditingAction.UpdateErrorMessage(BankCardError.Pin)
                        )
                    }
                },
                label = stringResource(R.string.pin),
                keyboardType = KeyboardType.Number,
                visualTransformation = EditableTextFieldDefaults
                    .passwordVisualTransformation(isVisible = isPinVisible),
                error = viewModel.showErrors && BankCardError.Pin in viewModel.bankCard.errors,
                errorMessage = viewModel.bankCard.errors[BankCardError.Pin],
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
                text = viewModel.bankCard.name,
                onTextChange = {
                    viewModel.bankCard.apply { ::name updateTo it }
                },
                label = stringResource(R.string.card_name),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = viewModel.bankCard.maxCashbacksNumber?.toString()
                    ?: stringResource(R.string.value_not_selected),
                onTextChange = {},
                enabled = false,
                label = stringResource(R.string.cashbacks_month_limit),
                trailingActions = {
                    if (viewModel.bankCard.maxCashbacksNumber != null) {
                        IconButton(
                            onClick = {
                                viewModel.bankCard.apply { ::maxCashbacksNumber updateTo null }
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
                        viewModel.push(
                            BankCardEditingAction.ShowBottomSheet(
                                type = MaxCashbacksNumberSelectionBottomSheet(
                                    maxCashbacksNumber = viewModel.bankCard.maxCashbacksNumber
                                )
                            )
                        )
                    }
                    .fillMaxWidth()
            )
        }

        item {
            EditableTextField(
                text = viewModel.bankCard.comment,
                onTextChange = {
                    viewModel.bankCard.apply { ::comment updateTo it }
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




@Parcelize
private data class ValidityPeriodSelectionBottomSheet(
    val validityPeriod: @WriteWith<LocalDateParceler> LocalDate?
) : BottomSheetType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValidityPeriodSelectionBottomSheet(
    initialValidityPeriod: LocalDate?,
    onConfirm: (LocalDate?) -> Unit,
    onClose: () -> Unit
) {
    val (month, year) = rememberSaveable {
        val date = initialValidityPeriod ?: Clock.System.today()
        listOf(date.monthNumber, date.year).map { mutableStateOf(it.toString()) }
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
                    month.value = (value % 12 + 1).toString().padStart(2, '0')
                },
                decrementValue = {
                    val value = month.value.toIntOrNull() ?: 2
                    month.value = ((value + 10) % 12 + 1).toString().padStart(2, '0')
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
                    year.value = ((value - 1999) % 100 + 2000).toString()
                },
                decrementValue = {
                    val value = year.value.toIntOrNull() ?: 2
                    year.value = ((value - 1901) % 100 + 2000).toString()
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
                    onConfirm(
                        LocalDate(year = resultYear, monthNumber = resultMonth, dayOfMonth = 1)
                    )
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


@Parcelize
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
                maxCashbacksNumber.value.toIntOrNull()?.plus(1).let {
                    maxCashbacksNumber.value = (it ?: 1).toString()
                }
            },
            decrementValue = {
                maxCashbacksNumber.value.toIntOrNull()?.takeIf { it > 1 }?.let {
                    maxCashbacksNumber.value = (it - 1).toString()
                }
            },
            imeAction = ImeAction.Done,
            textFieldColors = EditableTextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
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
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    inputFieldModifier: Modifier = Modifier,
    controlButtonsOrientation: Orientation = Orientation.Vertical,
    incrementValue: () -> Unit = {
        value.toIntOrNull()?.plus(1)?.toString()?.let(onValueChange)
    },
    decrementValue: () -> Unit = {
        value.toIntOrNull()?.minus(1)?.toString()?.let(onValueChange)
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
                onClick = incrementValue,
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
                onClick = incrementValue,
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
                onClick = decrementValue,
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
                onClick = decrementValue,
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