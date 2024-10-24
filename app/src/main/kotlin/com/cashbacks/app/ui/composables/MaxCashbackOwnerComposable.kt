package com.cashbacks.app.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.ui.theme.VerdanaFont
import com.cashbacks.app.util.CashbackUtils.displayableAmount
import com.cashbacks.app.util.CashbackUtils.getDisplayableExpirationDate
import com.cashbacks.app.util.OnClick
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.MaxCashbackOwner
import com.cashbacks.domain.model.ParentCashbackOwner
import com.cashbacks.domain.model.Shop

private object Animations {
    const val DURATION_MILLIS = 300
    fun <T> expandedAnimationSpec(): TweenSpec<T> = tween<T>(
        durationMillis = DURATION_MILLIS,
        easing = FastOutSlowInEasing
    )
}




@Composable
internal fun MaxCashbackOwnerComposable(
    cashbackOwner: MaxCashbackOwner,
    onClick: OnClick,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val listItemState = rememberScrollableListItemState(isSwiped)
    val onClickState = rememberUpdatedState(onClick)
    val expandedState = rememberSaveable { mutableStateOf(false) }

    val arrowRotationAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(isSwiped) {
        if (isSwiped != listItemState.isSwiped.value) {
            listItemState.swipe()
        }
    }

    LaunchedEffect(listItemState.isSwiped.value) {
        if (listItemState.isSwiped.value != isSwiped) {
            onSwipe(listItemState.isSwiped.value)
        }
    }

    LaunchedEffect(expandedState.value) {
        val targetDegrees = when {
            expandedState.value -> 180f
            else -> 0f
        }

        arrowRotationAnimation.animateTo(
            targetValue = targetDegrees,
            animationSpec = Animations.expandedAnimationSpec()
        )
    }

    ScrollableListItem(
        state = listItemState,
        onClick = onClickState.value.takeIf { !isEditing },
        hiddenContent = {
            EditDeleteContent(
                onEditClick = onEdit,
                onDeleteClick = onDelete
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp).weight(3f)
                ) {
                    if (cashbackOwner is ParentCashbackOwner) {
                        Text(
                            text = cashbackOwner.parent.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = cashbackOwner.name,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (cashbackOwner.maxCashback == null) {
                        Text(
                            text = when (cashbackOwner) {
                                is Category -> stringResource(R.string.no_cashbacks_for_category)
                                is Shop -> stringResource(R.string.no_cashbacks_for_shop)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.animate()
                        )
                    }
                }


                cashbackOwner.maxCashback?.let { cashback ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedState.value = !expandedState.value }
                            .padding(16.dp)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = cashback.displayableAmount,
                            color = MaterialTheme.colorScheme.onBackground.animate(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.animate(),
                            modifier = Modifier.graphicsLayer {
                                rotationZ = arrowRotationAnimation.value
                            }
                        )
                    }
                }
            }

            cashbackOwner.maxCashback?.let { cashback ->
                if (expandedState.value) {
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .clickable { expandedState.value = !expandedState.value }
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.on_card),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = cashback.bankCard.hiddenLastDigitsOfNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily(VerdanaFont),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }

                        cashback.expirationDate?.let { expirationDate ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.expires),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = cashback.getDisplayableExpirationDate(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily(VerdanaFont),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Preview
@Composable
private fun MaxCashbackOwnerComposablePreview() {
    CashbacksTheme(isDarkTheme = false) {
        MaxCashbackOwnerComposable(
            cashbackOwner = BasicCategoryShop(
                id = 0,
                name = buildString {
                    repeat(6) {
                        append("Test shop ")
                    }
                },
                maxCashback = null,
                parent = BasicCategory(name = "Test category")
            ),
            onClick = {},
            isEditing = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}