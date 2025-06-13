package com.cashbacks.features.shop.presentation.impl.mvi.model

import androidx.compose.runtime.Immutable
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.shop.domain.model.BasicShop
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop
import kotlinx.serialization.Serializable

@Immutable
@Serializable
internal data class EditableShop(
    val id: Long? = null,
    val parentCategory: Category? = null,
    val name: String = ""
) {
    constructor(shop: CategoryShop) : this(
        id = shop.id,
        parentCategory = shop.parent,
        name = shop.name
    )

    fun mapToShop(): Shop = BasicShop(
        id = id ?: 0L,
        name = this.name
    )

    fun mapToCategoryShop(): CategoryShop? {
        return CategoryShop(
            id = id ?: 0L,
            parent = parentCategory ?: return null,
            name = this.name
        )
    }
}