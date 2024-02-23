package com.cashbacks.app.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cashbacks.app.ui.managment.ScrollableListItemState
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.animate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ScrollableListItem(
    modifier: Modifier = Modifier,
    state: ScrollableListItemState = rememberScrollableListItemState(
        minOffset = ScrollableListItemDefaults.initialMinOffset,
        initialIsSwiped = false
    ),
    onClick: (() -> Unit)? = null,
    hiddenContent: @Composable (RowScope.() -> Unit) = {},
    shape: Shape = ScrollableListItemDefaults.shape,
    contentWindowInsets: WindowInsets = ScrollableListItemDefaults.contentWindowInsets,
    border: BorderStroke = ScrollableListItemDefaults.borderStroke,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    mainContent: @Composable (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val scrollableState = rememberScrollableState { delta ->
        if (state.canSwipe(delta)) {
            scope.launch { state.onScroll(delta) }
            return@rememberScrollableState delta
        } else {
            return@rememberScrollableState 0f
        }
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = state.contentOffset.floatValue.roundToInt(),
                            y = 0
                        )
                    }
                    .shadow(elevation = 4.dp, shape = shape)
                    .clip(shape)
                    .border(border, shape)
                    .background(containerColor.animate())
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember(::MutableInteractionSource),
                        role = Role.Button,
                        onClick = {
                            when {
                                scrollableState.isScrollInProgress || state.isSwiped.value || onClick == null ->
                                    scope.launch { state.swipe() }

                                else -> onClick.invoke()
                            }
                        }
                    )
                    .scrollable(
                        orientation = Orientation.Horizontal,
                        state = scrollableState
                    )
                    .windowInsetsPadding(contentWindowInsets)
                    .zIndex(2f)
            ) {
                mainContent()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .zIndex(1f)
                    .onGloballyPositioned {
                        state.updateMinOffset(-it.size.width - 16f)
                    }
                    .windowInsetsPadding(contentWindowInsets)
                    .align(Alignment.CenterEnd)
                    .wrapContentSize(),
                content = hiddenContent
            )
        }
    }
}


object ScrollableListItemDefaults {
    val borderStroke: BorderStroke
        @Composable
        get() = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                ).map { it.animate() }
            )
        )

    val shape
        @Composable
        get() = MaterialTheme.shapes.small

    val contentWindowInsets: WindowInsets
        @Composable
        get() = WindowInsets.tappableElement.only(WindowInsetsSides.Horizontal)

    val initialMinOffset
        @Composable
        get() = -LocalConfiguration.current.screenWidthDp.toFloat()
}



@Composable
internal fun EditDeleteContent(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    IconButton(
        onClick = onEditClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary.animate()
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "edit",
            modifier = Modifier.scale(1.1f)
        )
    }

    IconButton(
        onClick = onDeleteClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary.animate()
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = "delete",
            modifier = Modifier.scale(1.1f)
        )
    }
}


@Preview
@Composable
private fun ScrollableListItemPreview() {
    CashbacksTheme(isDarkTheme = false) {
        ScrollableListItem(
            hiddenContent = {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = null)
            }
        ) {
            ElevatedCard(
                shape = MaterialTheme.shapes.small
            ) {
                Text("Test text", modifier = Modifier.padding(16.dp))
            }
        }
    }
}