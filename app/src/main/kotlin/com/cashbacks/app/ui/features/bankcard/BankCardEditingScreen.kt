package com.cashbacks.app.ui.features.bankcard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cashbacks.app.model.PaymentSystemUtils
import com.cashbacks.app.model.PaymentSystemUtils.title
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingAction
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardEditingEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.mix
import com.cashbacks.domain.R
import com.cashbacks.domain.model.PaymentSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankCardEditingScreen(
    viewModel: BankCardEditingViewModel,
    navigateBack: () -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val contentState = rememberScrollState()
    val snackbarState = remember(::SnackbarHostState)
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BankCardEditingEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is BankCardEditingEvent.ChangeOpenedDialog -> dialogType = event.openedDialogType
                is BankCardEditingEvent.NavigateBack -> navigateBack()
            }
        }
    }

    val onBackPress = remember {
        fun () = when {
            viewModel.bankCard.haveChanges -> viewModel. push(BankCardEditingAction.OpenDialog(DialogType.Save))
            else -> viewModel.push(BankCardEditingAction.ClickButtonBack)
        }
    }

    BackHandler(onBack = onBackPress)

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
                viewModel.push(BankCardEditingAction.CloseDialog)
            }
        )
    }

    Crossfade(
        targetState = viewModel.state,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "state animation"
    ) { state ->
        when (state) {
            ScreenState.Loading -> LoadingInBox()
            ScreenState.Showing -> {
                CollapsingToolbarScaffold(
                    topBarState = topBarState,
                    contentState = contentState,
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
                                IconButton(onClick = onBackPress) {
                                    Icon(
                                        imageVector = when {
                                            viewModel.bankCard.haveChanges -> Icons.Rounded.Close
                                            else -> Icons.Rounded.ArrowBackIosNew
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.scale(1.2f)
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        viewModel.push(
                                            BankCardEditingAction.Save {
                                                viewModel.push(BankCardEditingAction.ClickButtonBack)
                                            }
                                        )
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Save,
                                        contentDescription = "save card",
                                        modifier = Modifier.scale(1.2f)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                                    .mix(MaterialTheme.colorScheme.primary)
                                    .ratio(topBarState.overlappedFraction),
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
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                ) {
                    BankCardEditingContent(
                        viewModel = viewModel,
                        state = contentState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankCardEditingContent(
    viewModel: BankCardEditingViewModel,
    state: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .background(MaterialTheme.colorScheme.background.animate())
            .fillMaxSize()
            .verticalScroll(state)
            .padding(16.dp)
    )
    {
        EditableTextField(
            text = viewModel.bankCard.name,
            onTextChange = {
                viewModel.bankCard.apply { ::name updateTo it }
            },
            label = stringResource(R.string.card_name)
        )

        Text(
            text = stringResource(R.string.main_info),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DarkerGray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        EditableTextField(
            value = viewModel.bankCard.number,
            onValueChange = { viewModel.bankCard.updateNumber(it) },
            label = stringResource(R.string.card_number),
            keyboardType = KeyboardType.Decimal
        )

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
            OutlinedTextField(
                value = viewModel.bankCard.paymentSystem.title,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { stringResource(R.string.payment_system) },
                leadingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = viewModel.showPaymentSystemSelection
                    )
                },
                trailingIcon = {
                    viewModel.bankCard.paymentSystem?.let {
                        PaymentSystemUtils.PaymentSystemImage(
                            paymentSystem = it,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = viewModel.showPaymentSystemSelection,
                onDismissRequest = {
                    viewModel.push(BankCardEditingAction.HidePaymentSystemSelection)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                val options = listOf(null) + PaymentSystem.entries
                options.forEach { paymentSystem ->
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
                                    drawBackground = false
                                )
                            }
                        },
                        onClick = {
                            viewModel.bankCard.paymentSystem = paymentSystem
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

        EditableTextField(
            text = viewModel.bankCard.holder,
            onTextChange = {
                viewModel.bankCard.apply { ::holder updateTo it.uppercase() }
            },
            label = stringResource(R.string.card_holder),
            keyboardCapitalization = KeyboardCapitalization.Characters
        )

        EditableTextField(
            value = viewModel.bankCard.validityPeriod,
            onValueChange = viewModel.bankCard::updateValidityPeriod,
            label = stringResource(R.string.validity_period),
            keyboardType = KeyboardType.Number
        )
        
        var isCvvVisible by rememberSaveable { mutableStateOf(false) }
        EditableTextField(
            text = viewModel.bankCard.cvv,
            onTextChange = {
                if (it.length <= 3) {
                    viewModel.bankCard.apply { ::cvv updateTo it }
                }
            },
            label = stringResource(R.string.cvv),
            keyboardType = KeyboardType.Number,
            visualTransformation = EditableTextFieldDefaults
                .passwordVisualTransformation(isVisible = isCvvVisible),
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
            }
        )

        var isPinVisible by rememberSaveable { mutableStateOf(false) }
        EditableTextField(
            text = viewModel.bankCard.pin,
            onTextChange = {
                if (it.length <= 4) {
                    viewModel.bankCard.apply { ::pin updateTo it }
                }
            },
            label = stringResource(R.string.pin),
            keyboardType = KeyboardType.Number,
            visualTransformation = EditableTextFieldDefaults
                .passwordVisualTransformation(isVisible = isPinVisible),
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
        )

        Text(
            text = stringResource(R.string.additional_info),
            style = MaterialTheme.typography.labelSmall,
            color = DarkerGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        EditableTextField(
            text = viewModel.bankCard.comment,
            onTextChange = {
                viewModel.bankCard.apply { ::comment updateTo it }
            },
            label = stringResource(R.string.comment),
            singleLine = false,
            imeAction = ImeAction.Default
        )
    }
}