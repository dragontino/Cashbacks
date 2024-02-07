package com.cashbacks.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.CashbackViewModel
import com.cashbacks.domain.model.BankCard
import kotlinx.coroutines.launch

@Composable
fun SingleCashbackScreen(
    viewModel: CashbackViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
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
            else -> CashbackContent(viewModel, navigateTo, popBackStack)
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbackContent(
    viewModel: CashbackViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = remember {
        fun (message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    if (viewModel.showConfirmDeletionDialog) {
        ConfirmDeletionDialog(
            text = stringResource(R.string.confrim_cashback_deletion),
            onConfirm = {
                viewModel.closeConfirmDeletionDialog()
                viewModel.deleteCashback(showSnackbar)
                popBackStack()
            },
            onDismiss = viewModel::closeConfirmDeletionDialog
        )
    }

    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = AppScreens.Cashback.title(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    if (viewModel.cashbackId != null) {
                        IconButton(onClick = viewModel::openConfirmDeletionDialog) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "delete cashback",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.saveCashback()
                            popBackStack()
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
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
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
    ) { contentPadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ExposedDropdownMenuBox(
                expanded = viewModel.showBankCardsSelection,
                onExpandedChange = viewModel::showBankCardsSelection::set
            ) {
                OutlinedTextField(
                    value = viewModel.cashback.value.bankCard?.toString()
                        ?: stringResource(R.string.value_not_selected),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.bank_card)) },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    leadingIcon = {
                        viewModel.cashback.value.bankCard?.paymentSystem?.let {
                            PaymentSystemMapper.PaymentSystemImage(
                                paymentSystem = it,
                                maxWidth = 30.dp,
                                drawBackground = false
                            )
                        }
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.showBankCardsSelection
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = viewModel.showBankCardsSelection,
                    onDismissRequest = { viewModel.showBankCardsSelection = false },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    viewModel.getAllBankCards().observeAsState().value.let { cards ->
                        when (cards) {
                            null -> Box(contentAlignment = Alignment.Center) {
                                Loading()
                            }

                            else -> CardsList(
                                cards = cards,
                                selectedCard = viewModel.cashback.value.bankCard,
                                onClick = {
                                    viewModel.cashback.value.bankCard = it
                                    viewModel.showBankCardsSelection = false
                                },
                                onAdd = { navigateTo(AppScreens.BankCardEditor.createUrl(null)) }
                            )
                        }
                    }
                }
            }

            EditableTextField(
                text = viewModel.cashback.value.amount,
                label = stringResource(R.string.amount),
                onTextChange = viewModel.cashback.value::amount::set,
                keyboardType = KeyboardType.Number
            )

            EditableTextField(
                text = viewModel.cashback.value.expirationDate,
                label = stringResource(R.string.expiration_date),
                onTextChange = viewModel.cashback.value::expirationDate::set,
                keyboardType = KeyboardType.Number,
            )

            EditableTextField(
                text = viewModel.cashback.value.comment,
                label = stringResource(R.string.comment),
                onTextChange = viewModel.cashback.value::comment::set,
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