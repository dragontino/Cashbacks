package com.cashbacks.app.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.cashbacks.app.R
import com.cashbacks.app.model.ColorDesignMapper.title
import com.cashbacks.app.ui.composables.BottomSheetContent
import com.cashbacks.app.ui.composables.Header
import com.cashbacks.app.ui.composables.ModalSheetItems.TextItem
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.SettingsViewModel
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    openDrawer: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    val showSnackbar = { message: String ->
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(AppScreens.Settings.titleRes),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(imageVector = Icons.Rounded.Menu, contentDescription = "open menu")
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
            SnackbarHost(hostState = scaffoldState.snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    actionColor = MaterialTheme.colorScheme.primary.animate()
                )
            }
        },
        sheetContent = {
            ThemeSheetContent(
                currentDesign = viewModel.settings.colorDesign,
                updateDesign = {
                    viewModel.updateSettingsProperty(
                        property = Settings::colorDesign,
                        value = it.name,
                        error = { exception -> exception.message?.let(showSnackbar) }
                    )
                    scope.launch { scaffoldState.bottomSheetState.hide() }
                }
            )
        },
        sheetContainerColor = MaterialTheme.colorScheme.surface.animate(),
        sheetContentColor = MaterialTheme.colorScheme.onSurface.animate(),
        sheetDragHandle = null,
        sheetPeekHeight = 0.dp,
        sheetShadowElevation = 400.dp,
        sheetTonalElevation = 200.dp
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            SettingsContent(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                openOrHideBottomSheet = {
                    scope.launch {
                        when (scaffoldState.bottomSheetState.currentValue) {
                            SheetValue.Expanded -> scaffoldState.bottomSheetState.hide()
                            else -> scaffoldState.bottomSheetState.expand()
                        }
                    }
                },
                showSnackbar = { showSnackbar(it) }
            )

            AnimatedVisibility(
                visible = viewModel.state == SettingsViewModel.State.Loading,
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
    viewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    openOrHideBottomSheet: () -> Unit,
    showSnackbar: (message: String) -> Unit
) {
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
                    .clickable(
                        enabled = viewModel.state != SettingsViewModel.State.Loading,
                        onClick = openOrHideBottomSheet,
                    )
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground.animate(),
                        shape = MaterialTheme.shapes.small
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
                            append(
                                text = viewModel.constructThemeText(
                                    currentTheme = viewModel.settings.colorDesign,
                                    isDark = isDarkTheme,
                                    context = context
                                )
                            )
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
                    enabled = viewModel.state != SettingsViewModel.State.Loading
                ) {
                    viewModel.updateSettingsProperty(
                        property = Settings::dynamicColor,
                        value = it,
                        error = { exception ->
                            exception.message?.let(showSnackbar)
                        }
                    )
                }
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




@Composable
private fun ColumnScope.ThemeSheetContent(
    currentDesign: ColorDesign,
    updateDesign: (newDesign: ColorDesign) -> Unit,
) = BottomSheetContent {
    ColorDesign.entries.forEach { design ->
        TextItem(
            text = design.title(LocalContext.current),
            selected = design == currentDesign,
            onClick = { updateDesign(design) }
        )
    }
}