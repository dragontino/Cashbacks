package com.cashbacks.features.bankcard.presentation.impl.ui

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.DataTextField
import com.cashbacks.common.composables.EditableTextFieldDefaults
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.resources.R
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.api.composables.BankCard
import com.cashbacks.features.bankcard.presentation.api.utils.BankCardPresentationUtils.getDisplayableString
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingIntent
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingLabel
import com.cashbacks.features.bankcard.presentation.impl.mvi.ViewingState
import com.cashbacks.features.bankcard.presentation.impl.viewmodel.BankCardViewingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
internal fun BankCardViewingRoot(
    navigateBack: () -> Unit,
    navigateToBankCard: (BankCardArgs) -> Unit,
    viewModel: BankCardViewingViewModel = koinViewModel()
) {
    val viewingState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarState = remember(::SnackbarHostState)
    val clipboard = LocalClipboard.current
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.labelsFlow.collect { label ->
            when (label) {
                is ViewingLabel.NavigateBack -> navigateBack()
                is ViewingLabel.NavigateToEditingBankCard -> navigateToBankCard(label.args)
                is ViewingLabel.DisplayMessage -> launch {
                    snackbarState.showSnackbar(label.message)
                }
                is ViewingLabel.CopyText -> {
                    val clipData = ClipData.newPlainText("BankCardViewing", label.text)
                    clipboard.setClipEntry(ClipEntry(clipData))
                }
                is ViewingLabel.ChangeOpenedDialog -> dialogType = label.openedDialogType
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
                onConfirm = { viewModel.sendIntent(ViewingIntent.ClickButtonBack) },
                onClose = { viewModel.sendIntent(ViewingIntent.CloseDialog) }
            )
        }
    }


    BankCardViewingScreen(
        state = viewingState,
        snackbarHostState = snackbarState,
        sendIntent = viewModel::sendIntent
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankCardViewingScreen(
    state: ViewingState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (ViewingIntent) -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val contentState = rememberLazyListState()


    CollapsingToolbarScaffold(
        topBarState = topBarState,
        contentState = contentState,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.card.name.ifBlank {
                            stringResource(R.string.bank_card)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { sendIntent(ViewingIntent.ClickButtonBack) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null,
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.screenState != ScreenState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                sendIntent(
                                    ViewingIntent.OpenDialog(
                                        DialogType.ConfirmDeletion(state.card)
                                    )
                                )
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
                        visible = state.screenState != ScreenState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { sendIntent(ViewingIntent.Edit) }
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
            SnackbarHost(snackbarHostState) {
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
            targetState = state.screenState,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            label = "state animation"
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Stable -> ScreenContent(
                    state = state,
                    sendIntent = sendIntent,
                    lazyListState = contentState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}



@Composable
private fun ScreenContent(
    state: ViewingState,
    sendIntent: (ViewingIntent) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            BankCard(
                bankCard = state.card,
                onCopy = { part, text ->
                    sendIntent(ViewingIntent.CopyText(AnnotatedString(text)))
                    val snackbarText = context.getString(
                        R.string.card_part_text_is_copied,
                        part.getDescription(context)
                    )
                    sendIntent(ViewingIntent.DisplayMessage(snackbarText))
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
                    .width(
                        width = with(LocalDensity.current) {
                            minOf(
                                windowInfo.containerSize.width,
                                windowInfo.containerSize.height
                            ).toDp()
                        }
                    )
            )
        }

        if (state.card.name.isNotBlank()) {
            item {
                DataTextField(
                    text = state.card.name,
                    heading = stringResource(R.string.card_name),
                    trailingActions = {
                        IconButton(
                            onClick = {
                                sendIntent(ViewingIntent.CopyText(AnnotatedString(state.card.name)))
                                sendIntent(
                                    ViewingIntent.DisplayMessage(
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
        }

        item {
            var isPinVisible by rememberSaveable { mutableStateOf(false) }
            DataTextField(
                text = state.card.pin,
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
                            sendIntent(ViewingIntent.CopyText(AnnotatedString(state.card.pin)))
                            sendIntent(
                                ViewingIntent.DisplayMessage(
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
        }

        state.card.maxCashbacksNumber?.toString()?.let { maxCashbacksNumber ->
            item {
                HorizontalDivider()
            }

            item {
                DataTextField(
                    text = maxCashbacksNumber,
                    heading = stringResource(R.string.cashbacks_month_limit),
                    trailingActions = {
                        IconButton(
                            onClick = {
                                sendIntent(ViewingIntent.CopyText(AnnotatedString(maxCashbacksNumber)))
                                sendIntent(
                                    ViewingIntent.DisplayMessage(
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
        }

        if (state.card.comment.isNotBlank()) {
            item {
                HorizontalDivider()
            }

            item {
                DataTextField(
                    text = state.card.comment,
                    heading = stringResource(R.string.comment),
                    trailingActions = {
                        IconButton(
                            onClick = {
                                sendIntent(ViewingIntent.CopyText(AnnotatedString(state.card.comment)))
                                sendIntent(
                                    ViewingIntent.DisplayMessage(
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
}



@Preview
@Composable
private fun ScreenContentPreview() {
    CashbacksTheme(isDarkTheme = false) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ScreenContent(
                state = ViewingState(
                    card = FullBankCard(number = "4444555566667777")
                ),
                sendIntent = {}
            )
        }
    }
}