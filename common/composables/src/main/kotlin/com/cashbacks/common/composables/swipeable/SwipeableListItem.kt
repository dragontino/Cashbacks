package com.cashbacks.common.composables.swipeable

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.utils.OnClick
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Stable
@Composable
fun SwipeableListItem(
    onClick: OnClick,
    modifier: Modifier = Modifier,
    state: SwipeableItemState = rememberSwipeableItemState(),
    leftActionIcon: @Composable () -> Unit = {},
    rightActionIcon: @Composable () -> Unit = {},
    shape: Shape = SwipeableListItemDefaults.shape,
    enabled: Boolean = true,
    isEnabledToSwipe: Boolean = true,
    colors: SwipeableListItemColors = SwipeableListItemDefaults.colors(),
    tonalElevation: Dp = 0.dp,
    shadow: Shadow? = SwipeableListItemDefaults.shadow(
        alpha = 1 - (state.swipeOffsetRatio.value / .1f)
    ),
    border: BorderStroke? = null,
    contentWindowInsets: WindowInsets = SwipeableListItemDefaults.contentWindowInsets,
    content: @Composable () -> Unit
) {
    val contentHeightPx = remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val context = LocalContext.current
    val scrollableState = rememberScrollableState { delta ->
        when {
            isEnabledToSwipe && state.canSwipe(SwipeDirection(delta)) -> state.onSwipe(delta)
            else -> 0f
        }
    }
    val flingBehavior = remember {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                state.onSwipeFinished()
                return 0f
            }
        }
    }

    LaunchedEffect(state.isOffsetReachedToTriggerAction.value) {
        if (state.isOffsetReachedToTriggerAction.value) {
            context.vibrate()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.windowInsetsPadding(contentWindowInsets)
    ) {
        if (state.contentOffset.floatValue > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left,
                modifier = Modifier
                    .align(Alignment.CenterStart)

            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(shape)
                        .background(
                            color = colors.leftActionColors.containerColor(
                                swipeRatio = (state.swipeOffsetRatio.value * 2).coerceAtMost(1f)
                            ),
                            shape = shape
                        )
                        .size(
                            width = with(density) {
                                state.contentOffset.floatValue.toDp() - SwipeableListItemDefaults.actionPadding
                            },
                            height = with(density) { contentHeightPx.intValue.toDp() }
                        ),
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.leftActionColors.contentColor(
                            swipeRatio = (state.swipeOffsetRatio.value * 2).coerceAtMost(1f)
                        )
                    ) {
                        leftActionIcon()
                    }
                }

                Spacer(Modifier.width(SwipeableListItemDefaults.actionPadding))
            }
        }

        val shadowModifier = when (shadow) {
            null -> Modifier
            else -> Modifier.dropShadow(shape, shadow)
        }
        Surface(
            onClick = onClick,
            modifier = Modifier
                .offset {
                    IntOffset(x = state.contentOffset.floatValue.roundToInt(), y = 0)
                }
                .then(shadowModifier)
                .scrollable(
                    state = scrollableState,
                    orientation = Orientation.Horizontal,
                    flingBehavior = flingBehavior
                )
                .onSizeChanged {
                    state.updateItemWidth(it.width.toFloat())
                    contentHeightPx.intValue = it.height
                },
            shape = shape,
            enabled = enabled,
            color = colors.containerColor(enabled),
            contentColor = colors.contentColor(enabled),
            tonalElevation = tonalElevation,
            border = border,
            content = content
        )

        if (state.contentOffset.floatValue < 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Spacer(Modifier.width(SwipeableListItemDefaults.actionPadding))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(shape)
                        .background(
                            color = colors.rightActionColors.containerColor(
                                swipeRatio = (state.swipeOffsetRatio.value * 2).coerceAtMost(1f)
                            ),
                            shape = shape
                        )
                        .size(
                            width = with(density) {
                                state.contentOffset.floatValue.absoluteValue.toDp() - SwipeableListItemDefaults.actionPadding
                            },
                            height = with(density) { contentHeightPx.intValue.toDp() }
                        )
                        .clipToBounds()
                ) {
                    CompositionLocalProvider(
                        value = LocalContentColor provides colors.rightActionColors.contentColor(
                            swipeRatio = (state.swipeOffsetRatio.value * 2).coerceAtMost(1f)
                        ),
                        content = rightActionIcon
                    )
                }
            }
        }
    }
}


private fun Context.vibrate() {
    val vibrateTimeMillis = 20L

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    }
    else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = VibrationEffect.createOneShot(vibrateTimeMillis, VibrationEffect.EFFECT_DOUBLE_CLICK)
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(vibrateTimeMillis)
    }
}


@Composable
fun SwipeableListItem(
    actions: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
    state: SwipeableListItemState = rememberSwipeableListItemState(
        minOffset = SwipeableListItemDefaults.initialMinOffset,
        initialIsSwiped = false
    ),
    onClick: OnClick? = null,
    clickIndication: Indication? = LocalIndication.current,
    userSwipeEnabled: Boolean = true,
    shape: Shape = SwipeableListItemDefaults.shape,
    contentWindowInsets: WindowInsets = SwipeableListItemDefaults.contentWindowInsets,
    border: BorderStroke? = SwipeableListItemDefaults.borderStroke,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val scrollableState = rememberScrollableState { delta ->
        when {
            userSwipeEnabled && state.canSwipe(delta) -> state.onPreSwipe(delta)
            else -> 0f
        }
    }
    val flingBehavior = remember {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                state.onPostSwipe()
                return 0f
            }
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
                        state = scrollableState,
                        flingBehavior = flingBehavior
                    )
                    .windowInsetsPadding(contentWindowInsets)
                    .zIndex(2f)
            ) {
                content()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .zIndex(1f)
                    .onSizeChanged { state.coerceMinOffset(-it.width - 16f) }
                    .align(Alignment.CenterEnd)
                    .wrapContentSize(),
                content = actions
            )
        }
    }
}


@Immutable
data class SwipeableListItemColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val leftActionColors: ActionColors,
    val rightActionColors: ActionColors
) {
    @Stable
    fun containerColor(enabled: Boolean): Color = when {
        enabled -> containerColor
        else -> disabledContainerColor
    }

    @Stable
    fun contentColor(enabled: Boolean): Color = when {
        enabled -> contentColor
        else -> disabledContentColor
    }
}



@Immutable
data class ActionColors(
    val containerColor: Color,
    val contentColor: Color,
    val clickedContainerColor: Color,
    val clickedContentColor: Color
) {
    @Stable
    internal fun containerColor(swipeRatio: Float): Color {
        return clickedContainerColor mix containerColor ratio swipeRatio
    }

    @Stable
    internal fun contentColor(swipeRatio: Float): Color {
        return clickedContentColor mix contentColor ratio swipeRatio
    }
}



object SwipeableListItemDefaults {
    val borderStroke: BorderStroke
        @Composable
        get() = BorderStroke(width = 1.5.dp, brush = borderBrush())

    @Stable
    @Composable
    fun borderBrush(colors: List<Color> = borderColors): Brush = Brush.linearGradient(colors)

    val borderColors: List<Color>
        @Stable
        @Composable
        get() = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
    
    @Stable
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.background,
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor: Color = containerColor,
        disabledContentColor: Color = contentColor,
        leftActionColors: ActionColors = actionColors(),
        rightActionColors: ActionColors = actionColors()
    ) = SwipeableListItemColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        leftActionColors = leftActionColors,
        rightActionColors = rightActionColors
    )

    @Stable
    @Composable
    fun actionColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        clickedContainerColor: Color = containerColor,
        clickedContentColor: Color = contentColor
    ) = ActionColors(
        containerColor = containerColor,
        contentColor = contentColor,
        clickedContainerColor = clickedContainerColor,
        clickedContentColor = clickedContentColor
    )


    @Stable
    @Composable
    fun shadow(
        colors: List<Color> = borderColors,
        radius: Dp = 11.dp,
        alpha: Float = 1f
    ) = shadow(
        radius = radius,
        brush = borderBrush(colors),
        alpha = alpha
    )


    @Stable
    @Composable
    fun shadow(
        color: Color,
        radius: Dp = 11.dp,
        alpha: Float = 1f
    ) = shadow(
        radius = radius,
        brush = SolidColor(color),
        alpha = alpha
    )


    @Stable
    @Composable
    fun shadow(
        radius: Dp = 10.dp,
        brush: Brush = borderBrush(),
        spread: Dp = 1.dp,
        alpha: Float = 1f
    ) = Shadow(
        radius = radius,
        brush = brush,
        spread = spread,
        alpha = alpha.coerceIn(0f..1f)
    )


    val shape
        @Composable
        get() = MaterialTheme.shapes.small

    val contentWindowInsets: WindowInsets
        @Composable
        get() = WindowInsets.tappableElement.only(WindowInsetsSides.Horizontal)

    val initialMinOffset
        @Composable
        get() = -LocalWindowInfo.current.containerSize.width.toFloat()

    val defaultItemWidth
        @Composable
        get() = LocalWindowInfo.current.containerSize.width.toFloat()

    val swipeAnimationSpec = tween<Float>(
        durationMillis = 350,
        easing = LinearEasing
    )

    internal val actionPadding = 6.dp
}


@Preview
@Composable
private fun ScrollableListItemPreview() {
    var selected by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color.White) }
    val state = rememberSwipeableItemState(
        leftAction = {
            color = Color.Blue
        },
        rightAction = { color = Color.Red }
    )
    val selectedAnimatable = remember { Animatable(0f) }

    LaunchedEffect(selected) {
        launch {
            val target = if (selected) 1f else 0f
            selectedAnimatable.animateTo(
                targetValue = target,
                animationSpec = SwipeableListItemDefaults.swipeAnimationSpec
            )
        }
    }

    CashbacksTheme(isDarkTheme = false) {
        SwipeableListItem(
            onClick = { selected = !selected },
            state = state,
            leftActionIcon = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "edit",
                    modifier = Modifier.padding(16.dp)
                )
            },
            rightActionIcon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = "delete",
                    modifier = Modifier.padding(16.dp)
                )
            },
            shape = RoundedCornerShape(11.dp * selectedAnimatable.value),
            colors = SwipeableListItemDefaults.colors(
                containerColor = color,
                leftActionColors = SwipeableListItemDefaults.actionColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                rightActionColors = SwipeableListItemDefaults.actionColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selected = !selected }
                    .padding(16.dp)
            ) {
                Text(
                    text = buildString {
                        append("Offset: ")
                        val offset = state.contentOffset.floatValue
                        val roundedOffset = "%.02f".format(abs(offset))
                        if (offset < 0) append('-') else append('+')
                        repeat(3 - abs(offset.toInt()).toString().length) {
                            append('0')
                        }
                        append(roundedOffset)
                    },
                )

                AnimatedVisibility(selected) {
                    Column {
                        Spacer(Modifier.height(32.dp))
                        Text("Hidden Text")
                    }
                }
            }
        }
    }
}