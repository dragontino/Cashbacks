package com.cashbacks.features.cashback.presentation.api.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.swipeable.SwipeableListItem
import com.cashbacks.common.composables.swipeable.SwipeableListItemDefaults
import com.cashbacks.common.composables.swipeable.rememberSwipeableItemState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.expandedAnimationSpec
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.OnClick
import com.cashbacks.common.utils.now
import com.cashbacks.features.bankcard.domain.model.PreviewBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.displayableAmount
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDatesTitle
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDisplayableDatesText
import kotlinx.datetime.LocalDate
import java.util.Currency

@Stable
@Composable
fun MaxCashbackOwnerComposable(
    maxCashback: Cashback?,
    modifier: Modifier = Modifier,
    title: @Composable RowScope.() -> Unit = {},
    mainContent: @Composable RowScope.() -> Unit = {},
    supportingContent: @Composable RowScope.() -> Unit = {},
    isEnabledToSwipe: Boolean = true,
    isExpanded: Boolean = false,
    onClick: OnClick = {},
    onSwipeStatusChanged: (isOnSwipe: Boolean) -> Unit = {},
    onClickToCashback: () -> Unit = {},
    onExpandedStatusChanged: (isExpanded: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val listItemState = rememberSwipeableItemState(
        leftAction = { onEdit() },
        rightAction = {
            onDelete()
            swipeToLeft()
        },

    )
    val onClickState = rememberUpdatedState(onClick)

    val expandAnimation = remember { Animatable(initialValue = 0f) }
    val arrowDegrees = remember {
        derivedStateOf { expandAnimation.value * 180 }
    }
    val elevation = remember {
        derivedStateOf {
            (4.9 * expandAnimation.value - .9).coerceAtLeast(0.0).dp
        }
    }

    LaunchedEffect(listItemState.isOnSwipe.value) {
        onSwipeStatusChanged(listItemState.isOnSwipe.value)
    }

    LaunchedEffect(isEnabledToSwipe) {
        if (!isEnabledToSwipe && listItemState.contentOffset.floatValue != 0f) {
            listItemState.swipeToZero()
        }
    }

    LaunchedEffect(isExpanded) {
        expandAnimation.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = expandedAnimationSpec()
        )
    }

    SwipeableListItem(
        state = listItemState,
        onClick = onClickState.value,
        leftActionIcon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "pencil",
                modifier = Modifier.padding(16.dp)
            )
        },
        rightActionIcon = {
            Icon(
                imageVector = Icons.Rounded.DeleteForever,
                contentDescription = "trash can",
                modifier = Modifier.padding(16.dp)
            )
        },
        isEnabledToSwipe = isEnabledToSwipe,
        shape = MaterialTheme.shapes.small,
        colors = SwipeableListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
                    .mix(MaterialTheme.colorScheme.background)
                    .ratio((listItemState.swipeOffsetRatio.value * 2 + expandAnimation.value).coerceAtMost(1f)),
            leftActionColors = SwipeableListItemDefaults.actionColors(
                containerColor = MaterialTheme.colorScheme.background,
                clickedContainerColor = Color.Blue,
                contentColor = MaterialTheme.colorScheme.onBackground,
                clickedContentColor = Color.White
            ),
            rightActionColors = SwipeableListItemDefaults.actionColors(
                containerColor = MaterialTheme.colorScheme.background,
                clickedContainerColor = Color.Red,
                contentColor = MaterialTheme.colorScheme.onBackground,
                clickedContentColor = Color.White
            )
        ),
        tonalElevation = elevation.value,
        shadow = null,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(3f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        content = title,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        content = mainContent,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        content = supportingContent,
                    )
                }

                val expandClickModifier = when (maxCashback) {
                    null -> Modifier
                    else -> Modifier.clickable {
                        onExpandedStatusChanged(isExpanded.not())
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .then(expandClickModifier)
                        .padding(16.dp)
                        .fillMaxHeight()
                ) {
                    if (maxCashback == null) {
                        Text(
                            text = "0" + MeasureUnit.Percent.toString(),
                            color = MaterialTheme.colorScheme.error.animate(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = maxCashback.displayableAmount,
                            color = MaterialTheme.colorScheme.onBackground.animate(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "arrow right",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = arrowDegrees.value
                            }
                        )
                    }
                }
            }

            if (maxCashback != null) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = expandedAnimationSpec()),
                    exit = shrinkVertically(animationSpec = expandedAnimationSpec())
                ) {
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .clickable(onClick = onClickToCashback)
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
                                text = maxCashback.bankCard
                                    .getHiddenNumber()
                                    .takeLast(8)
                                    .withSpaces(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily(VerdanaFont),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = maxCashback.getDatesTitle(),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = maxCashback.getDisplayableDatesText(),
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



@Preview(showSystemUi = true)
@Composable
private fun MaxCashbackOwnerComposablePreview() {
    CashbacksTheme(isDarkTheme = false) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(3) { index ->
                MaxCashbackOwnerComposable(
                    mainContent = {
                        Text(
                            text = buildString {
                                repeat(6) {
                                    append("Test shop ")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxCashback = BasicCashback(
                        id = 11,
                        amount = "3000",
                        measureUnit = MeasureUnit.Currency(Currency.getInstance("RUB")),
                        bankCard = PreviewBankCard(number = "4321432143214321"),
                        expirationDate = LocalDate.now()
                    ),
                    onClick = {},
                )
            }
        }
    }
}