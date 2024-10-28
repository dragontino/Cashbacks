package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.CashbackUtils.calculateNumberOfDaysBeforeExpiration
import com.cashbacks.app.util.CashbackUtils.displayableAmount
import com.cashbacks.app.util.CashbackUtils.getDisplayableExpirationDate
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.model.PreviewBankCard
import com.cashbacks.domain.model.Shop
import kotlinx.datetime.LocalDate

@Composable
fun CashbackComposable(
    cashback: Cashback,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val state = rememberScrollableListItemState(isSwiped)
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

    ScrollableListItem(
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
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "delete",
                    modifier = Modifier.scale(1.2f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            verticalArrangement =  Arrangement.spacedBy(verticalPadding),
            modifier = Modifier
                .padding(vertical = verticalPadding)
                .fillMaxWidth()
        ) {
            if (cashback is FullCashback) {
                CashbackRow(
                    title = when (cashback.owner) {
                        is Category -> stringResource(R.string.category_title)
                        is Shop -> stringResource(R.string.shop_title)
                    },
                    content = cashback.owner.name,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            CashbackRow(
                title = stringResource(R.string.amount),
                content = cashback.displayableAmount,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )

            cashback.expirationDate?.let { expirationDate ->
                CashbackRow(
                    title = when {
                        cashback.calculateNumberOfDaysBeforeExpiration() < 0 -> {
                            stringResource(R.string.expired)
                        }
                        else -> {
                            stringResource(R.string.expires)
                        }
                    },
                    content = cashback.getDisplayableExpirationDate(),
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            CashbackRow(
                title = stringResource(R.string.on_card),
                content = "${cashback.bankCard.hiddenLastDigitsOfNumber}\t(${cashback.bankCard.name})",
                modifier = Modifier.padding(horizontal = horizontalPadding)

            )

            if (cashback.comment.isNotBlank()) {
                HorizontalDivider()
                CashbackRow(
                    title = stringResource(R.string.comment),
                    content = cashback.comment,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }
    }
}


@Composable
private fun CashbackRow(
    title: CharSequence,
    content: CharSequence,
    modifier: Modifier = Modifier,
) {
    val titleStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start
    )
    val contentStyle = MaterialTheme.typography.bodyMedium.copy(
        textAlign = TextAlign.Start
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .then(modifier)
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        when (title) {
            is String -> Text(
                text = title,
                style = titleStyle
            )

            is AnnotatedString -> Text(
                text = title,
                style = titleStyle
            )
        }

        when (content) {
            is String -> Text(
                text = content,
                style = contentStyle,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            is AnnotatedString -> Text(
                text = content,
                style = contentStyle,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}


@Preview(locale = "ru")
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
                expirationDate = LocalDate(dayOfMonth = 26, monthNumber = 10, year = 2024),
                comment = "Hello world!\nGoodbye, Angels!",
                owner = BasicCategory(name = "Groceries")
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}