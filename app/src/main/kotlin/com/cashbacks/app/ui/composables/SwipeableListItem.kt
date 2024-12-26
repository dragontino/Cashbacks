package com.cashbacks.app.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
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
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
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
import com.cashbacks.app.ui.managment.SwipeableListItemState
import com.cashbacks.app.ui.managment.rememberSwipeableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.OnClick
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableListItem(
    modifier: Modifier = Modifier,
    state: SwipeableListItemState = rememberSwipeableListItemState(
        minOffset = SwipeableListItemDefaults.initialMinOffset,
        initialIsSwiped = false
    ),
    onClick: OnClick? = null,
    clickIndication: Indication? = LocalIndication.current,
    swipeEnabled: Boolean = true,
    hiddenContent: @Composable (RowScope.() -> Unit) = {},
    shape: Shape = SwipeableListItemDefaults.shape,
    contentWindowInsets: WindowInsets = SwipeableListItemDefaults.contentWindowInsets,
    border: BorderStroke? = SwipeableListItemDefaults.borderStroke,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    mainContent: @Composable (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val scrollableState = rememberScrollableState { delta ->
        if (swipeEnabled && state.canSwipe(delta)) {
            scope.launch { state.onSwipe(delta) }
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
            val borderModifier = border?.let { Modifier.border(it, shape) } ?: Modifier

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
                    .then(borderModifier)
                    .background(containerColor)
                    .clickable(
                        indication = clickIndication,
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
                    .onGloballyPositioned { state.coerceMinOffset(-it.size.width - 16f) }
                    .windowInsetsPadding(contentWindowInsets)
                    .align(Alignment.CenterEnd)
                    .wrapContentSize(),
                content = hiddenContent
            )
        }
    }
}


object SwipeableListItemDefaults {
    val borderStroke: BorderStroke
        @Composable
        get() = BorderStroke(width = 1.5.dp, brush = borderBrush())

    @Composable
    fun borderBrush(colors: List<Color> = borderColors): Brush = Brush.linearGradient(colors)

    val borderColors: List<Color>
        @Composable
        get() = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
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
internal fun RowScope.EditDeleteContent(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    editButtonColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    deleteButtonColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    IconButton(onClick = onEditClick, colors = editButtonColors) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "edit",
            modifier = Modifier.scale(1.1f)
        )
    }

    IconButton(onClick = onDeleteClick, colors = deleteButtonColors) {
        Icon(
            imageVector = Icons.Rounded.DeleteForever,
            contentDescription = "delete",
            modifier = Modifier.scale(1.1f)
        )
    }
}


@Preview
@Composable
private fun ScrollableListItemPreview() {
    CashbacksTheme(isDarkTheme = false) {
        SwipeableListItem(
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