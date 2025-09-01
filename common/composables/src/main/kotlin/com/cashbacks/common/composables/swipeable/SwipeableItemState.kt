package com.cashbacks.common.composables.swipeable

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.os.bundleOf
import kotlin.math.absoluteValue

@Stable
sealed class SwipeableItemState {
    abstract val contentOffset: FloatState

    /**
     * Calculates ratio of [contentOffset] to max offset.
     *
     * Possible values: from <i>0</i> to <i>1,</i> where:
     * - 0 -> [contentOffset] is 0f
     * - 1 -> [contentOffset] is max offset
     */
    abstract val swipeOffsetRatio: State<Float>

    abstract val isOnSwipe: State<Boolean>

    internal abstract val isOffsetReachedToTriggerAction: State<Boolean>

    abstract suspend fun leftAction()

    abstract suspend fun rightAction()

    abstract fun updateItemWidth(width: Float)

    abstract suspend fun swipeToZero()

    abstract suspend fun swipeToLeft()

    abstract suspend fun swipeToRight()

    internal abstract fun canSwipe(direction: SwipeDirection): Boolean

    internal abstract fun onSwipe(delta: Float): Float

    internal abstract suspend fun onSwipeFinished()
}


@Immutable
enum class SwipeDirection {
    Ltr,
    Rtl
}


/**
 * Определяет, можно ли свайпать
 * @param scrollDelta направление свайпа. Значение < 0 означает свайп влево, значение > 0 — свайп вправо
 */
@Stable
fun SwipeDirection(scrollDelta: Float): SwipeDirection = when {
    scrollDelta < 0 -> SwipeDirection.Rtl
    else -> SwipeDirection.Ltr
}


@Stable
internal class SwipeableItemStateImpl(
    initialOffset: Float,
    initialWidth: Float,
    private val swipeEnabled: (SwipeDirection) -> Boolean,
    private val leftAction: suspend SwipeableItemState.() -> Unit,
    private val rightAction: suspend SwipeableItemState.() -> Unit
) : SwipeableItemState() {

    private val itemWidth = mutableFloatStateOf(initialWidth)

    override val contentOffset = mutableFloatStateOf(initialOffset)

    override val swipeOffsetRatio = derivedStateOf {
        contentOffset.floatValue.absoluteValue / itemWidth.floatValue
    }

    override val isOnSwipe = derivedStateOf { contentOffset.floatValue != 0f }

    override val isOffsetReachedToTriggerAction = derivedStateOf {
        isLeftActionReached() || isRightActionReached()
    }

    override fun updateItemWidth(width: Float) {
        itemWidth.floatValue = width
    }

    override fun canSwipe(direction: SwipeDirection): Boolean {
        return swipeEnabled(direction) || when (direction) {
            SwipeDirection.Ltr -> contentOffset.floatValue < 0f
            SwipeDirection.Rtl -> contentOffset.floatValue > 0f
        }
    }

    override fun onSwipe(delta: Float): Float {
        val previousOffset = contentOffset.floatValue
        contentOffset.floatValue = (contentOffset.floatValue + delta).coerceIn(
            minimumValue = -itemWidth.floatValue,
            maximumValue = itemWidth.floatValue
        )
        val consumedOffset = contentOffset.floatValue - previousOffset
        return consumedOffset
    }

    override suspend fun onSwipeFinished() {
        when {
            isLeftActionReached() -> leftAction()
            isRightActionReached() -> rightAction()
        }
        swipeToZero()
    }

    override suspend fun swipeToLeft() {
        animateOffset(-itemWidth.floatValue)
    }

    override suspend fun swipeToRight() {
        animateOffset(itemWidth.floatValue)
    }

    override suspend fun swipeToZero() {
        animateOffset(0f)
    }

    override suspend fun leftAction() {
        leftAction(this)
    }
    override suspend fun rightAction() {
        rightAction(this)
    }

    private suspend fun animateOffset(target: Float) {
        animate(
            initialValue = contentOffset.floatValue,
            targetValue = target,
            animationSpec = SwipeableListItemDefaults.swipeAnimationSpec
        ) { offset, _ ->
            contentOffset.floatValue = offset
        }
    }

    private fun isLeftActionReached(): Boolean {
        return contentOffset.floatValue > itemWidth.floatValue / 2
    }

    private fun isRightActionReached(): Boolean {
        return contentOffset.floatValue < -itemWidth.floatValue / 2
    }

    companion object {
        private const val ITEM_WIDTH_KEY = "minOffset"
        private const val CONTENT_OFFSET_KEY = "contentOffset"

        fun saver(
            swipeEnabled: (SwipeDirection) -> Boolean,
            leftAction: suspend SwipeableItemState.() -> Unit,
            rightAction: suspend SwipeableItemState.() -> Unit
        ): Saver<SwipeableItemStateImpl, *> = Saver(
            save = {
                bundleOf(
                    ITEM_WIDTH_KEY to it.itemWidth.floatValue,
                    CONTENT_OFFSET_KEY to it.contentOffset.floatValue
                )
            },
            restore = {
                SwipeableItemStateImpl(
                    initialWidth = it.getFloat(ITEM_WIDTH_KEY, 0f),
                    initialOffset = it.getFloat(CONTENT_OFFSET_KEY, 0f),
                    swipeEnabled = swipeEnabled,
                    leftAction = leftAction,
                    rightAction = rightAction
                )
            }
        )
    }
}


@Composable
fun rememberSwipeableItemState(
    initialItemWidth: Float = SwipeableListItemDefaults.defaultItemWidth,
    leftAction: suspend SwipeableItemState.() -> Unit = {},
    rightAction: suspend SwipeableItemState.() -> Unit = {},
    swipeEnabled: (SwipeDirection) -> Boolean = { true }
): SwipeableItemState {
    val swipeEnabledState = rememberUpdatedState(swipeEnabled)
    val leftActionState = rememberUpdatedState(leftAction)
    val rightActionState = rememberUpdatedState(rightAction)

    return rememberSaveable(
        initialItemWidth,
        swipeEnabledState,
        leftActionState,
        rightActionState,
        saver = SwipeableItemStateImpl.saver(
            swipeEnabled = swipeEnabledState.value,
            leftAction = leftActionState.value,
            rightAction = rightActionState.value
        )
    ) {
        SwipeableItemStateImpl(
            initialOffset = 0f,
            initialWidth = initialItemWidth,
            swipeEnabled = swipeEnabledState.value,
            leftAction = leftActionState.value,
            rightAction = rightActionState.value
        )
    }
}