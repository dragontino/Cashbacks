package com.cashbacks.app.ui.features.bankcard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.composables.BankCardCompose
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.DataTextField
import com.cashbacks.app.ui.composables.EditableTextFieldDefaults
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BankCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankCardViewingScreen(
    viewModel: BankCardViewingViewModel,
    navigateToBankCard: (args: BankCardArgs) -> Unit,
    popBackStack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarState.showSnackbar(message) }
        }
    }
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
                is ScreenEvents.Navigate -> (event.args as? BankCardArgs)
                    ?.let(navigateToBankCard)
                    ?: popBackStack()
                is ScreenEvents.ShowSnackbar -> event.message.let(showSnackbar)
            }
        }
    }


    val onCopy = remember {
        fun (text: String) {
            clipboardManager.setText(AnnotatedString(text))
            showSnackbar(context.getString(R.string.text_is_copied))
        }
    }

    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = stringResource(R.string.confirm_card_deletion, (type.value as BankCard).name),
                onConfirm = remember {
                    fun () {
                        viewModel.deleteCard()
                        popBackStack()
                    }
                },
                onClose = viewModel::closeDialog
            )
        }
        else -> {}
    }

    OnLifecycleEvent(onStart = viewModel::refreshCard)


    Crossfade(
        targetState = viewModel.state.value,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "state animation"
    ) { state ->
        when (state) {
            ViewModelState.Loading -> LoadingInBox()
            else -> CollapsingToolbarScaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = viewModel.bankCard.value.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = popBackStack) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = null,
                                    modifier = Modifier.scale(1.2f)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.onItemClick {
                                        viewModel.openDialog(
                                            DialogType.ConfirmDeletion(viewModel.bankCard.value)
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

                            IconButton(
                                onClick = {
                                    navigateToBankCard(
                                        BankCardArgs(id = viewModel.cardId, isEditing = true)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "edit card",
                                    modifier = Modifier.scale(1.2f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                            titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                        )
                    )
                },
                contentState = scrollState,
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
                ScreenContent(
                    bankCard = viewModel.bankCard.value,
                    onCopy = onCopy,
                    scrollState = scrollState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}



@Composable
private fun ScreenContent(
    bankCard: BankCard,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        BankCardCompose(
            bankCard = bankCard,
            onCopy = onCopy,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
        )

        DataTextField(
            text = bankCard.name,
            heading = stringResource(R.string.card_name),
            trailingActions = {
                IconButton(
                    onClick = { onCopy(bankCard.name) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "copy card name"
                    )
                }
            }
        )

        HorizontalDivider()

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

                IconButton(onClick = { onCopy(bankCard.pin) }) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "copy card's pin code"
                    )
                }
            }
        )

        HorizontalDivider()

        if (bankCard.comment.isNotBlank()) {
            DataTextField(
                text = bankCard.comment,
                heading = stringResource(R.string.comment),
                trailingActions = {
                    IconButton(onClick = { onCopy(bankCard.comment) }) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy card comment"
                        )
                    }
                }
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
                bankCard = BankCard(number = "4444555566667777"),
                onCopy = {}
            )
        }
    }
}