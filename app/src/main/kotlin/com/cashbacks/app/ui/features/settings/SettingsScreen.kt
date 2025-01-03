package com.cashbacks.app.ui.features.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashbacks.app.ui.composables.Header
import com.cashbacks.app.ui.composables.ModalBottomSheet
import com.cashbacks.app.ui.composables.ModalSheetItems.IconTextItem
import com.cashbacks.app.ui.features.settings.mvi.SettingsAction
import com.cashbacks.app.ui.features.settings.mvi.SettingsEvent
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.ColorDesignUtils.icon
import com.cashbacks.app.util.ColorDesignUtils.isDark
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.ColorDesign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navigateBack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsEvent.NavigateBack -> navigateBack()
                is SettingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

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
                        onClick = { viewModel.push(SettingsAction.ClickButtonBack) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    actionColor = MaterialTheme.colorScheme.primary.animate()
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            SettingsContent(viewModel = viewModel)

            AnimatedVisibility(
                visible = viewModel.state == ScreenState.Loading,
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
private fun SettingsContent(viewModel: SettingsViewModel) {
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

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
                    .clickable(enabled = viewModel.state != ScreenState.Loading) {
                        isSheetOpen = true
                    }
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground.animate(),
                        shape = MaterialTheme.shapes.small
                    )
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                    .background(MaterialTheme.colorScheme.surface.animate())
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.switchThemeText), " ")

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.7.sp,
                                shadow = Shadow(
                                    color = MaterialTheme.colorScheme.primary.animate(),
                                    blurRadius = 0.4f
                                ),
                                fontSynthesis = FontSynthesis.Weight
                            ),
                        ) {
                            val currentDesign = viewModel.settings.colorDesign
                            append(currentDesign.getTitle(context.resources).lowercase())
                            if (currentDesign == ColorDesign.System) {
                                val textToAppend = when {
                                    currentDesign.isDark -> stringResource(R.string.dark_scheme)
                                    else -> stringResource(R.string.light_scheme)
                                }

                                append("\n", stringResource(R.string.now, textToAppend).lowercase())
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.weight(10f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.animate(),
                    modifier = Modifier
                        .scale(.6f)
                        .weight(1f)
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                SwitchItem(
                    isChecked = viewModel.settings.dynamicColor,
                    header = Header(
                        title = stringResource(R.string.dynamic_color),
                        subtitle = stringResource(R.string.dynamic_color_desc)
                    ),
                    enabled = viewModel.state != ScreenState.Loading
                ) { isChecked ->
                    viewModel.push(
                        SettingsAction.UpdateSetting { it.copy(dynamicColor = isChecked) }
                    )
                }
            }
        }


        item {
            SwitchItem(
                isChecked = viewModel.settings.autoDeleteExpiredCashbacks,
                header = Header(
                    title = stringResource(R.string.auto_delete_expired_cashbacks)
                ),
                enabled = viewModel.state != ScreenState.Loading
            ) { isChecked ->
                viewModel.push(
                    SettingsAction.UpdateSetting { it.copy(autoDeleteExpiredCashbacks = isChecked) }
                )
            }
        }
    }

    if (isSheetOpen) {
        ThemeBottomSheet(
            currentDesign = viewModel.settings.colorDesign,
            updateDesign = { newDesign ->
                viewModel.push(
                    SettingsAction.UpdateSetting {
                        it.copy(colorDesign = newDesign)
                    }
                )
            },
            onClose = { isSheetOpen = false }
        )
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
                color = MaterialTheme.colorScheme.onBackground.animate(),
                shape = MaterialTheme.shapes.small
            )
            .background(MaterialTheme.colorScheme.surface.animate())
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
                color = MaterialTheme.colorScheme.onBackground.animate()
            )

            if (header.subtitle.isNotBlank()) {
                Text(
                    text = header.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f).animate()
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary.animate(),
                checkedTrackColor = MaterialTheme.colorScheme.primary.animate(),
                uncheckedThumbColor = MaterialTheme.colorScheme.onTertiary.animate(),
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
                text = design.getTitle(LocalContext.current.resources),
                selected = design == currentDesign,
                iconTintColor = MaterialTheme.colorScheme.primary.animate(),
                onClick = {
                    updateDesign(design)
                    onClose()
                }
            )
        }
    }
}