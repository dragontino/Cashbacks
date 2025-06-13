package com.cashbacks.common.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.model.Header
import com.cashbacks.common.composables.theme.DarkerGray
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.composableLet
import kotlinx.coroutines.launch


@ExperimentalMaterial3Api
@Composable
fun ModalBottomSheet(
    onClose: () -> Unit,
    state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String = "",
    subtitle: String = "",
    beautifulDesign: Boolean = false,
    actions: @Composable (RowScope.() -> Unit)? = null,
    contentWindowInsets: WindowInsets = ModalSheetDefaults.contentWindowInsets,
    bodyContent: @Composable (ColumnScope.() -> Unit)
) {
    ModalBottomSheet(
        onClose = onClose,
        state = state,
        header = Header(title, subtitle, beautifulDesign),
        actions = actions,
        contentWindowInsets = contentWindowInsets,
        bodyContent = bodyContent
    )
}


@ExperimentalMaterial3Api
@Composable
fun ModalBottomSheet(
    onClose: () -> Unit,
    state: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    header: Header = Header(),
    actions: @Composable (RowScope.() -> Unit)? = null,
    contentWindowInsets: WindowInsets = ModalSheetDefaults.contentWindowInsets,
    bodyContent: @Composable (ColumnScope.() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val heightDp = remember { mutableIntStateOf(0) }
    val topWindowInset = contentWindowInsets.asPaddingValues().calculateTopPadding()
    val topPadding = animateDpAsState(
        targetValue = when {
            heightDp.intValue >= configuration.screenHeightDp -> topWindowInset
            else -> 0.dp
        },
        label = "topInsetPadding",
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    val scrollModifier = remember(configuration.orientation, heightDp.intValue) {
        when {
            heightDp.intValue >= configuration.screenHeightDp ->
                Modifier.verticalScroll(scrollState)

            else -> Modifier
        }
    }

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            scope.launch {
                state.hide()
                onClose()
            }
        },
        shape = ModalSheetDefaults.BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface.animate(),
        contentColor = MaterialTheme.colorScheme.onSurface.animate(),
        dragHandle = {
            if (header.isEmpty()) {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(top = topPadding.value)
                )
            }
        },
        contentWindowInsets = { WindowInsets(0) },
        tonalElevation = 40.dp,
        modifier = Modifier.onGloballyPositioned {
            with(density) {
                heightDp.intValue = it.size.height.toDp().value.toInt()
            }
        }
    ) {
        BottomSheetContent(
            modifier = scrollModifier,
            header = header,
            actions = actions,
            contentWindowInsets = with(contentWindowInsets) {
                this.exclude(this.only(WindowInsetsSides.Top))
                    .union(WindowInsets(top = topPadding.value))
            },
            bodyContent = bodyContent
        )
    }
}


@Composable
private fun ColumnScope.BottomSheetContent(
    modifier: Modifier = Modifier,
    header: Header = Header(),
    actions: @Composable (RowScope.() -> Unit)? = null,
    contentWindowInsets: WindowInsets = ModalSheetDefaults.contentWindowInsets,
    bodyContent: @Composable (ColumnScope.() -> Unit),
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .align(Alignment.CenterHorizontally)
            .windowInsetsPadding(contentWindowInsets)
            .padding(bottom = 16.dp)
    ) {
        if (header.isEmpty().not() || actions != null) {
            BottomSheetHeader(header = header, actions = actions)
        }
        bodyContent()
    }
}


@Composable
private fun ColumnScope.BottomSheetHeader(
    header: Header,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    @Composable
    fun Title() {
        Text(
            text = header.title,
            style = when {
                header.beautifulDesign -> MaterialTheme.typography.headlineLarge
                else -> MaterialTheme.typography.titleMedium
            },
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
        )
    }

    @Composable
    fun Subtitle() {
        Text(
            text = header.subtitle,
            color = DarkerGray,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 4.dp)
                .fillMaxWidth()
        )
    }


    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ListItem(
            overlineContent = header.subtitle
                .takeIf { it.isNotBlank() }
                ?.composableLet { Title() },
            headlineContent = {
                when {
                    header.subtitle.isBlank() -> Title()
                    else -> Subtitle()
                }
            },
            trailingContent = actions?.composableLet { actions ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions()
                }
            },
            modifier = modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        )

        HorizontalDivider(color = DarkerGray)
    }
}


object ModalSheetItems {
    @Composable
    fun ColumnScope.TextItem(
        text: String,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        onClick: (text: String) -> Unit
    ) {
        BottomSheetItem(
            text = text,
            modifier = modifier,
            selected = selected,
            onClick = onClick,
        )
    }


    @Composable
    fun ColumnScope.IconTextItem(
        text: String,
        icon: ImageVector,
        iconTintColor: Color,
        modifier: Modifier = Modifier,
        iconModifier: Modifier = Modifier,
        selected: Boolean = false,
        iconAlignment: Alignment.Horizontal = Alignment.Start,
        onClick: (String) -> Unit
    ) {
        val iconContent = @Composable {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTintColor,
                modifier = iconModifier.clip(RoundedCornerShape(40))
            )
        }

        BottomSheetItem(
            text = text,
            onClick = onClick,
            leadingIcon = {
                if (iconAlignment == Alignment.Start) {
                    iconContent()
                }
            },
            trailingIcon = {
                if (iconAlignment == Alignment.End) {
                    iconContent()
                }
            },
            selected = selected,
            modifier = modifier,
        )
    }


    @Composable
    private fun ColumnScope.BottomSheetItem(
        text: String,
        onClick: (text: String) -> Unit,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null
    ) {
        NavigationDrawerItem(
            label = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground.animate(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(2f)
                )
            },
            icon = leadingIcon,
            badge = trailingIcon,
            selected = selected,
            onClick = { onClick(text) },
            shape = MaterialTheme.shapes.small,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = Color.Transparent,
                unselectedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .background(
                    brush = when {
                        selected -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondary,
                            )
                        )

                        else -> SolidColor(Color.Transparent)
                    },
                    shape = MaterialTheme.shapes.small,
                    alpha = .25f
                )
                .then(modifier)
                .align(Alignment.Start)
                .fillMaxWidth()
        )
    }
}


object ModalSheetDefaults {
    private val shapeCornerRadius = 18.dp

    val BottomSheetShape = RoundedCornerShape(
        topStart = shapeCornerRadius,
        topEnd = shapeCornerRadius
    )
    val NavigationDrawerShape = RoundedCornerShape(
        topEnd = shapeCornerRadius,
        bottomEnd = shapeCornerRadius
    )

    @OptIn(ExperimentalLayoutApi::class)
    val contentWindowInsets: WindowInsets
        @Composable
        get() = WindowInsets.systemBarsIgnoringVisibility.only(WindowInsetsSides.Vertical)

    val drawerAnimationSpec = tween<Float>(
        durationMillis = 600,
        easing = FastOutSlowInEasing
    )
}