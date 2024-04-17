package com.cashbacks.app.ui.composables

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cashbacks.app.R
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop


@Composable
internal fun ShopComposable(
    categoryShop: CategoryShop,
    onClick: () -> Unit,
    isEditing: Boolean = false,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    ShopComposable(
        shop = categoryShop.asShop(),
        category = categoryShop.parentCategory,
        onClick = onClick,
        isEditing = isEditing,
        isSwiped = isSwiped,
        onSwipe = onSwipe,
        onEdit = onEdit,
        onDelete = onDelete
    )
}


@Composable
internal fun ShopComposable(
    shop: Shop,
    onClick: () -> Unit,
    isEditing: Boolean = false,
    category: Category? = null,
    isSwiped: Boolean = false,
    onSwipe: suspend (isSwiped: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
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
        containerColor = MaterialTheme.colorScheme.background
    ) {
        ListItem(
            overlineContent = category?.let {
                {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            headlineContent = {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (shop.maxCashback == null) {
                    Text(
                        text = stringResource(R.string.no_cashbacks_for_shop),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                shop.maxCashback?.let { BasicInfoCashback(cashback = it) }
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


@Preview
@Composable
private fun ShopComposablePreview() {
    ShopComposable(
        categoryShop = CategoryShop(
            id = 0,
            name = "Test shop",
            maxCashback = null,
            parentCategory = Category(name = "Test category")
        ),
        onClick = {},
        isEditing = true
    )
}