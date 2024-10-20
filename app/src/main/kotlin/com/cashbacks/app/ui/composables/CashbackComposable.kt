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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.model.PaymentSystemUtils
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.model.PreviewBankCard
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.calculateNumberOfDaysBeforeExpiration
import com.cashbacks.domain.model.roundedAmount
import com.cashbacks.domain.util.getDisplayableDateString

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            if (cashback is FullCashback) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = when (cashback.owner) {
                            is Category -> stringResource(R.string.category_title)
                            is Shop -> stringResource(R.string.shop_title)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cashback.owner.name,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${cashback.roundedAmount}%",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            cashback.expirationDate?.let { expirationDate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.expires),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val numberOfDaysBeforeExpiration = cashback.calculateNumberOfDaysBeforeExpiration()
                    Text(
                        text = when (numberOfDaysBeforeExpiration) {
                            0 -> stringResource(R.string.today)
                            1 -> stringResource(R.string.tomorrow)
                            2 -> stringResource(R.string.after_tomorrow)
                            else -> getDisplayableDateString(
                                dateString = expirationDate,
                                inputFormatBuilder = Cashback.DateFormat
                            )
                        }.lowercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = when (numberOfDaysBeforeExpiration) {
                            in 0 .. 2 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }.animate()
                    )
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.on_card),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )

                cashback.bankCard.paymentSystem?.let {
                    PaymentSystemUtils.PaymentSystemImage(
                        paymentSystem = it,
                        drawBackground = false,
                        maxWidth = 30.dp
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            MaterialTheme.typography.bodyMedium.toSpanStyle()
                        ) {
                            appendLine(cashback.bankCard.name)
                        }
                        append(cashback.bankCard.hiddenLastDigitsOfNumber)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right
                )
            }

            if (cashback.comment.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.comment),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cashback.comment,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
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
                expirationDate = null,
                comment = "Hello world!",
                owner = BasicCategory(name = "Products")
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}