package com.cashbacks.features.cashback.presentation.api.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.swipeablelistitem.SwipeableListItem
import com.cashbacks.common.composables.swipeablelistitem.rememberSwipeableListItemState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.maxOfOrNull
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import com.cashbacks.features.bankcard.domain.model.PreviewBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.displayableAmount
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDatesTitle
import com.cashbacks.features.cashback.presentation.api.utils.CashbackPresentationUtils.getDisplayableDatesText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

@Composable
fun CashbackComposable(
    cashback: Cashback,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val state = rememberSwipeableListItemState(isSwiped)
    val verticalPadding = 12.dp
    val horizontalPadding = 12.dp

    LaunchedEffect(isSwiped) {
        if (isSwiped != state.isSwiped.value) {
            state.swipe()
        }
    }

    LaunchedEffect(state.isSwiped.value) {
        if (state.isSwiped.value != isSwiped) {
            onSwipe(state.isSwiped.value)
        }
    }

    SwipeableListItem(
        state = state,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        hiddenContent = {
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary.animate()
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = "delete",
                    modifier = Modifier.scale(1.2f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Layout(
            measurePolicy = CashbackMeasurePolicy(
                horizontalPadding = horizontalPadding,
                verticalSpacing = verticalPadding
            ),
            content = {
                if (cashback is FullCashback) {
                    CashbackRowTitle(
                        title = when (cashback.owner) {
                            is CashbackOwner.Category -> stringResource(R.string.category_title)
                            is CashbackOwner.Shop -> stringResource(R.string.shop_title)
                        },
                        modifier = Modifier.layoutId(LayoutIds.Owner.titleId)
                    )
                    CashbackRowContent(
                        content = buildString {
                            append(cashback.owner.name)
                            val owner = cashback.owner
                            if (owner is CashbackOwner.CategoryShop) {
                                val parentName = owner.parent.name
                                append(" ", "($parentName)")
                            }
                        },
                        modifier = Modifier.layoutId(LayoutIds.Owner.contentId)
                    )
                }


                CashbackRowTitle(
                    title = stringResource(R.string.amount),
                    modifier = Modifier.layoutId(LayoutIds.Amount.titleId)
                )
                CashbackRowContent(
                    content = cashback.displayableAmount,
                    modifier = Modifier.layoutId(LayoutIds.Amount.contentId)
                )


                CashbackRowTitle(
                    title = cashback.getDatesTitle(),
                    modifier = Modifier.layoutId(LayoutIds.Dates.titleId)
                )
                CashbackRowContent(
                    content = cashback.getDisplayableDatesText(),
                    modifier = Modifier.layoutId(LayoutIds.Dates.contentId)
                )


                CashbackRowTitle(
                    title = stringResource(R.string.on_card),
                    modifier = Modifier.layoutId(LayoutIds.Card.titleId)
                )
                CashbackRowContent(
                    content = buildString {
                        append(cashback.bankCard.getHiddenNumber().takeLast(8).withSpaces())
                        if (cashback.bankCard.name.isNotBlank()) {
                            append("\t\t(", cashback.bankCard.name, ")")
                        }
                    },
                    modifier = Modifier.layoutId(LayoutIds.Card.contentId)
                )


                if (cashback.comment.isNotBlank()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .layoutId(LayoutIds.DIVIDER)
                            .fillMaxWidth()
                    )
                    CashbackRowTitle(
                        title = stringResource(R.string.comment),
                        modifier = Modifier.layoutId(LayoutIds.Comment.titleId)
                    )
                    CashbackRowContent(
                        content = cashback.comment,
                        modifier = Modifier.layoutId(LayoutIds.Comment.contentId)
                    )
                }
            },
            modifier = Modifier
                .padding(vertical = verticalPadding)
                .fillMaxWidth()
        )
    }
}


@Composable
private fun CashbackRowTitle(
    title: CharSequence,
    modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start
    )
    val modifier = modifier
        .padding(end = 20.dp)
        .wrapContentSize()

    when (title) {
        is String -> Text(
            text = title,
            style = style,
            modifier = modifier
        )

        is AnnotatedString -> Text(
            text = title,
            style = style,
            modifier = modifier
        )
    }
}


@Composable
private fun CashbackRowContent(
    content: CharSequence,
    modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.bodyMedium.copy(
        textAlign = TextAlign.Start
    )

    when (content) {
        is String -> Text(
            text = content,
            style = style,
            modifier = modifier.wrapContentSize()
        )

        is AnnotatedString -> Text(
            text = content,
            style = style,
            modifier = modifier.wrapContentWidth()
        )
    }
}



private class CashbackMeasurePolicy(val verticalSpacing: Dp, val horizontalPadding: Dp) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val constraints = constraints.copy(minWidth = 0, minHeight = 0)
        val paddedConstraints = constraints.copy(
            maxWidth = constraints.maxWidth - horizontalPadding.roundToPx() * 2
        )

        fun Map<String, Measurable>.measure(): Map<String, Placeable> {
            var maxTitleWidth = 0
            val resultMap = mutableMapOf<String, Placeable>()
            LayoutIds.MainItems.forEach {
                this[it.titleId]?.measure(
                    constraints = paddedConstraints.copy(
                        maxWidth = paddedConstraints.maxWidth / 2,
                        maxHeight = paddedConstraints.maxWidth / 2
                    )
                )?.let { placeableTitle ->
                    maxTitleWidth = maxOf(maxTitleWidth, placeableTitle.width)
                    resultMap[it.titleId] = placeableTitle
                }
            }

            LayoutIds.MainItems.forEach {
                this[it.contentId]?.measure(
                    constraints = paddedConstraints.copy(
                        maxWidth = paddedConstraints.maxWidth - maxTitleWidth
                            .coerceAtLeast(resultMap[it.titleId]!!.width),
                        maxHeight = paddedConstraints.maxWidth - maxTitleWidth
                    )
                )?.let { placeableContent ->
                    resultMap[it.contentId] = placeableContent
                }
            }

            this[LayoutIds.DIVIDER]?.measure(constraints)?.let {
                resultMap[LayoutIds.DIVIDER] = it
            }
            LayoutIds.Comment.let { comment ->
                this[comment.titleId]?.measure(
                    constraints = paddedConstraints.copy(
                        maxWidth = paddedConstraints.maxWidth / 2
                    )
                )?.let { resultMap[comment.titleId] = it }

                this[comment.contentId]?.measure(
                    constraints = paddedConstraints.copy(
                        maxWidth = paddedConstraints.maxWidth -
                                maxTitleWidth.coerceAtLeast(resultMap[comment.titleId]!!.width),
                        maxHeight = paddedConstraints.maxWidth - maxTitleWidth
                    )
                )?.let { resultMap[comment.contentId] = it }
            }

            return resultMap
        }


        val measurables = measurables.associateBy { it.layoutId.toString() }
        val placeables = measurables.measure()

        val maxTitleWidth = placeables
            .filter { LayoutIds.TITLE in it.key && it.key != LayoutIds.Comment.titleId }
            .maxOf { it.value.width }

        val layoutHeight = LayoutIds.AllItems
            .mapNotNull {
                maxOfOrNull(placeables[it.titleId]?.height, placeables[it.contentId]?.height)
            }
            .let { heights ->
                placeables[LayoutIds.DIVIDER]?.height?.let { heights + it } ?: heights
            }
            .reduceOrNull { acc, height ->
                acc + height + verticalSpacing.roundToPx()
            }
            ?: constraints.minHeight

        return layout(
            width = constraints.maxWidth,
            height = layoutHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        ) {
            var currentY = placeMainInfo(
                placeables = placeables,
                titleWidth = maxTitleWidth,
                verticalSpacingPx = verticalSpacing.roundToPx(),
                horizontalPaddingPx = horizontalPadding.roundToPx()
            )
            placeables[LayoutIds.DIVIDER]?.also {
                it.place(x = 0, y = currentY)
                currentY += it.height + verticalSpacing.roundToPx()
            }

            placeComment(
                placeables = placeables,
                titleWidth = maxTitleWidth,
                topPaddingPx = currentY,
                horizontalPaddingPx = horizontalPadding.roundToPx()
            )
        }
    }


    private fun Placeable.PlacementScope.placeMainInfo(
        placeables: Map<String, Placeable>,
        titleWidth: Int,
        verticalSpacingPx: Int,
        horizontalPaddingPx: Int
    ): Int {
        var currentY = 0
        LayoutIds.MainItems.forEach {
            val lineHeight = maxOfOrNull(
                placeables[it.titleId]?.height,
                placeables[it.contentId]?.height
            )

            placeables[it.titleId]?.placeRelative(x = horizontalPaddingPx, y = currentY)
            placeables[it.contentId]?.apply {
                placeRelative(
                    x = titleWidth + horizontalPaddingPx,
                    y = currentY + (lineHeight!! - height) / 2
                )
            }
            lineHeight?.let {
                currentY += it + verticalSpacingPx
            }
        }
        return currentY
    }


    private fun Placeable.PlacementScope.placeComment(
        placeables: Map<String, Placeable>,
        titleWidth: Int,
        topPaddingPx: Int,
        horizontalPaddingPx: Int
    ) {
        val titlePlaceable = placeables[LayoutIds.Comment.titleId]
        val contentPlaceable = placeables[LayoutIds.Comment.contentId]
        val lineHeight = maxOfOrNull(titlePlaceable?.height, contentPlaceable?.height)

        titlePlaceable?.placeRelative(
            x = horizontalPaddingPx,
            y = topPaddingPx
        )
        contentPlaceable?.placeRelative(
            x = maxOfOrNull(titlePlaceable?.width, titleWidth)!! + horizontalPaddingPx,
            y = topPaddingPx + (lineHeight!! - contentPlaceable.height) / 2
        )
    }
}



private object LayoutIds {
    const val TITLE = "Title"

    interface RowType {
        val titleId: String
        val contentId: String
    }

    object Owner : RowType {
        override val titleId = "Owner$TITLE"
        override val contentId: String = "OwnerName"
    }
    object Amount : RowType {
        override val titleId = "Amount$TITLE"
        override val contentId = "Amount"
    }
    object Dates : RowType {
        override val titleId = "Dates$TITLE"
        override val contentId = "Dates"
    }
    object Card : RowType {
        override val titleId = "Card$TITLE"
        override val contentId = "Card"
    }
    object Comment : RowType {
        override val titleId = "Comment$TITLE"
        override val contentId = "Comment"
    }
    const val DIVIDER = "Divider"

    val MainItems by lazy {
        listOf(Owner, Amount, Dates, Card)
    }
    val AllItems by lazy {
        MainItems + Comment
    }
}


@Preview
@Composable
private fun CashbackComposablePreview() {
    CashbacksTheme(isDarkTheme = false) {
        CashbackComposable(
            cashback = FullCashback(
                id = 0,
                bankCard = PreviewBankCard(
                    paymentSystem = PaymentSystem.MasterCard,
                    name = "My Card",
                    number = "1111222233334444"
                ),
                amount = "12",
                measureUnit = MeasureUnit.Percent,
                expirationDate = LocalDate(day = 26, month = Month.OCTOBER, year = 2024),
                comment = "Hello world! Goodbye, Angels! 11 1",
                owner = CashbackOwner.Category(
                    id = 1,
                    name = "5ka (Groceries)"
                )
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}