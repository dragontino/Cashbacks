package com.cashbacks.app.ui.managment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.cashbacks.app.ui.composables.ScrollableListItemDefaults

class ScrollableListItemState(
    minOffset: Float,
    initialOffset: Float
) {

    private val minOffset = mutableFloatStateOf(minOffset)
    private val maxOffset = 0f

    val contentOffset = mutableFloatStateOf(initialOffset)

    val isSwiped = derivedStateOf { contentOffset.floatValue != maxOffset }

    /**
     * Определяет, можно ли скроллить
     * @param delta направление скролла. Значение < 0 означает сколл влево, значение > 0 — вправо
     */
    fun canSwipe(delta: Float): Boolean {
        return delta < 0 && contentOffset.floatValue > minOffset.floatValue
                || delta > 0 && contentOffset.floatValue < 0f
    }

    suspend fun onScroll(delta: Float) {
        contentOffset.floatValue = (contentOffset.floatValue + delta).coerceIn(
            minimumValue = this.minOffset.floatValue,
            maximumValue = this.maxOffset
        )
        animateOffset()
    }

    fun updateMinOffset(offset: Float) {
        minOffset.floatValue = offset
        contentOffset.floatValue = contentOffset.floatValue.coerceAtLeast(offset)
    }

    suspend fun swipe() {
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

        internal val Saver: Saver<ScrollableListItemState, *> = Saver(
            save = {
                mapOf(
                    MIN_OFFSET_KEY to it.minOffset.floatValue,
                    CONTENT_OFFSET_KEY to it.contentOffset.floatValue
                )
            },
            restore = {
                ScrollableListItemState(
                    minOffset = it[MIN_OFFSET_KEY] ?: 0f,
                    initialOffset = it[CONTENT_OFFSET_KEY] ?: 0f
                )
            }
        )
    }
}


@Composable
fun rememberScrollableListItemState(
    initialIsSwiped: Boolean = false,
    minOffset: Float = ScrollableListItemDefaults.initialMinOffset
): ScrollableListItemState {
    val isSwipedState = rememberUpdatedState(initialIsSwiped)
    return rememberSaveable(saver = ScrollableListItemState.Saver) {
        ScrollableListItemState(
            minOffset,
            initialOffset = if (isSwipedState.value) minOffset else 0f
        )
    }
}