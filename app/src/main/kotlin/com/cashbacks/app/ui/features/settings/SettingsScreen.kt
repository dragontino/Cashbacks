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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shadow
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
import com.cashbacks.app.model.ColorDesignMapper.icon
import com.cashbacks.app.model.ColorDesignMapper.title
import com.cashbacks.app.ui.composables.BottomSheetContent
import com.cashbacks.app.ui.composables.Header
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.ModalSheetItems.IconTextItem
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    popBackStack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            if (event is ScreenEvents.ShowSnackbar) {
                showSnackbar(event.message)
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
                    IconButton(onClick = popBackStack) {
                        Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "open menu")
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
                visible = viewModel.state.value == ViewModelState.Loading,
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(viewModel: SettingsViewModel) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
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
                    .clickable(enabled = viewModel.state.value != ViewModelState.Loading) {
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
                            append(viewModel.constructThemeText())
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
                    isChecked = viewModel.settings.value.dynamicColor,
                    header = Header(
                        title = stringResource(R.string.dynamic_color),
                        subtitle = stringResource(R.string.dynamic_color_desc)
                    ),
                    enabled = viewModel.state.value != ViewModelState.Loading
                ) {
                    viewModel.updateSettingsProperty(
                        property = Settings::dynamicColor,
                        value = it
                    )
                }
            }
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { isSheetOpen = false },
            shape = ModalSheetDefaults.BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface.animate(),
            contentColor = MaterialTheme.colorScheme.onSurface.animate(),
            dragHandle = null,
            windowInsets = WindowInsets(0),
            tonalElevation = 40.dp
        ) {
            ThemeSheetContent(
                currentDesign = viewModel.settings.value.colorDesign,
                updateDesign = {
                    viewModel.updateSettingsProperty(
                        property = Settings::colorDesign,
                        value = it.name
                    )
                    scope.launch {
                        bottomSheetState.hide()
                        delay(50)
                        isSheetOpen = false
                    }
                }
            )
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
        IconTextItem(
            icon = design.icon,
            text = design.title,
            selected = design == currentDesign,
            iconTintColor = MaterialTheme.colorScheme.primary.animate(),
            onClick = { updateDesign(design) }
        )
    }
}