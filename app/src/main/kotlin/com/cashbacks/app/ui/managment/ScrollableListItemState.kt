package com.cashbacks.app.ui.managment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
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

    val isSwiped: Boolean get() = contentOffset.floatValue != maxOffset

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
        val targetOffset = when (contentOffset.floatValue) {
            minOffset.floatValue -> maxOffset
            else -> minOffset.floatValue
        }
        animateOffset(targetOffset)
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
        private const val minOffsetKey = "minOffset"
        private const val contentOffsetKey = "contentOffset"

        internal val Saver: Saver<ScrollableListItemState, *> = Saver(
            save = {
                mapOf(
                    minOffsetKey to it.minOffset.floatValue,
                    contentOffsetKey to it.contentOffset.floatValue
                )
            },
            restore = {
                ScrollableListItemState(
                    minOffset = it[minOffsetKey] ?: 0f,
                    initialOffset = it[contentOffsetKey] ?: 0f
                )
            }
        )
    }
}


@Composable
fun rememberScrollableListItemState(
    isSwiped: Boolean,
    minOffset: Float = ScrollableListItemDefaults.initialMinOffset
): ScrollableListItemState {
    val isSwipedState = rememberUpdatedState(isSwiped)
    return rememberSaveable(saver = ScrollableListItemState.Saver) {
        ScrollableListItemState(
            minOffset,
            initialOffset = if (isSwipedState.value) minOffset else 0f
        )
    }
}