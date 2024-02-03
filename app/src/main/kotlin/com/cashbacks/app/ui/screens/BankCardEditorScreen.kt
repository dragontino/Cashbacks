package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmExitDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.ui.theme.DarkerGray
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.BankCardEditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankCardEditorScreen(
    viewModel: BankCardEditorViewModel,
    popBackStack: () -> Unit
) {
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarState.showSnackbar(message) }
        }
    }

    val onBackPress = remember {
        fun () = when {
            viewModel.bankCard.value.haveChanges -> showDialog = true
            else -> popBackStack()
        }
    }

    BackHandler(onBack = onBackPress)

    if (showDialog) {
        ConfirmExitDialog(
            onConfirm = viewModel::saveCard,
            onDismiss = popBackStack,
            onClose = { showDialog = false }
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
                                    text = AppScreens.BankCardEditor.title(),
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
                                        popBackStack()
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
                        modifier = Modifier.padding(contentPadding),
                        showSnackbar = showSnackbar
                    )
                }
            }
        }
    }
}


@Composable
private fun BankCardEditingContent(
    viewModel: BankCardEditorViewModel,
    modifier: Modifier = Modifier,
    showSnackbar: (String) -> Unit
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
            text = bankCard.number,
            onTextChange = {
                bankCard.updateNumber(it)
                bankCard.updateValue(
                    property = bankCard::paymentSystem,
                    newValue = viewModel.getPaymentSystemByNumber(it)
                )
            },
            label = stringResource(R.string.card_number),
            keyboardType = KeyboardType.Decimal
        )

        EditableTextField(
            text = bankCard.paymentSystem?.name ?: stringResource(R.string.value_not_selected),
            onTextChange = {},
            readOnly = true,
            label = stringResource(R.string.payment_system),
            trailingActions = {
                bankCard.paymentSystem?.let {
                    PaymentSystemMapper.PaymentSystemImage(
                        paymentSystem = it,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            },
            modifier = Modifier.clickable {
                showSnackbar("Открыть экран с платежными системами")
            }
        )

        EditableTextField(
            text = bankCard.holder,
            onTextChange = {
                bankCard.updateValue(bankCard::holder, it.uppercase())
            },
            label = stringResource(R.string.card_holder),
            keyboardCapitalization = KeyboardCapitalization.Characters
        )

        EditableTextField(
            text = bankCard.validityPeriod,
            onTextChange = bankCard::updateValidityPeriod,
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
            maxLines = 10,
            imeAction = ImeAction.Default
        )
    }
}