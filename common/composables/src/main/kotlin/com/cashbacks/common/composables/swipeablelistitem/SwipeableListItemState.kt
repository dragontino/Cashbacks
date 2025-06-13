package com.cashbacks.common.composables.swipeablelistitem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable


interface SwipeableListItemState {
    /**
     * Определяет, можно ли скроллить
     * @param delta направление скролла. Значение < 0 означает сколл влево, значение > 0 — вправо
     */
    fun canSwipe(delta: Float): Boolean

    suspend fun swipe()

    val isSwiped: State<Boolean>

    suspend fun onSwipe(delta: Float): Float

    fun coerceMinOffset(minOffset: Float)

    val contentOffset: FloatState
}


private class SwipeableListItemStateImpl(
    minOffset: Float,
    initialOffset: Float
) : SwipeableListItemState {

    private val minOffset = mutableFloatStateOf(minOffset)
    private val maxOffset = 0f

    override val contentOffset = mutableFloatStateOf(initialOffset)

    override val isSwiped = derivedStateOf { contentOffset.floatValue < maxOffset }


    override fun canSwipe(delta: Float): Boolean {
        return delta < 0 && contentOffset.floatValue > minOffset.floatValue
                || delta > 0 && contentOffset.floatValue < 0f
    }

    override suspend fun onSwipe(delta: Float): Float {
        val previousOffset = contentOffset.floatValue
        contentOffset.floatValue = (contentOffset.floatValue + delta).coerceIn(
            minimumValue = this.minOffset.floatValue,
            maximumValue = this.maxOffset
        )
        val consumedOffset = contentOffset.floatValue - previousOffset
        animateOffset()
        return consumedOffset
    }

    override fun coerceMinOffset(minOffset: Float) {
        this.minOffset.floatValue = minOffset
        contentOffset.floatValue = contentOffset.floatValue.coerceAtLeast(minOffset)
    }

    override suspend fun swipe() {
        when (contentOffset.floatValue) {
            minOffset.floatValue -> swipeToRight()
            else -> swipeToLeft()
        }
    }

    private suspend fun swipeToLeft() {
        animateOffset(minOffset.floatValue)
    }

    private suspend fun swipeToRight() {
        animateOffset(maxOffset)
    }

    private suspend fun animateOffset() {
        val targetOffset = when {
            contentOffset.floatValue < minOffset.floatValue / 2 -> minOffset.floatValue
            else -> maxOffset
        }
        animateOffset(targetOffset)
    }

    private suspend fun animateOffset(target: Float) {
        animate(
            initialValue = contentOffset.floatValue,
            targetValue = target,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        ) { offset, _ ->
            contentOffset.floatValue = offset
        }
    }

    companion object {
        private const val MIN_OFFSET_KEY = "minOffset"
        private const val CONTENT_OFFSET_KEY = "contentOffset"

        val Saver: Saver<SwipeableListItemStateImpl, *> = Saver(
            save = {
                mapOf(
                    MIN_OFFSET_KEY to it.minOffset.floatValue,
                    CONTENT_OFFSET_KEY to it.contentOffset.floatValue
                )
            },
            restore = {
                SwipeableListItemStateImpl(
                    minOffset = it[MIN_OFFSET_KEY] ?: 0f,
                    initialOffset = it[CONTENT_OFFSET_KEY] ?: 0f
                )
            }
        )
    }
}


@Composable
fun rememberSwipeableListItemState(
    initialIsSwiped: Boolean = false,
    minOffset: Float = SwipeableListItemDefaults.initialMinOffset
): SwipeableListItemState {
    val isSwipedState = rememberUpdatedState(initialIsSwiped)
    return rememberSaveable(saver = SwipeableListItemStateImpl.Saver) {
        SwipeableListItemStateImpl(
            minOffset,
            initialOffset = if (isSwipedState.value) minOffset else 0f
        )
    }
}