package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.model.PaymentSystemMapper
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.PaymentSystem

@Composable
fun CashbackComposable(
    cashback: Cashback,
    onClick: () -> Unit,
    parentType: String? = null,
    parentName: String? = null,
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
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            if (parentType != null && parentName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = parentType,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = parentName,
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cashback_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${cashback.roundedAmount}%",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.validity_period),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = cashback.expirationDate ?: stringResource(R.string.indefinitely_period),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.on_card),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )

                cashback.bankCard.paymentSystem?.let {
                    PaymentSystemMapper.PaymentSystemImage(
                        paymentSystem = it,
                        drawBackground = false,
                        maxWidth = 30.dp
                    )
                }
                Text(
                    text = cashback.bankCard.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cashback.bankCard.hiddenLastDigitsOfNumber,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (cashback.comment.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
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
    CashbackComposable(
        cashback = Cashback(
            id = 0,
            bankCard = BasicBankCard(
                paymentSystem = PaymentSystem.MasterCard,
                name = "My Card",
                number = "1111 2222 3333 4444"
            ),
            amount = "12",
            expirationDate = null,
            comment = "Hello world!"
        ),
        onClick = {},
        parentName = "Products"
    )
}