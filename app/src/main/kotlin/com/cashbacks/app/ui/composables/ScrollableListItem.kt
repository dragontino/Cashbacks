package com.cashbacks.app.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.animate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ScrollableListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    hiddenContent: @Composable (RowScope.() -> Unit) = {},
    shape: Shape = ScrollableListItemDefaults.shape(),
    border: BorderStroke = ScrollableListItemDefaults.borderStroke(),
    initialAlignment: Alignment.Horizontal = Alignment.End,
    mainContent: @Composable (() -> Unit)
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    val minOffset = rememberSaveable {
        mutableFloatStateOf(-screenWidth.toFloat())
    }
    val maxOffset = 0f

    val contentOffset = rememberSaveable {
        mutableFloatStateOf(
            if (initialAlignment == Alignment.Start) minOffset.floatValue else maxOffset
        )
    }
    val scope = rememberCoroutineScope()


    suspend fun animateOffset(target: Float) {
        animate(
            initialValue = contentOffset.floatValue,
            targetValue = target,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        ) { offset, _ ->
            contentOffset.floatValue = offset
        }
    }

    suspend fun swipe() {
        val targetOffset = when (contentOffset.floatValue) {
            minOffset.floatValue -> maxOffset
            else -> minOffset.floatValue
        }
        animateOffset(targetOffset)
    }


    suspend fun animateOffset() {
        val targetOffset = when {
            contentOffset.floatValue < minOffset.floatValue / 2 -> minOffset.floatValue
            else -> maxOffset
        }
        animateOffset(targetOffset)
    }


    val scrollableState = rememberScrollableState { delta ->
        contentOffset.floatValue = (contentOffset.floatValue + delta).coerceIn(
            minimumValue = minOffset.floatValue,
            maximumValue = maxOffset
        )
        scope.launch { animateOffset() }
        delta
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = contentOffset.floatValue.roundToInt(),
                        y = 0
                    )
                }
                .background(MaterialTheme.colorScheme.surface.animate())
                .clip(shape)
                .border(border, shape)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (contentOffset.floatValue > minOffset.floatValue) {
                            onClick?.invoke() ?: scope.launch { animateOffset() }
                        }
                    }
                }
                .clickable(
                    indication = LocalIndication.current,
                    interactionSource = remember(::MutableInteractionSource),
                    role = Role.Button,
                    onClick = {
                        if (contentOffset.floatValue > minOffset.floatValue) {
                            onClick?.invoke() ?: scope.launch { swipe() }
                        } else {
                            scope.launch { swipe() }
                        }
                    }
                )
                .scrollable(
                    orientation = Orientation.Horizontal,
                    state = scrollableState
                )
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
                    minOffset.floatValue = -it.size.width - 16f
                }
                .align(Alignment.CenterEnd)
                .wrapContentSize()
        ) {
            Row(content = hiddenContent)
        }
    }
}


object ScrollableListItemDefaults {

    @Composable
    fun borderStroke() = BorderStroke(
        width = 1.5.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
            ).map { it.animate() }
        )
    )

    @Composable
    fun shape() = MaterialTheme.shapes.small
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