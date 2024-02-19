package com.cashbacks.app.ui.features.bankcard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.cashbacks.app.R
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.model.PaymentSystemMapper.title
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.PaymentSystem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankCardEditingScreen(
    viewModel: BankCardEditingViewModel,
    popBackStack: () -> Unit
) {
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarState.showSnackbar(message) }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> popBackStack()
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
            }
        }
    }

    val onBackPress = remember {
        fun () = when {
            viewModel.bankCard.value.haveChanges -> viewModel.openDialog(DialogType.Save)
            else -> viewModel.navigateTo(null)
        }
    }

    BackHandler(onBack = onBackPress)

    if (dialogType == DialogType.Save) {
        ConfirmExitWithSaveDataDialog(
            onConfirm = viewModel::saveCard,
            onDismiss = popBackStack,
            onClose = viewModel::closeDialog
        )
    }

    Crossfade(
        targetState = viewModel.state.value,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "state animation"
    ) { state ->
        when (state) {
            ViewModelState.Loading -> LoadingInBox()
            else -> {
                CollapsingToolbarScaffold(
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
                                            viewModel.bankCard.value.haveChanges -> Icons.Rounded.Close
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
                                        viewModel.saveCard()
                                        viewModel.navigateTo(null)
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
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                ) { contentPadding ->
                    BankCardEditingContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(contentPadding)
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
    modifier: Modifier = Modifier,
) {
    val bankCard = viewModel.bankCard.value

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .background(MaterialTheme.colorScheme.background.animate())
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
    {
        EditableTextField(
            text = bankCard.name,
            onTextChange = { bankCard.updateValue(bankCard::name, it) },
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
            value = bankCard.number,
            onValueChange = {
                bankCard.updateNumber(it)
                bankCard.updateValue(
                    property = bankCard::paymentSystem,
                    newValue = viewModel.getPaymentSystemByNumber(it.text)
                )
            },
            label = stringResource(R.string.card_number),
            keyboardType = KeyboardType.Decimal
        )

        ExposedDropdownMenuBox(
                expanded = viewModel.showPaymentSystemSelection,
                onExpandedChange = viewModel::showPaymentSystemSelection::set
            ) {
            OutlinedTextField(
                value = bankCard.paymentSystem.title,
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
                    bankCard.paymentSystem?.let {
                        PaymentSystemMapper.PaymentSystemImage(
                            paymentSystem = it,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .menuAnchor()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = viewModel.showPaymentSystemSelection,
                onDismissRequest = { viewModel.showPaymentSystemSelection = false },
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
                            if (bankCard.paymentSystem == paymentSystem) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            if (paymentSystem != null) {
                                PaymentSystemMapper.PaymentSystemImage(
                                    paymentSystem = paymentSystem,
                                    drawBackground = false
                                )
                            }
                        },
                        onClick = {
                            bankCard.paymentSystem = paymentSystem
                            viewModel.showPaymentSystemSelection = false
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
            text = bankCard.holder,
            onTextChange = {
                bankCard.updateValue(bankCard::holder, it.uppercase())
            },
            label = stringResource(R.string.card_holder),
            keyboardCapitalization = KeyboardCapitalization.Characters
        )

        EditableTextField(
            value = bankCard.validityPeriod,
            onValueChange = bankCard::updateValidityPeriod,
            label = stringResource(R.string.validity_period),
            keyboardType = KeyboardType.Number
        )
        
        var isCvvVisible by rememberSaveable { mutableStateOf(false) }
        EditableTextField(
            text = bankCard.cvv,
            onTextChange = {
                if (it.length <= 3) {
                    bankCard.updateValue(bankCard::cvv, it)
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
            text = bankCard.pin,
            onTextChange = {
                if (it.length <= 4) {
                    bankCard.updateValue(bankCard::pin, it)
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
            text = bankCard.comment,
            onTextChange = { bankCard.updateValue(bankCard::comment, it) },
            label = stringResource(R.string.comment),
            singleLine = false,
            imeAction = ImeAction.Default
        )
    }
}