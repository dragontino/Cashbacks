package com.cashbacks.features.cashback.presentation.api.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import com.cashbacks.common.composables.swipeablelistitem.EditDeleteContent
import com.cashbacks.common.composables.swipeablelistitem.SwipeableListItem
import com.cashbacks.common.composables.swipeablelistitem.rememberSwipeableListItemState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.expandedAnimationSpec
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.OnClick
import com.cashbacks.common.utils.today
import com.cashbacks.features.bankcard.domain.model.PreviewBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.displayableAmount
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDatesTitle
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDisplayableDatesText
import kotlinx.datetime.Clock
import java.util.Currency

@Composable
fun MaxCashbackOwnerComposable(
    cashbackOwner: CashbackOwner,
    maxCashbacks: Set<Cashback>,
    onClick: OnClick,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onClickToCashback: ((Cashback) -> Unit)? = null,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val listItemState = rememberSwipeableListItemState(isSwiped)
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
            animationSpec = expandedAnimationSpec()
        )
    }

    SwipeableListItem(
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
                    if (cashbackOwner is CashbackOwner.CategoryShop) {
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

                    if (maxCashbacks.isEmpty()) {
                        Text(
                            text = when (cashbackOwner) {
                                is CashbackOwner.Category -> stringResource(R.string.no_cashbacks_for_category)
                                is CashbackOwner.Shop -> stringResource(R.string.no_cashbacks_for_shop)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.animate()
                        )
                    }
                }

                if (maxCashbacks.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedState.value = !expandedState.value }
                            .padding(16.dp)
                            .fillMaxHeight()
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            maxCashbacks.forEach { cashback ->
                                Text(
                                    text = cashback.displayableAmount,
                                    color = MaterialTheme.colorScheme.onBackground.animate(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

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

            if (maxCashbacks.isNotEmpty()) {
                AnimatedVisibility(
                    visible = expandedState.value,
                    enter = expandVertically(animationSpec = expandedAnimationSpec()),
                    exit = shrinkVertically(animationSpec = expandedAnimationSpec())
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { expandedState.value = !expandedState.value }
                            .fillMaxWidth()
                    ) {
                        maxCashbacks.forEach { cashback ->
                            HorizontalDivider()

                            Column(
                                modifier = Modifier
                                    .let {
                                        if (onClickToCashback != null) {
                                            Modifier.clickable {
                                                onClickToCashback(cashback)
                                            }
                                        } else Modifier
                                    }
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (maxCashbacks.size > 1) {
                                        Text(
                                            text = when (cashback.measureUnit) {
                                                is MeasureUnit.Percent -> stringResource(R.string.cashback_title)
                                                is MeasureUnit.Currency -> stringResource(R.string.discount)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = cashback.displayableAmount,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily(VerdanaFont),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }

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
                                        text = cashback.bankCard
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
                                        text = cashback.getDatesTitle(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = cashback.getDisplayableDatesText(),
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
}



@Preview
@Composable
private fun MaxCashbackOwnerComposablePreview() {
    CashbacksTheme(isDarkTheme = false) {
        MaxCashbackOwnerComposable(
            cashbackOwner = CashbackOwner.BasicShop(
                id = 0,
                name = buildString {
                    repeat(6) {
                        append("Test shop ")
                    }
                },
            ),
            maxCashbacks = setOf(
                BasicCashback(
                    id = 10,
                    amount = "25",
                    measureUnit = MeasureUnit.Percent,
                    bankCard = PreviewBankCard(number = "1234123412341234"),
                    expirationDate = Clock.System.today()
                ),
                BasicCashback(
                    id = 11,
                    amount = "3000",
                    measureUnit = MeasureUnit.Currency(Currency.getInstance("RUB")),
                    bankCard = PreviewBankCard(number = "4321432143214321"),
                    expirationDate = Clock.System.today()
                )
            ),
            onClick = {},
            isEditing = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}