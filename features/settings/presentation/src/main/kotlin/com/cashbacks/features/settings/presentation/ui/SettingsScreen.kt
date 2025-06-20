package com.cashbacks.features.settings.presentation.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.ModalBottomSheet
import com.cashbacks.common.composables.ModalSheetItems.IconTextItem
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.model.Header
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.resources.R
import com.cashbacks.features.settings.domain.model.ColorDesign
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.presentation.SettingsViewModel
import com.cashbacks.features.settings.presentation.mvi.SettingsIntent
import com.cashbacks.features.settings.presentation.mvi.SettingsLabel
import com.cashbacks.features.settings.presentation.mvi.SettingsState
import com.cashbacks.features.settings.presentation.utils.icon
import com.cashbacks.features.settings.presentation.utils.title
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoot(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is SettingsLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }

                is SettingsLabel.NavigateBack -> navigateBack()
            }
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        sendIntent = viewModel::sendIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (SettingsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { sendIntent(SettingsIntent.ClickButtonBack) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            SettingsContent(state, sendIntent)

            AnimatedVisibility(
                visible = state.screenState == ScreenState.Loading,
                enter = fadeIn(tween(durationMillis = 50)),
                exit = fadeOut(tween(durationMillis = 50))
            ) {
                LoadingInBox(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = .9f),
                    loadingModifier = Modifier.scale(2.2f),
                )
            }
        }
    }
}



@Composable
private fun SettingsContent(
    state: SettingsState,
    sendIntent: (SettingsIntent) -> Unit
) {
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = state.screenState != ScreenState.Loading) {
                        isSheetOpen = true
                    }
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = MaterialTheme.shapes.small
                    )
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                ColorDesignText(
                    colorDesign = state.settings.colorDesign,
                    modifier = Modifier.weight(2f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .scale(.6f)
                        .padding(horizontal = 8.dp)
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                SwitchItem(
                    isChecked = state.settings.dynamicColor,
                    header = Header(
                        title = stringResource(R.string.dynamic_color),
                        subtitle = stringResource(R.string.dynamic_color_desc)
                    ),
                    enabled = state.screenState != ScreenState.Loading
                ) { isChecked ->
                    sendIntent(
                        SettingsIntent.UpdateSetting { it.copy(dynamicColor = isChecked) }
                    )
                }
            }
        }


        item {
            SwitchItem(
                isChecked = state.settings.autoDeleteExpiredCashbacks,
                header = Header(
                    title = stringResource(R.string.auto_delete_expired_cashbacks)
                ),
                enabled = state.screenState != ScreenState.Loading
            ) { isChecked ->
                sendIntent(
                    SettingsIntent.UpdateSetting {
                        it.copy(autoDeleteExpiredCashbacks = isChecked)
                    }
                )
            }
        }
    }

    if (isSheetOpen) {
        ThemeBottomSheet(
            currentDesign = state.settings.colorDesign,
            updateDesign = { newDesign ->
                sendIntent(
                    SettingsIntent.UpdateSetting {
                        it.copy(colorDesign = newDesign)
                    },
                )
            },
            onClose = { isSheetOpen = false }
        )
    }
}


private enum class ColorDesignTextId {
    Title,
    Content
}

@Composable
private fun ColorDesignText(
    colorDesign: ColorDesign,
    modifier: Modifier = Modifier
) {
    Layout(
        content = {
            Text(
                text = buildString {
                    if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
                        append(":")
                    }
                    append(stringResource(R.string.switchThemeText))
                    if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
                        append(":")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                lineHeight = 22.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .layoutId(ColorDesignTextId.Title)
                    .wrapContentSize(),
            )

            Text(
                text = colorDesign.title.lowercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary,
                        blurRadius = 0.4f
                    ),
                    fontSynthesis = FontSynthesis.Weight
                ),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.7.sp,
                lineHeight = 22.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = Modifier
                    .layoutId(ColorDesignTextId.Content)
                    .wrapContentSize()
            )
        },
        modifier = modifier.wrapContentSize(),
    ) { measurables, constraints ->
        val titleContentPadding = 8.dp.roundToPx()

        val titlePlaceable = measurables
            .first { it.layoutId == ColorDesignTextId.Title }
            .measure(constraints)

        val contentPlaceable = measurables
            .first { it.layoutId == ColorDesignTextId.Content }
            .measure(constraints)

        val freeSpaceInFirstLine = constraints.maxWidth - titlePlaceable.width - titleContentPadding

        val height = when {
            freeSpaceInFirstLine >= contentPlaceable.width -> {
                maxOf(titlePlaceable.height, contentPlaceable.height)
            }

            else -> {
                titlePlaceable.height + contentPlaceable.height + titleContentPadding
            }
        }

        layout(constraints.maxWidth, height) {
            if (freeSpaceInFirstLine >= contentPlaceable.width) {
                titlePlaceable.place(
                    x = when (layoutDirection) {
                        LayoutDirection.Ltr -> 0
                        LayoutDirection.Rtl -> constraints.maxWidth - titlePlaceable.width
                    },
                    y = (height - titlePlaceable.height) / 2
                )
                contentPlaceable.place(
                    x = when (layoutDirection) {
                        LayoutDirection.Ltr -> titlePlaceable.width + titleContentPadding
                        LayoutDirection.Rtl -> constraints.maxWidth -
                                titlePlaceable.width -
                                titleContentPadding -
                                contentPlaceable.width
                    },
                    y = (height - contentPlaceable.height) / 2
                )
            } else {
                titlePlaceable.place(
                    x = when (layoutDirection) {
                        LayoutDirection.Ltr -> 0
                        LayoutDirection.Rtl -> constraints.maxWidth - titlePlaceable.width
                    },
                    y = 0
                )
                contentPlaceable.place(
                    x = when (layoutDirection) {
                        LayoutDirection.Ltr -> 0
                        LayoutDirection.Rtl -> constraints.maxWidth - contentPlaceable.width
                    },
                    y = titlePlaceable.height + titleContentPadding
                )
            }
        }
    }
}



@Composable
private fun SwitchItem(
    isChecked: Boolean,
    header: Header,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (isChecked: Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.small
            )
            .background(MaterialTheme.colorScheme.surface)
            .toggleable(
                value = isChecked,
                role = Role.Switch,
                enabled = enabled,
                onValueChange = onClick
            )
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = header.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (header.subtitle.isNotBlank()) {
                Text(
                    text = header.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onTertiary,
                uncheckedTrackColor = MaterialTheme.colorScheme.tertiary.animate(),
                uncheckedBorderColor = MaterialTheme.colorScheme.secondary.animate(),
            ),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeBottomSheet(
    currentDesign: ColorDesign,
    updateDesign: (newDesign: ColorDesign) -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onClose = onClose) {
        ColorDesign.entries.forEach { design ->
            IconTextItem(
                icon = design.icon,
                text = design.title,
                selected = design == currentDesign,
                iconTintColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    updateDesign(design)
                    onClose()
                }
            )
        }
    }
}




@Preview(locale = "ru")
@Composable
private fun SettingsScreenPreview() {
    CashbacksTheme {
        SettingsScreen(
            state = SettingsState(settings = Settings(colorDesign = ColorDesign.System)),
            snackbarHostState = remember(::SnackbarHostState),
            sendIntent = {}
        )
    }
}