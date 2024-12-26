package com.cashbacks.app.ui.features.bankcard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.composables.BankCard
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.DataTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardViewingAction
import com.cashbacks.app.ui.features.bankcard.mvi.BankCardViewingEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.BankCardUtils.getDisplayableString
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.mix
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.FullBankCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankCardViewingScreen(
    viewModel: BankCardViewingViewModel,
    navigateToBankCard: (args: BankCardArgs) -> Unit,
    navigateBack: () -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val contentState = rememberScrollState()
    val snackbarState = remember(::SnackbarHostState)
    val clipboardManager = LocalClipboardManager.current
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BankCardViewingEvent.NavigateBack -> navigateBack()
                is BankCardViewingEvent.NavigateToEditingBankCard -> navigateToBankCard(event.args)
                is BankCardViewingEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is BankCardViewingEvent.CopyText -> clipboardManager.setText(event.text)
                is BankCardViewingEvent.ChangeOpenedDialog -> dialogType = event.openedDialogType
            }
        }
    }

    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            val card = type.value as BasicBankCard
            ConfirmDeletionDialog(
                text = stringResource(
                    R.string.confirm_card_deletion,
                    card.name.ifBlank { card.getDisplayableString() }
                ),
                onConfirm = remember {
                    fun() {
                        viewModel.push(
                            BankCardViewingAction.Delete {
                                viewModel.push(BankCardViewingAction.ClickButtonBack)
                            }
                        )
                    }
                },
                onClose = { viewModel.push(BankCardViewingAction.CloseDialog) }
            )
        }

        else -> {}
    }


    CollapsingToolbarScaffold(
        topBarState = topBarState,
        contentState = contentState,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = viewModel.bankCard.name.ifBlank {
                            stringResource(R.string.bank_card)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null,
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = viewModel.state != ScreenState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.onItemClick {
                                    viewModel.push(
                                        BankCardViewingAction.OpenDialog(
                                            DialogType.ConfirmDeletion(viewModel.bankCard)
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "delete card",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = viewModel.state != ScreenState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { viewModel.push(BankCardViewingAction.Edit) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "edit card",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
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
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) {
        Crossfade(
            targetState = viewModel.state,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            label = "state animation"
        ) { state ->
            when (state) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Showing -> ScreenContent(
                    bankCard = viewModel.bankCard,
                    pushAction = viewModel::push,
                    scrollState = contentState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}



@Composable
private fun ScreenContent(
    bankCard: FullBankCard,
    pushAction: (BankCardViewingAction) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
) {
    val context = LocalContext.current
    val screenConfiguration = LocalConfiguration.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        BankCard(
            bankCard = bankCard,
            onCopy = { part, text ->
                pushAction(BankCardViewingAction.CopyText(AnnotatedString(text)))
                val snackbarText = context.getString(
                    R.string.card_part_text_is_copied,
                    part.getDescription(context)
                )
                pushAction(BankCardViewingAction.ShowSnackbar(snackbarText))
            },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp)
                .width(
                    width = minOf(
                        screenConfiguration.screenWidthDp,
                        screenConfiguration.screenHeightDp
                    ).dp
                )
        )

        if (bankCard.name.isNotBlank()) {
            DataTextField(
                text = bankCard.name,
                heading = stringResource(R.string.card_name),
                trailingActions = {
                    IconButton(
                        onClick = {
                            pushAction(BankCardViewingAction.CopyText(AnnotatedString(bankCard.name)))
                            pushAction(
                                BankCardViewingAction.ShowSnackbar(
                                    context.getString(
                                        R.string.card_part_text_is_copied,
                                        context.getString(R.string.card_name_for_copy)
                                    )
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy card name"
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider()
        }

        var isPinVisible by rememberSaveable { mutableStateOf(false) }
        DataTextField(
            text = bankCard.pin,
            heading = stringResource(R.string.pin),
            visualTransformation = EditableTextFieldDefaults
                .passwordVisualTransformation(isPinVisible),
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

                IconButton(
                    onClick = {
                        pushAction(BankCardViewingAction.CopyText(AnnotatedString(bankCard.pin)))
                        pushAction(
                            BankCardViewingAction.ShowSnackbar(
                                context.getString(
                                    R.string.card_part_text_is_copied,
                                    context.getString(R.string.pin_for_copy)
                                )
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "copy card pin code"
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        bankCard.maxCashbacksNumber?.toString()?.let { maxCashbacksNumber ->
            HorizontalDivider()

            DataTextField(
                text = maxCashbacksNumber,
                heading = stringResource(R.string.cashbacks_month_limit),
                trailingActions = {
                    IconButton(
                        onClick = {
                            pushAction(BankCardViewingAction.CopyText(AnnotatedString(maxCashbacksNumber)))
                            pushAction(
                                BankCardViewingAction.ShowSnackbar(
                                    context.getString(
                                        R.string.card_part_text_is_copied,
                                        context.getString(R.string.number_for_copy)
                                    )
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy number"
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (bankCard.comment.isNotBlank()) {
            HorizontalDivider()

            DataTextField(
                text = bankCard.comment,
                heading = stringResource(R.string.comment),
                trailingActions = {
                    IconButton(
                        onClick = {
                            pushAction(BankCardViewingAction.CopyText(AnnotatedString(bankCard.comment)))
                            pushAction(
                                BankCardViewingAction.ShowSnackbar(
                                    context.getString(
                                        R.string.card_part_text_is_copied,
                                        context.getString(R.string.comment_for_copy)
                                    )
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy card comment"
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}


@Preview
@Composable
private fun ScreenContentPreview() {
    CashbacksTheme(isDarkTheme = false) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ScreenContent(
                bankCard = FullBankCard(number = "4444555566667777"),
                pushAction = {}
            )
        }
    }
}