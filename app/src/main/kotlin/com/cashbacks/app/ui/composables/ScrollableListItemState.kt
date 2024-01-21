package com.cashbacks.app.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

class ScrollableListItemState(
    minOffset: Float,
    initialOffset: Float
) : ScrollableState {

    private val minOffset = mutableFloatStateOf(minOffset)
    private val maxOffset = 0f

    val contentOffset = mutableFloatStateOf(initialOffset)

    private val scrollableState = ScrollableState { delta ->
        contentOffset.floatValue = (contentOffset.floatValue + delta).coerceIn(
            minimumValue = this.minOffset.floatValue,
            maximumValue = this.maxOffset
        )
        /*scope.launch { animateOffset() }*/
        delta
    }

    fun updateMinOffset(offset: Float) {
        minOffset.floatValue = offset
        contentOffset.floatValue = contentOffset.floatValue.coerceAtLeast(offset)
    }

    suspend fun animateOffset() {
        val targetOffset = when {
            contentOffset.floatValue < minOffset.floatValue / 2 -> minOffset.floatValue
            else -> maxOffset
        }
        animateOffset(targetOffset)
    }

    suspend fun animateOffset(target: Float) {
        animate(
            initialValue = contentOffset.floatValue,
            targetValue = target,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        ) { offset, _ ->
            contentOffset.floatValue = offset
        }
    }


    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float {
        return scrollableState.dispatchRawDelta(delta)
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        scrollableState.scroll(scrollPriority, block)
    }

    companion object {
        private const val minOffsetKey = "minOffset"
        private const val contentOffsetKey = "contentOffset"

        val Saver: Saver<ScrollableListItemState, *> = Saver(
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
fun rememberScrollableListItemState(minOffset: Float, isSwiped: Boolean): ScrollableListItemState {
    val isSwipedState = rememberUpdatedState(isSwiped)
    return rememberSaveable(saver = ScrollableListItemState.Saver) {
        ScrollableListItemState(
            minOffset,
            initialOffset = if (isSwipedState.value) minOffset else 0f
        )
    }
}