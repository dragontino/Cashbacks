package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.model.PaymentSystemUtils
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.MaxCashbackOwner
import com.cashbacks.domain.model.ParentCashbackOwner
import com.cashbacks.domain.model.Shop

@Composable
internal fun MaxCashbackOwnerComposable(
    cashbackOwner: MaxCashbackOwner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val listItemState = rememberScrollableListItemState(isSwiped)
    val onClickState = rememberUpdatedState(onClick)

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

    ScrollableListItem(
        state = listItemState,
        onClick = if (isEditing) null else onClickState.value,
        hiddenContent = {
            EditDeleteContent(
                onEditClick = onEdit,
                onDeleteClick = onDelete
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        ListItem(
            overlineContent = {
                if (cashbackOwner is ParentCashbackOwner) {
                    Text(
                        text = cashbackOwner.parent.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            headlineContent = {
                Text(
                    text = cashbackOwner.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (cashbackOwner.maxCashback == null) {
                    Text(
                        text = when (cashbackOwner) {
                            is Category -> stringResource(R.string.no_cashbacks_for_category)
                            is Shop -> stringResource(R.string.no_cashbacks_for_shop)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                cashbackOwner.maxCashback?.let { BasicInfoCashback(cashback = it) }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                supportingColor = MaterialTheme.colorScheme.error.animate(),
                trailingIconColor = MaterialTheme.colorScheme.primary.animate(),
                overlineColor = MaterialTheme.colorScheme.onBackground.animate()
            )
        )
    }
}



@Composable
private fun BasicInfoCashback(cashback: Cashback, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onBackground.animate()
    val textStyle = MaterialTheme.typography.bodySmall

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = "${cashback.roundedAmount}%",
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cashback.bankCard.paymentSystem?.let {
                PaymentSystemUtils.PaymentSystemImage(
                    paymentSystem = it,
                    drawBackground = false,
                    maxWidth = 30.dp
                )
            }
            Text(text = cashback.bankCard.name, color = textColor, style = textStyle)
            Text(
                text = cashback.bankCard.hiddenLastDigitsOfNumber,
                color = textColor,
                style = textStyle
            )
        }
    }
}



@Preview
@Composable
private fun MaxCashbackOwnerComposablePreview() {
    MaxCashbackOwnerComposable(
        cashbackOwner = BasicCategoryShop(
            id = 0,
            name = "Test shop",
            maxCashback = null,
            parent = BasicCategory(name = "Test category")
        ),
        onClick = {},
        isEditing = true
    )
}